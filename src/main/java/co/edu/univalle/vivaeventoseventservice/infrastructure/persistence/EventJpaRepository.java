package co.edu.univalle.vivaeventoseventservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventJpaRepository extends JpaRepository<EventEntity, UUID>{
    
}
