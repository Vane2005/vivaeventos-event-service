package co.edu.univalle.vivaeventoseventservice.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "event_cancellations")
public class EventCancellationEntity {

    @Id
    @GeneratedValue
    private UUID id;


    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "event_name", nullable = false, length = 200)
    private String eventName;

    @Column(name = "cancelled_by", nullable = false, length = 100)
    private String cancelledBy;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "cancelled_at", nullable = false)
    private Instant cancelledAt;

    public EventCancellationEntity() {}

    public EventCancellationEntity(UUID eventId, String eventName,
                                   String cancelledBy, String reason,
                                   Instant cancelledAt) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.cancelledBy = cancelledBy;
        this.reason = reason;
        this.cancelledAt = cancelledAt;
    }

    public UUID getId() { return id; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
}