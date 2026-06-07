package co.edu.univalle.vivaeventoseventservice.application.usecase;

import co.edu.univalle.vivaeventoseventservice.domain.model.EventStatus;
import co.edu.univalle.vivaeventoseventservice.infrastructure.messaging.EventCancelledMessage;
import co.edu.univalle.vivaeventoseventservice.infrastructure.messaging.RabbitMQConfig;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventCancellationEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventCancellationJpaRepository;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelEventUseCaseTest {

    @Mock
    private EventJpaRepository eventJpaRepository;

    @Mock
    private EventCancellationJpaRepository cancellationJpaRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CancelEventUseCase useCase;

    @Test
    void shouldThrowNotFoundWhenEventDoesNotExist() {

        UUID eventId = UUID.randomUUID();

        when(eventJpaRepository.findById(eventId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> useCase.execute(eventId, "admin", "motivo")
                );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(eventJpaRepository).findById(eventId);
        verifyNoMoreInteractions(cancellationJpaRepository);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void shouldThrowConflictWhenEventIsNotActive() {

        UUID eventId = UUID.randomUUID();

        EventEntity event = new EventEntity();
        event.setId(eventId);
        event.setName("Rock Fest");
        event.setStatus(EventStatus.CANCELLED);

        when(eventJpaRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> useCase.execute(eventId, "admin", "motivo")
                );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(eventJpaRepository).findById(eventId);
        verify(eventJpaRepository, never()).save(any());
        verifyNoInteractions(cancellationJpaRepository);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void shouldCancelEventAndPublishMessage() {

        UUID eventId = UUID.randomUUID();

        EventEntity event = new EventEntity();
        event.setId(eventId);
        event.setName("Rock Fest");
        event.setStatus(EventStatus.ACTIVE);

        when(eventJpaRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        useCase.execute(eventId, "admin", "Mal clima");

        assertEquals(EventStatus.CANCELLED, event.getStatus());
        assertEquals("admin", event.getCancelledBy());
        assertEquals("Mal clima", event.getCancellationReason());
        assertNotNull(event.getCancelledAt());

        verify(eventJpaRepository).save(event);

        ArgumentCaptor<EventCancellationEntity> auditCaptor =
                ArgumentCaptor.forClass(EventCancellationEntity.class);

        verify(cancellationJpaRepository).save(auditCaptor.capture());

        EventCancellationEntity audit = auditCaptor.getValue();

        assertEquals(eventId, audit.getEventId());
        assertEquals("Rock Fest", audit.getEventName());
        assertEquals("admin", audit.getCancelledBy());
        assertEquals("Mal clima", audit.getReason());

        ArgumentCaptor<EventCancelledMessage> messageCaptor =
                ArgumentCaptor.forClass(EventCancelledMessage.class);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.ROUTING_KEY_EVENTO_CANCELADO),
                messageCaptor.capture()
        );

        EventCancelledMessage message = messageCaptor.getValue();

        assertEquals(eventId, message.getEventId());
        assertEquals("Rock Fest", message.getEventName());
        assertEquals("admin", message.getCancelledBy());
        assertEquals("Mal clima", message.getReason());
    }

    @Test
    void shouldCancelEventEvenWhenRabbitMqFails() {

        UUID eventId = UUID.randomUUID();

        EventEntity event = new EventEntity();
        event.setId(eventId);
        event.setName("Rock Fest");
        event.setStatus(EventStatus.ACTIVE);

        when(eventJpaRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        doThrow(new RuntimeException("RabbitMQ down"))
                .when(rabbitTemplate)
                .convertAndSend(
                        anyString(),
                        anyString(),
                        any(EventCancelledMessage.class)
                );

        assertDoesNotThrow(
                () -> useCase.execute(eventId, "admin", "Mal clima")
        );

        verify(eventJpaRepository).save(event);
        verify(cancellationJpaRepository)
                .save(any(EventCancellationEntity.class));

        verify(rabbitTemplate)
                .convertAndSend(
                        anyString(),
                        anyString(),
                        any(EventCancelledMessage.class)
                );
    }
}