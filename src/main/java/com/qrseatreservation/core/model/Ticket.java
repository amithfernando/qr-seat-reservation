package com.qrseatreservation.core.model;

import com.qrseatreservation.core.enums.TicketStatus;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Blob;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket", indexes = {
    @Index(name = "idx_ticket_no", columnList = "ticketNo"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning ticket campaign; mandatory (enforced in @PrePersist and NOT NULL after migration)
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    // Globally unique across ALL events so a QR code from one event can never be valid at another
    @Column(unique = true, nullable = false)
    private String ticketNo;
    @Enumerated(jakarta.persistence.EnumType.STRING)
    private TicketStatus status;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] data;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;


    @PrePersist
    protected void onCreate() {
        if (this.event == null) {
            throw new IllegalStateException("Ticket must belong to an event");
        }
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public Ticket(String ticketNo, TicketStatus status,byte[] data) {
        this.ticketNo = ticketNo;
        this.status = status;
        this.data = data;
    }
}
