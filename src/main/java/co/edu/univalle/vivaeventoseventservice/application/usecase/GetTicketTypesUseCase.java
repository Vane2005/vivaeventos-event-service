package co.edu.univalle.vivaeventoseventservice.application.usecase;


import co.edu.univalle.vivaeventoseventservice.domain.model.TicketTypeModel;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class GetTicketTypesUseCase {

    private final TicketTypeJpaRepository ticketTypeJpaRepository;

    public GetTicketTypesUseCase(TicketTypeJpaRepository ticketTypeJpaRepository) {
        this.ticketTypeJpaRepository = ticketTypeJpaRepository;
    }

    // Todos los tipos de un evento
    public List<TicketTypeModel> getByEvent(UUID eventId) {
        return ticketTypeJpaRepository.findByEvent_Id(eventId)
                .stream()
                .map(this::toModel)
                .toList();
    }

    // Un tipo específico (para que order-service valide precio y stock)
    public TicketTypeModel getById(UUID ticketTypeId) {
        return ticketTypeJpaRepository.findById(ticketTypeId)
                .map(this::toModel)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tipo de boleta no encontrado"));
    }

    private TicketTypeModel toModel(TicketTypeEntity e) {
        return new TicketTypeModel(
                e.getId(),
                e.getEvent().getId(),
                e.getType(),
                e.getPrice(),
                e.getQuantityAvailable()
        );
    }
}