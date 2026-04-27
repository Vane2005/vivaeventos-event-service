package co.edu.univalle.vivaeventoseventservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketTypeJpaRepository extends JpaRepository<TicketTypeEntity, UUID> {
    List<TicketTypeEntity> findByEvent_Id(UUID eventId);
}