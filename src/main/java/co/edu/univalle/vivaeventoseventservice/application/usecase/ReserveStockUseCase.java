package co.edu.univalle.vivaeventoseventservice.application.usecase;
// application/usecase/ReserveStockUseCase.java

import co.edu.univalle.vivaeventoseventservice.domain.model.TicketType;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeJpaRepository;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ReserveStockUseCase {

    private final TicketTypeJpaRepository ticketTypeJpaRepository;

    public ReserveStockUseCase(TicketTypeJpaRepository ticketTypeJpaRepository) {
        this.ticketTypeJpaRepository = ticketTypeJpaRepository;
    }

    @Transactional
    public void release(UUID ticketTypeId, int quantity) {
        TicketTypeEntity entity = ticketTypeJpaRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tipo de boleta no encontrado"));
        entity.setQuantityAvailable(entity.getQuantityAvailable() + quantity);
        ticketTypeJpaRepository.save(entity);
    }

    @Transactional
    public void execute(UUID ticketTypeId, int quantity) {
        TicketTypeEntity entity = ticketTypeJpaRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tipo de boleta no encontrado"));

        if (entity.getQuantityAvailable() < quantity) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Stock insuficiente. Disponible: " + entity.getQuantityAvailable());
        }

        entity.setQuantityAvailable(entity.getQuantityAvailable() - quantity);
        ticketTypeJpaRepository.save(entity);
    }


}