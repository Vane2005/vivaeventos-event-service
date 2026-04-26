package co.edu.univalle.vivaeventoseventservice.infrastructure.web;

import co.edu.univalle.vivaeventoseventservice.application.dto.CreateEventRequest;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventJpaRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventJpaRepository eventJpaRepository;

    public EventController(EventJpaRepository eventJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
    }

    @PostMapping
    public ResponseEntity<EventEntity> createEvent(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateEventRequest request
    ) {
        EventEntity entity = new EventEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setEventDate(request.getEventDate());
        entity.setLocation(request.getLocation());
        entity.setCapacity(request.getCapacity());

        entity.setCreatedBy(userId);
        entity.setCreatedAt(Instant.now());

        EventEntity saved = eventJpaRepository.save(entity);
        return ResponseEntity.status(201).body(saved);
    }
}