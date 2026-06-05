package co.edu.univalle.vivaeventoseventservice.application.usecase;

import co.edu.univalle.vivaeventoseventservice.domain.model.TicketType;
import co.edu.univalle.vivaeventoseventservice.domain.model.TicketTypeModel;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTicketTypesUseCaseTest {

    @Mock
    private TicketTypeJpaRepository ticketTypeJpaRepository;

    @InjectMocks
    private GetTicketTypesUseCase getTicketTypesUseCase;

    private UUID eventId;
    private UUID ticketTypeId;
    private TicketTypeEntity entity;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        ticketTypeId = UUID.randomUUID();

        EventEntity event = new EventEntity();
        event.setId(eventId);

        entity = new TicketTypeEntity();
        entity.setId(ticketTypeId);
        entity.setEvent(event);
        entity.setType(TicketType.VIP);
        entity.setPrice(new BigDecimal("80000"));
        entity.setQuantityAvailable(50);
    }

    // ─── getByEvent ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByEvent debe retornar lista de TicketTypeModel para el evento dado")
    void getByEvent_returnsList() {
        when(ticketTypeJpaRepository.findByEvent_Id(eventId)).thenReturn(List.of(entity));

        List<TicketTypeModel> result = getTicketTypesUseCase.getByEvent(eventId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(ticketTypeId);
        assertThat(result.get(0).getEventId()).isEqualTo(eventId);
        assertThat(result.get(0).getType()).isEqualTo(TicketType.VIP);
        assertThat(result.get(0).getPrice()).isEqualByComparingTo(new BigDecimal("80000"));
        assertThat(result.get(0).getQuantityAvailable()).isEqualTo(50);
    }

    @Test
    @DisplayName("getByEvent debe retornar lista vacía cuando no hay tipos para el evento")
    void getByEvent_returnsEmptyList() {
        when(ticketTypeJpaRepository.findByEvent_Id(eventId)).thenReturn(List.of());

        List<TicketTypeModel> result = getTicketTypesUseCase.getByEvent(eventId);

        assertThat(result).isEmpty();
    }

    // ─── getById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById debe retornar el TicketTypeModel cuando existe")
    void getById_returnsModel() {
        when(ticketTypeJpaRepository.findById(ticketTypeId)).thenReturn(Optional.of(entity));

        TicketTypeModel result = getTicketTypesUseCase.getById(ticketTypeId);

        assertThat(result.getId()).isEqualTo(ticketTypeId);
        assertThat(result.getType()).isEqualTo(TicketType.VIP);
        assertThat(result.getQuantityAvailable()).isEqualTo(50);
    }

    @Test
    @DisplayName("getById debe lanzar 404 cuando el tipo de boleta no existe")
    void getById_throwsNotFound() {
        when(ticketTypeJpaRepository.findById(ticketTypeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getTicketTypesUseCase.getById(ticketTypeId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tipo de boleta no encontrado");
    }
}