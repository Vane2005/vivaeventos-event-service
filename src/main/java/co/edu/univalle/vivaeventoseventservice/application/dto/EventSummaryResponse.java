package co.edu.univalle.vivaeventoseventservice.application.dto;

import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventEntity;
import java.time.OffsetDateTime;
import java.util.UUID;

public class EventSummaryResponse {
    private UUID id;
    private String name;
    private OffsetDateTime eventDate;
    private String location;

    public static EventSummaryResponse from(EventEntity e) {
        EventSummaryResponse dto = new EventSummaryResponse();
        dto.id = e.getId();
        dto.name = e.getName();
        dto.eventDate = e.getEventDate();
        dto.location = e.getLocation();
        return dto;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public OffsetDateTime getEventDate() { return eventDate; }
    public String getLocation() { return location; }
}