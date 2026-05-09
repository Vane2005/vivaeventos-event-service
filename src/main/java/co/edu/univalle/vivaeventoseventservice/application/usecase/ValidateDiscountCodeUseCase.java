package co.edu.univalle.vivaeventoseventservice.application.usecase;

import co.edu.univalle.vivaeventoseventservice.application.dto.DiscountCodeResponse;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.DiscountCodeEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.DiscountCodeJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.OffsetDateTime;

@Service
public class ValidateDiscountCodeUseCase {

    private final DiscountCodeJpaRepository discountCodeJpaRepository;

    public ValidateDiscountCodeUseCase(DiscountCodeJpaRepository discountCodeJpaRepository) {
        this.discountCodeJpaRepository = discountCodeJpaRepository;
    }

    public DiscountCodeResponse validate(String code) {
        DiscountCodeEntity entity = discountCodeJpaRepository
                .findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Código de descuento no encontrado"));

        // Validar que esté activo
        if (!entity.getActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código de descuento inactivo");
        }

        // Validar vigencia
        OffsetDateTime now = OffsetDateTime.now();
        if (now.isBefore(entity.getStartsAt()) || now.isAfter(entity.getEndsAt())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código de descuento fuera de vigencia");
        }

        // Validar límite de uso
        if (entity.getUsageCount() >= entity.getUsageLimit()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código de descuento agotado");
        }

        // Incrementar uso
        entity.setUsageCount(entity.getUsageCount() + 1);
        discountCodeJpaRepository.save(entity);

        return DiscountCodeResponse.from(entity);
    }
}