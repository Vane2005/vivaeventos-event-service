package co.edu.univalle.vivaeventoseventservice.application.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;


public class UpdateEventRequest {

    @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
    private String name;

    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String description;

    @Future(message = "La fecha del evento debe ser futura")
    private OffsetDateTime eventDate;

    @Size(min = 3, max = 255, message = "La ubicación debe tener entre 3 y 255 caracteres")
    private String location;

    @Positive(message = "El aforo debe ser mayor a 0")
    private Integer capacity;

    public UpdateEventRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public OffsetDateTime getEventDate() { return eventDate; }
    public void setEventDate(OffsetDateTime eventDate) { this.eventDate = eventDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}