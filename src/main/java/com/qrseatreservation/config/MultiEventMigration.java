package com.qrseatreservation.config;

import com.qrseatreservation.core.enums.EventStatus;
import com.qrseatreservation.core.enums.TicketType;
import com.qrseatreservation.core.model.Event;
import com.qrseatreservation.core.repository.EventRepository;
import com.qrseatreservation.core.repository.SettingRepository;
import com.qrseatreservation.core.service.SettingService;
import com.qrseatreservation.core.service.TicketCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * One-time, idempotent upgrade of a single-event database to the multi-event schema.
 * <p>
 * Runs after Hibernate's schema update (which adds the nullable {@code event_id} columns) and before the web server
 * accepts requests. On the first run all existing data is assigned to a newly created ACTIVE event, so the
 * running campaign keeps working unchanged. On every run it makes sure each event has settings, drops the old global
 * unique indexes that would block reusing table / category names in later events, and enforces
 * {@code event_id NOT NULL} at database level.
 */
@Component
@Slf4j
public class MultiEventMigration implements SmartInitializingSingleton {

    static final List<String> SCOPED_TABLES = List.of(
            "setting", "ticket", "ticket_category", "table_detail", "venue_zone", "seller_detail", "reservation_detail");

    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[A-Za-z0-9_]+");

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final EventRepository eventRepository;
    private final SettingRepository settingRepository;
    private final SettingService settingService;
    private final TicketCategoryService ticketCategoryService;

    public MultiEventMigration(JdbcTemplate jdbc, PlatformTransactionManager transactionManager,
                               EventRepository eventRepository, SettingRepository settingRepository,
                               SettingService settingService, TicketCategoryService ticketCategoryService) {
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactionManager);
        this.eventRepository = eventRepository;
        this.settingRepository = settingRepository;
        this.settingService = settingService;
        this.ticketCategoryService = ticketCategoryService;
    }

    @Override
    public void afterSingletonsInstantiated() {
        log.info("Checking multi-event schema");
        tx.executeWithoutResult(status -> assignLegacyDataAndEnsureSettings());
        dropLegacyUniqueIndex("table_detail", "table_name");
        dropLegacyUniqueIndex("ticket_category", "name");
        SCOPED_TABLES.forEach(this::enforceNotNull);
        applyOnce("user-events-v1", this::assignExistingUsersToActiveEvent);
        syncTicketTypeEnum();
        dropSingleActiveConstraint();
        log.info("Multi-event schema ready");
    }

    private void assignLegacyDataAndEnsureSettings() {
        long events = count("SELECT COUNT(*) FROM events");
        if (events == 0) {
            List<Map<String, Object>> legacy = jdbc.queryForList(
                    "SELECT id, event_name, venue FROM setting ORDER BY id LIMIT 1");
            String name = legacy.isEmpty() ? null : (String) legacy.get(0).get("event_name");
            String venue = legacy.isEmpty() ? null : (String) legacy.get(0).get("venue");
            boolean hasLegacyData = !legacy.isEmpty() || count("SELECT COUNT(*) FROM ticket") > 0;
            if (name == null || name.isBlank()) name = hasLegacyData ? "Event 1" : "My Event";

            Event event = eventRepository.save(Event.builder()
                    .name(name.trim())
                    .venue(venue)
                    .status(EventStatus.ACTIVE)
                    .activatedAt(LocalDateTime.now())
                    .createdBy("system-migration")
                    .updatedBy("system-migration")
                    .build());
            log.info("Created event id={} '{}' for existing data", event.getId(), event.getName());

            if (!legacy.isEmpty()) {
                // Only one settings row per event is allowed; the legacy app always used the first one
                Number settingId = (Number) legacy.get(0).get("id");
                jdbc.update("UPDATE setting SET event_id = ? WHERE id = ? AND event_id IS NULL", event.getId(), settingId.longValue());
                long extra = count("SELECT COUNT(*) FROM setting WHERE event_id IS NULL");
                if (extra > 0) log.warn("{} unused extra settings row(s) left without an event", extra);
            }
            for (String table : SCOPED_TABLES) {
                if ("setting".equals(table)) continue;
                int updated = jdbc.update("UPDATE `" + table + "` SET event_id = ? WHERE event_id IS NULL", event.getId());
                if (updated > 0) log.info("Assigned {} existing row(s) of {} to event id={}", updated, table, event.getId());
            }
        } else {
            for (String table : SCOPED_TABLES) {
                long orphans = count("SELECT COUNT(*) FROM `" + table + "` WHERE event_id IS NULL");
                if (orphans > 0) log.error("{} row(s) in {} have no event and are not visible in the app", orphans, table);
            }
        }

        for (Event event : eventRepository.findAll()) {
            if (settingRepository.findByEventId(event.getId()).isEmpty()) {
                settingService.createDefaultSetting(event);
            }
            if (event.getStatus() != EventStatus.ARCHIVED) {
                ticketCategoryService.initializeDefaultCategories(event);
            }
        }
    }

    /** Drops a single-column UNIQUE index (from the single-event schema) so names can repeat across events. */
    private void dropLegacyUniqueIndex(String table, String column) {
        List<String> indexes = jdbc.queryForList("""
                SELECT INDEX_NAME FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND NON_UNIQUE = 0 AND INDEX_NAME <> 'PRIMARY'
                GROUP BY INDEX_NAME
                HAVING COUNT(*) = 1 AND MAX(COLUMN_NAME) = ?
                """, String.class, table, column);
        for (String index : indexes) {
            if (!SAFE_IDENTIFIER.matcher(index).matches()) {
                log.warn("Skipping unexpected index name {} on {}", index, table);
                continue;
            }
            jdbc.execute("ALTER TABLE `" + table + "` DROP INDEX `" + index + "`");
            log.info("Dropped legacy global unique index {} on {}({})", index, table, column);
        }
    }

    private void enforceNotNull(String table) {
        List<String> nullable = jdbc.queryForList("""
                SELECT IS_NULLABLE FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = 'event_id'
                """, String.class, table);
        if (nullable.isEmpty() || !"YES".equals(nullable.get(0))) return;
        if (count("SELECT COUNT(*) FROM `" + table + "` WHERE event_id IS NULL") > 0) {
            log.warn("Not enforcing NOT NULL on {}.event_id: rows without event exist", table);
            return;
        }
        jdbc.execute("ALTER TABLE `" + table + "` MODIFY event_id BIGINT NOT NULL");
        log.info("Enforced NOT NULL on {}.event_id", table);
    }

    /** Runs a data migration exactly once, tracked in app_migration (so later admin changes are never overridden). */
    private void applyOnce(String id, Runnable migration) {
        jdbc.execute("CREATE TABLE IF NOT EXISTS app_migration (id VARCHAR(100) PRIMARY KEY, applied_at DATETIME NOT NULL)");
        Long done = jdbc.queryForObject("SELECT COUNT(*) FROM app_migration WHERE id = ?", Long.class, id);
        if (done != null && done > 0) return;
        tx.executeWithoutResult(status -> {
            migration.run();
            jdbc.update("INSERT INTO app_migration (id, applied_at) VALUES (?, NOW())", id);
        });
        log.info("Applied data migration {}", id);
    }

    /** Existing users keep access to the event they were already using. */
    private void assignExistingUsersToActiveEvent() {
        int assigned = jdbc.update("INSERT IGNORE INTO user_events (user_id, event_id) "
                + "SELECT u.id, e.id FROM users u CROSS JOIN events e WHERE e.status = 'ACTIVE'");
        log.info("Assigned {} existing user(s) to the active event", assigned);
    }

    /**
     * Hibernate's schema update never widens an existing MySQL ENUM column, so seat_reservation.ticket_type stayed
     * ('FULL','HALF') and INVITATION reservations failed. Keep the column in sync with the TicketType enum.
     */
    private void syncTicketTypeEnum() {
        List<String> types = jdbc.queryForList("""
                SELECT COLUMN_TYPE FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'seat_reservation' AND COLUMN_NAME = 'ticket_type'
                """, String.class);
        if (types.isEmpty() || !types.get(0).toLowerCase().startsWith("enum(")) return;
        String current = types.get(0);
        java.util.List<String> missing = java.util.Arrays.stream(TicketType.values()).map(Enum::name)
                .filter(v -> !current.contains("'" + v + "'")).toList();
        if (missing.isEmpty()) return;
        String values = java.util.Arrays.stream(TicketType.values()).map(v -> "'" + v.name() + "'")
                .collect(java.util.stream.Collectors.joining(","));
        jdbc.execute("ALTER TABLE seat_reservation MODIFY COLUMN ticket_type ENUM(" + values + ") NULL");
        log.info("Added {} to seat_reservation.ticket_type", missing);
    }

    /** Several events may be ACTIVE at once: remove the old one-active-event marker column (and its unique index). */
    private void dropSingleActiveConstraint() {
        Long exists = jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'events' AND COLUMN_NAME = 'active_marker'", Long.class);
        if (exists == null || exists == 0) return;
        jdbc.execute("ALTER TABLE events DROP COLUMN active_marker");
        log.info("Dropped events.active_marker: multiple ACTIVE events are now allowed");
    }

    private long count(String sql) {
        Long n = jdbc.queryForObject(sql, Long.class);
        return n != null ? n : 0;
    }
}
