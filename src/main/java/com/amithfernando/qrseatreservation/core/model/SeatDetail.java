package com.amithfernando.qrseatreservation.core.model;

import com.amithfernando.qrseatreservation.core.enums.SeatStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"tableDetail"})
public class SeatDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String seatNo;
    @Enumerated(jakarta.persistence.EnumType.STRING)
    private SeatStatus seatStatus;
    @ManyToOne
    private TableDetail tableDetail;
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

    public String getNoWithStatus() {
        return seatNo + " - " + seatStatus;
    }

    public boolean isReserved() {
        return seatStatus == SeatStatus.RESERVED;
    }

    public boolean isAvailable() {
        return seatStatus == SeatStatus.AVAILABLE;
    }

    public boolean isUnavailable() {
        return seatStatus == SeatStatus.UNAVAILABLE;
    }

    public boolean isCheckedIn() {
        return seatStatus == SeatStatus.CHECKED_IN;
    }
}
