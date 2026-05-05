package co.edu.univalle.vivaeventoseventservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import co.edu.univalle.vivaeventoseventservice.domain.model.EventStatus;
import java.util.List;

public interface EventJpaRepository extends JpaRepository<EventEntity, UUID>{
    List<EventEntity> findByStatus(EventStatus status);
    
}
