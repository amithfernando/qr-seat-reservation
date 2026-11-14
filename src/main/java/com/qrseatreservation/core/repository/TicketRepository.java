package com.qrseatreservation.core.repository;

import com.qrseatreservation.core.enums.TicketStatus;
import com.qrseatreservation.core.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Global check across ALL events: ticket numbers are never reused so an old QR code
     * cannot become valid at a later event. Use only for uniqueness checks.
     */
    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.ticketNo = :ticketNo")
    boolean existsAnywhere(@Param("ticketNo") String ticketNo);

    /** Ticket info (no BLOB) within one event. */
    @Query("SELECT t.id as id, t.ticketNo as ticketNo, t.status as status FROM Ticket t WHERE t.event.id = :eventId AND t.ticketNo = :ticketNo")
    Optional<TicketInfo> findTicketInfoByTicketNo(@Param("eventId") Long eventId, @Param("ticketNo") String ticketNo);

    /** First available ticket of the event, row-locked to prevent two reservations getting the same ticket. */
    @Query(value = "SELECT * FROM ticket WHERE event_id = :eventId AND status = :status ORDER BY id ASC LIMIT 1 FOR UPDATE", nativeQuery = true)
    Ticket findFirstAvailableTicketForUpdate(@Param("eventId") Long eventId, @Param("status") String status);

    /** USED -> AVAILABLE for the given ticket numbers of one event (other statuses and events are untouched). */
    @Modifying
    @Query("UPDATE Ticket t SET t.status = com.qrseatreservation.core.enums.TicketStatus.AVAILABLE, t.updatedAt = :now " +
           "WHERE t.event.id = :eventId AND t.status = com.qrseatreservation.core.enums.TicketStatus.USED AND t.ticketNo IN :ticketNos")
    int releaseUsed(@Param("eventId") Long eventId, @Param("ticketNos") java.util.Collection<String> ticketNos,
                    @Param("now") java.time.LocalDateTime now);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.status = :status")
    long countByStatus(@Param("eventId") Long eventId, @Param("status") TicketStatus status);

    long countByEventId(Long eventId);

    Page<Ticket> findByEventId(Long eventId, Pageable pageable);

    @Query("SELECT t.id as id, t.ticketNo as ticketNo, t.status as status FROM Ticket t WHERE t.event.id = :eventId AND t.status = :status")
    List<TicketInfo> findTicketInfoByStatus(@Param("eventId") Long eventId, @Param("status") TicketStatus status);

    /** Projection interface for ticket info without BLOB */
    interface TicketInfo {
        Long getId();
        String getTicketNo();
        TicketStatus getStatus();
    }
}
