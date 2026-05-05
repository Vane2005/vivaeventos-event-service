package co.edu.univalle.vivaeventoseventservice.application.dto;

import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class EventDetailResponse {
    private UUID id;
    private String name;
    private String description;
    private OffsetDateTime eventDate;
    private String location;
    private Integer capacity;
    private String status;
    private List<TicketTypeResponse> ticketTypes;

    public static EventDetailResponse from(EventEntity e, List<TicketTypeResponse> ticketTypes) {
        EventDetailResponse dto = new EventDetailResponse();
        dto.id = e.getId();
        dto.name = e.getName();
        dto.description = e.getDescription();
        dto.eventDate = e.getEventDate();
        dto.location = e.getLocation();
        dto.capacity = e.getCapacity();
        dto.status = e.getStatus().name();
        dto.ticketTypes = ticketTypes;
        return dto;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public OffsetDateTime getEventDate() { return eventDate; }
    public String getLocation() { return location; }
    public Integer getCapacity() { return capacity; }
    public String getStatus() { return status; }
    public List<TicketTypeResponse> getTicketTypes() { return ticketTypes; }
}