package com.qrseatreservation.core.model;

import com.qrseatreservation.core.enums.ZoneType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "venue_zone")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning ticket campaign; mandatory (enforced in @PrePersist and NOT NULL after migration)
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "zone_type")
    private ZoneType zoneType;

    private Integer posx;
    private Integer posy;
    private Integer width;
    private Integer height;
    private Integer rotation;
    private String color;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (this.event == null) {
            throw new IllegalStateException("VenueZone must belong to an event");
        }
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
