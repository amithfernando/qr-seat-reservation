package com.qrseatreservation.core.model;

import com.qrseatreservation.core.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A ticket campaign. All event-specific data (settings, tickets, tables, sellers, ...) references one Event.
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String venue;

    private LocalDate eventDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status;

    private LocalDateTime activatedAt;
    private LocalDateTime archivedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == EventStatus.ACTIVE;
    }

    public boolean isArchived() {
        return status == EventStatus.ARCHIVED;
    }

    public String getDisplayName() {
        return name + " [" + status + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Event.class.hashCode();
    }
}
