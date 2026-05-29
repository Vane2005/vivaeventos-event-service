package co.edu.univalle.vivaeventoseventservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventCancellationJpaRepository
        extends JpaRepository<EventCancellationEntity, UUID> {

    List<EventCancellationEntity> findByEventId(UUID eventId);
}