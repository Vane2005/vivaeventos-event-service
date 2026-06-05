package co.edu.univalle.vivaeventoseventservice.application.usecase;

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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReserveStockUseCaseTest {

    @Mock
    private TicketTypeJpaRepository ticketTypeJpaRepository;

    @InjectMocks
    private ReserveStockUseCase reserveStockUseCase;

    private UUID ticketTypeId;
    private TicketTypeEntity ticketTypeEntity;

    @BeforeEach
    void setUp() {
        ticketTypeId = UUID.randomUUID();

        ticketTypeEntity = new TicketTypeEntity();
        ticketTypeEntity.setId(ticketTypeId);
        ticketTypeEntity.setQuantityAvailable(10);
    }

    @Test
    @DisplayName("Debe reservar stock correctamente cuando hay suficiente disponible")
    void shouldReserveStockWhenAvailable() {
        when(ticketTypeJpaRepository.findById(ticketTypeId))
                .thenReturn(Optional.of(ticketTypeEntity));
        when(ticketTypeJpaRepository.save(any())).thenReturn(ticketTypeEntity);

        reserveStockUseCase.execute(ticketTypeId, 3);

        assertThat(ticketTypeEntity.getQuantityAvailable()).isEqualTo(7);
        verify(ticketTypeJpaRepository).save(ticketTypeEntity);
    }

    @Test
    @DisplayName("Debe reservar stock cuando la cantidad es exactamente igual al disponible")
    void shouldReserveStockWhenQuantityMatchesAvailable() {
        when(ticketTypeJpaRepository.findById(ticketTypeId))
                .thenReturn(Optional.of(ticketTypeEntity));
        when(ticketTypeJpaRepository.save(any())).thenReturn(ticketTypeEntity);

        reserveStockUseCase.execute(ticketTypeId, 10);

        assertThat(ticketTypeEntity.getQuantityAvailable()).isEqualTo(0);
        verify(ticketTypeJpaRepository).save(ticketTypeEntity);
    }

    @Test
    @DisplayName("Debe lanzar 409 cuando el stock es insuficiente")
    void shouldThrow409WhenStockIsInsufficient() {
        when(ticketTypeJpaRepository.findById(ticketTypeId))
                .thenReturn(Optional.of(ticketTypeEntity));

        assertThatThrownBy(() -> reserveStockUseCase.execute(ticketTypeId, 11))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(ticketTypeJpaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar 404 cuando el tipo de boleta no existe")
    void shouldThrow404WhenTicketTypeNotFound() {
        when(ticketTypeJpaRepository.findById(ticketTypeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reserveStockUseCase.execute(ticketTypeId, 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tipo de boleta no encontrado");

        verify(ticketTypeJpaRepository, never()).save(any());
    }
}