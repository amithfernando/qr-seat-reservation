package com.qrseatreservation.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_category", uniqueConstraints = {
    @UniqueConstraint(name = "uk_ticket_category_event_name", columnNames = {"event_id", "name"})
}, indexes = {
    @Index(name = "idx_category_name", columnList = "name"),
    @Index(name = "idx_category_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning ticket campaign; mandatory (enforced in @PrePersist and NOT NULL after migration)
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (this.event == null) {
            throw new IllegalStateException("TicketCategory must belong to an event");
        }
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return name + " - €" + String.format("%.2f", price);
    }
}
