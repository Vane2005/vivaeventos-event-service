package co.edu.univalle.vivaeventoseventservice.application.usecase;

import co.edu.univalle.vivaeventoseventservice.domain.model.TicketType;
import co.edu.univalle.vivaeventoseventservice.domain.model.TicketTypeModel;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetTicketTypesUseCaseTest {

    @Mock
    private TicketTypeJpaRepository ticketTypeJpaRepository;

    @InjectMocks
    private GetTicketTypesUseCase getTicketTypesUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getById_WhenExists_ShouldReturnModel() {
        UUID id = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        
        EventEntity event = new EventEntity();
        event.setId(eventId);

        TicketTypeEntity entity = new TicketTypeEntity();
        entity.setId(id);
        entity.setEvent(event);
        entity.setType(TicketType.GENERAL);
        entity.setPrice(BigDecimal.valueOf(100));
        entity.setQuantityAvailable(50);

        when(ticketTypeJpaRepository.findById(id)).thenReturn(Optional.of(entity));

        TicketTypeModel result = getTicketTypesUseCase.getById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(eventId, result.getEventId());
        assertEquals(TicketType.GENERAL, result.getType());
        assertEquals(BigDecimal.valueOf(100), result.getPrice());
        assertEquals(50, result.getQuantityAvailable());
    }

    @Test
    void getById_WhenNotExists_ShouldThrowException() {
        UUID id = UUID.randomUUID();
        when(ticketTypeJpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> getTicketTypesUseCase.getById(id));
    }
}
