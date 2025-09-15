package com.amithfernando.qrseatreservation.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"seatDetails"})
public class TableDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String tableName;
    private String description;
    private Integer noOfSeats;
    @OneToMany(mappedBy = "tableDetail", cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    private List<SeatDetail> seatDetails;
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

    public String getTableNoWithStatus() {
        long availableSeats = seatDetails.stream().filter(seatDetail -> seatDetail.isAvailable()).count();
        return tableName + " - Available Seats: " +availableSeats+" / "+ noOfSeats;
    }

    public long getAvailableSeats() {
        return seatDetails.stream().filter(seatDetail -> seatDetail.isAvailable()).count();
    }

    public long getUnavailableSeats() {
        return seatDetails.stream().filter(seatDetail -> seatDetail.isUnavailable()).count();
    }

    public long getReservedSeats() {
        return seatDetails.stream().filter(seatDetail -> ( seatDetail.isReserved() || seatDetail.isCheckedIn())).count();
    }

}
