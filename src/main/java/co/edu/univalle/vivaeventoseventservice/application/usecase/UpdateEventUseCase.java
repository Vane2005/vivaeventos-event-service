package co.edu.univalle.vivaeventoseventservice.application.usecase;

import co.edu.univalle.vivaeventoseventservice.application.dto.UpdateEventRequest;
import co.edu.univalle.vivaeventoseventservice.domain.model.EventStatus;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventJpaRepository;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class UpdateEventUseCase {

    private final EventJpaRepository eventJpaRepository;
    private final TicketTypeJpaRepository ticketTypeJpaRepository;

    public UpdateEventUseCase(EventJpaRepository eventJpaRepository,
                              TicketTypeJpaRepository ticketTypeJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
        this.ticketTypeJpaRepository = ticketTypeJpaRepository;
    }

    @Transactional
    public EventEntity execute(UUID eventId, String requesterId, UpdateEventRequest request) {


        EventEntity event = eventJpaRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Evento no encontrado"));


        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No se puede modificar un evento cancelado");
        }


        if (!event.getCreatedBy().equals(requesterId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo el organizador que creó el evento puede modificarlo");
        }

        if (request.getCapacity() != null) {
            int totalTickets = ticketTypeJpaRepository.findByEvent_Id(eventId)
                    .stream()
                    .mapToInt(t -> t.getQuantityAvailable())
                    .sum();

            if (request.getCapacity() < totalTickets) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El nuevo aforo (" + request.getCapacity() + ") no puede ser menor "
                                + "que las boletas ya definidas (" + totalTickets + ")");
            }
            event.setCapacity(request.getCapacity());
        }


        if (request.getName() != null)        event.setName(request.getName());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getEventDate() != null)   event.setEventDate(request.getEventDate());
        if (request.getLocation() != null)    event.setLocation(request.getLocation());

        return eventJpaRepository.save(event);
    }
}