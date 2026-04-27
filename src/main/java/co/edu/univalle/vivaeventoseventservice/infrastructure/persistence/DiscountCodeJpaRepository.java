package co.edu.univalle.vivaeventoseventservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DiscountCodeJpaRepository extends JpaRepository<DiscountCodeEntity, UUID> {
    Optional<DiscountCodeEntity> findByCode(String code);
}