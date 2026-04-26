package co.edu.univalle.vivaeventoseventservice.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public class CreateEventRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private OffsetDateTime eventDate;

    @NotBlank
    private String location;

    @NotNull
    @Min(1)
    private Integer capacity;

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