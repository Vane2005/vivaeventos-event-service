package co.edu.univalle.vivaeventoseventservice.application.usecase;

import co.edu.univalle.vivaeventoseventservice.domain.model.EventStatus;
import co.edu.univalle.vivaeventoseventservice.infrastructure.messaging.EventCancelledMessage;
import co.edu.univalle.vivaeventoseventservice.infrastructure.messaging.RabbitMQConfig;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventCancellationEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventCancellationJpaRepository;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventJpaRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;


@Service
public class CancelEventUseCase {

    private final EventJpaRepository eventJpaRepository;
    private final EventCancellationJpaRepository cancellationJpaRepository;
    private final RabbitTemplate rabbitTemplate;

    public CancelEventUseCase(EventJpaRepository eventJpaRepository,
                              EventCancellationJpaRepository cancellationJpaRepository,
                              RabbitTemplate rabbitTemplate) {
        this.eventJpaRepository = eventJpaRepository;
        this.cancellationJpaRepository = cancellationJpaRepository;
        this.rabbitTemplate = rabbitTemplate;
    }


    @Transactional
    public void execute(UUID eventId, String cancelledBy, String reason) {


        EventEntity event = eventJpaRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Evento no encontrado"));


        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden cancelar eventos en estado ACTIVE. Estado actual: "
                            + event.getStatus());
        }

        Instant now = Instant.now();


        event.setStatus(EventStatus.CANCELLED);
        event.setCancelledBy(cancelledBy);
        event.setCancelledAt(now);
        event.setCancellationReason(reason);
        eventJpaRepository.save(event);


        EventCancellationEntity audit = new EventCancellationEntity(
                eventId,
                event.getName(),
                cancelledBy,
                reason,
                now
        );
        cancellationJpaRepository.save(audit);


        EventCancelledMessage message = new EventCancelledMessage(
                eventId,
                event.getName(),
                cancelledBy,
                reason,
                now
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_EVENTO_CANCELADO,
                message
        );
    }
}