package com.qrseatreservation.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning ticket campaign; mandatory (enforced in @PrePersist and NOT NULL after migration)
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    private String name;
    private String address;
    private String email;
    private String phone;
    private String description;

    @Column(name = "access_token", unique = true)
    private String accessToken;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;


    @PrePersist
    protected void onCreate() {
        if (this.event == null) {
            throw new IllegalStateException("SellerDetail must belong to an event");
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
