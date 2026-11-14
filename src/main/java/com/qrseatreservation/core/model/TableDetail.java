package com.qrseatreservation.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_table_detail_event_name", columnNames = {"event_id", "table_name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"seatDetails"})
public class TableDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning ticket campaign; mandatory (enforced in @PrePersist and NOT NULL after migration)
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    @Column(nullable = false)
    private String tableName;
    private String description;
    private Integer noOfSeats;
    @OneToMany(mappedBy = "tableDetail", cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    private List<SeatDetail> seatDetails;
    // Manual position on the floor layout editor canvas; null = not yet dragged, use auto grid layout
    private Integer posX;
    private Integer posY;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;


    @PrePersist
    protected void onCreate() {
        if (this.event == null) {
            throw new IllegalStateException("TableDetail must belong to an event");
        }
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
