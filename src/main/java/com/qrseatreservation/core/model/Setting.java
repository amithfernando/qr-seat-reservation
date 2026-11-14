package com.qrseatreservation.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_setting_event", columnNames = "event_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning ticket campaign; mandatory (enforced in @PrePersist and NOT NULL after migration)
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    private String eventName;
    private String venue;
    //layout
    private  int tableSize;
    private  int seatSize;
    private  int noOfColumns;
    //qr ticket
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] baseImage;
    private  int fontSize;
    private  int qrSize;  // QR code size (scale factor)
    private  int qrBorder; // QR quiet zone width in modules
    private  int qrX;
    private  int qrY;
    private  int textX;
    private  int textY;
    // separate label for table/seat info drawn on ticket
    private Integer tableSeatFontSize;
    private Integer tableSeatX;
    private Integer tableSeatY;
    // text colors (hex, e.g. "#000000"); null = default black
    private String ticketNoColor;
    private String tableSeatColor;
    //tickets
    private String ticketPrefix;
    private Integer noOfDigits;
    private Integer maxNoOfTickets;
    // saved sample values for the layout editor preview
    private String sampleTicketNo;
    private String sampleTable;
    private String sampleSeat;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;


    @PrePersist
    protected void onCreate() {
        if (this.event == null) {
            throw new IllegalStateException("Setting must belong to an event");
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
