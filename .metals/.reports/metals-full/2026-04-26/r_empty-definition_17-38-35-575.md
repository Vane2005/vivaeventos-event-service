error id: file:///D:/codigo/desarrollo%203/Proyecto%20del%20curso/vivaeventos-event-service/src/main/java/co/edu/univalle/vivaeventoseventservice/infrastructure/web/EventController.java:_empty_/RestController#
file:///D:/codigo/desarrollo%203/Proyecto%20del%20curso/vivaeventos-event-service/src/main/java/co/edu/univalle/vivaeventoseventservice/infrastructure/web/EventController.java
empty definition using pc, found symbol in pc: _empty_/RestController#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1035
uri: file:///D:/codigo/desarrollo%203/Proyecto%20del%20curso/vivaeventos-event-service/src/main/java/co/edu/univalle/vivaeventoseventservice/infrastructure/web/EventController.java
text:
```scala
package co.edu.univalle.vivaeventoseventservice.infrastructure.web;

import co.edu.univalle.vivaeventoseventservice.application.dto.CreateEventRequest;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventJpaRepository;
import co.edu.univalle.vivaeventoseventservice.application.dto.DefineTicketTypesRequest;
import co.edu.univalle.vivaeventoseventservice.application.dto.TicketTypeRequest;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeJpaRepository;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestCo@@ntroller
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventJpaRepository eventJpaRepository;
    private final TicketTypeJpaRepository ticketTypeJpaRepository;

    public EventController(EventJpaRepository eventJpaRepository, TicketTypeJpaRepository ticketTypeJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
        this.ticketTypeJpaRepository = ticketTypeJpaRepository;
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

    @PostMapping("/{eventId}/ticket-types")
    public ResponseEntity<?> defineTicketTypes(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID eventId,
            @Valid @RequestBody DefineTicketTypesRequest request
    ) {
        EventEntity event = eventJpaRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        int capacity = event.getCapacity();

        int totalExisting = ticketTypeJpaRepository.findByEvent_Id(eventId).stream()
                .mapToInt(TicketTypeEntity::getQuantityAvailable)
                .sum();

        int totalNew = request.getTicketTypes().stream()
                .mapToInt(TicketTypeRequest::getQuantityAvailable)
                .sum();

        if (totalExisting + totalNew > capacity) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad total de boletas supera el aforo del evento (" + capacity + ")"
            );
        }

        List<TicketTypeEntity> entities = request.getTicketTypes().stream().map(t -> {
            TicketTypeEntity e = new TicketTypeEntity();
            e.setEvent(event);
            e.setType(t.getType());
            e.setPrice(t.getPrice());
            e.setQuantityAvailable(t.getQuantityAvailable());
            return e;
        }).toList();

        List<TicketTypeEntity> saved = ticketTypeJpaRepository.saveAll(entities);
        return ResponseEntity.status(201).body(saved);
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/RestController#