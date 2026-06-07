package co.edu.univalle.vivaeventoseventservice.infrastructure.web;

import co.edu.univalle.vivaeventoseventservice.application.dto.*;
import co.edu.univalle.vivaeventoseventservice.application.usecase.CancelEventUseCase;
import co.edu.univalle.vivaeventoseventservice.application.usecase.GetTicketTypesUseCase;
import co.edu.univalle.vivaeventoseventservice.application.usecase.ReserveStockUseCase;
import co.edu.univalle.vivaeventoseventservice.application.usecase.UpdateEventUseCase;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventJpaRepository;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeJpaRepository;
import co.edu.univalle.vivaeventoseventservice.domain.model.EventStatus;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventJpaRepository eventJpaRepository;
    private final TicketTypeJpaRepository ticketTypeJpaRepository;
    private final GetTicketTypesUseCase getTicketTypesUseCase;
    private final ReserveStockUseCase reserveStockUseCase;
    private final CancelEventUseCase cancelEventUseCase;
    private final UpdateEventUseCase updateEventUseCase;

    public EventController(EventJpaRepository eventJpaRepository,
                           TicketTypeJpaRepository ticketTypeJpaRepository,
                           GetTicketTypesUseCase getTicketTypesUseCase,
                           ReserveStockUseCase reserveStockUseCase,
                           CancelEventUseCase cancelEventUseCase,
                           UpdateEventUseCase updateEventUseCase) {
        this.eventJpaRepository = eventJpaRepository;
        this.ticketTypeJpaRepository = ticketTypeJpaRepository;
        this.getTicketTypesUseCase = getTicketTypesUseCase;
        this.reserveStockUseCase = reserveStockUseCase;
        this.cancelEventUseCase = cancelEventUseCase;
        this.updateEventUseCase = updateEventUseCase;
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
        entity.setStatus(EventStatus.ACTIVE);
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

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No se pueden agregar boletas a un evento cancelado");
        }
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


    @GetMapping("/{eventId}/ticket-types")
    public ResponseEntity<List<TicketTypeResponse>> getTicketTypes(
            @PathVariable UUID eventId) {
        List<TicketTypeResponse> response = getTicketTypesUseCase
                .getByEvent(eventId)
                .stream()
                .map(TicketTypeResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/ticket-types/{ticketTypeId}")
    public ResponseEntity<TicketTypeResponse> getTicketType(
            @PathVariable UUID ticketTypeId) {
        return ResponseEntity.ok(
                TicketTypeResponse.from(getTicketTypesUseCase.getById(ticketTypeId)));
    }


    @PutMapping("/ticket-types/{ticketTypeId}/reserve")
    public ResponseEntity<Void> reserveStock(
            @PathVariable UUID ticketTypeId,
            @RequestParam int quantity) {
        reserveStockUseCase.execute(ticketTypeId, quantity);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<EventSummaryResponse>> getActiveEvents() {
    List<EventSummaryResponse> events = eventJpaRepository
            .findByStatus(EventStatus.ACTIVE)
            .stream()
            .map(EventSummaryResponse::from)
            .toList();
    return ResponseEntity.ok(events);
   }

   @GetMapping("/{eventId}")
   public ResponseEntity<EventDetailResponse> getEventDetail(
        @PathVariable UUID eventId) {

    EventEntity event = eventJpaRepository.findById(eventId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Evento no encontrado"));

    if (event.getStatus() == EventStatus.CANCELLED) {
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Evento no disponible");
    }

    List<TicketTypeResponse> ticketTypes = getTicketTypesUseCase
            .getByEvent(eventId)
            .stream()
            .map(TicketTypeResponse::from)
            .toList();

    return ResponseEntity.ok(EventDetailResponse.from(event, ticketTypes));
   }
    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<Void> cancelEvent(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID eventId,
            @Valid @RequestBody CancelEventRequest request
    ) {
        cancelEventUseCase.execute(eventId, userId, request.getReason());
        return ResponseEntity.noContent().build(); // 204 No Content
    }
    @PatchMapping("/{eventId}")
    public ResponseEntity<EventEntity> updateEvent(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID eventId,
            @Valid @RequestBody UpdateEventRequest request
    ) {
        EventEntity updated = updateEventUseCase.execute(eventId, userId, request);
        return ResponseEntity.ok(updated);
    }

}