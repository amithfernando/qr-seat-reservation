package com.amithfernando.qrseatreservation.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;

import com.amithfernando.qrseatreservation.core.enums.ReservationStatus;
import com.amithfernando.qrseatreservation.core.enums.TicketType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private SeatDetail seatDetail;
    private String ticketNo;
    @Enumerated(EnumType.STRING)
    private TicketType ticketType;
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    @Override
    public String toString() {
        return "SeatReservation{" +
                "id=" + id +
                ", ticketNo='" + ticketNo + '\'' +
                '}';
    }

    public SeatReservation(SeatDetail seatDetail) {
        this.seatDetail = seatDetail;
    }

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
}
