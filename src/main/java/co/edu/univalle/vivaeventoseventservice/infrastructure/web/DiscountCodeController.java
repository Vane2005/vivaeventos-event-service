package co.edu.univalle.vivaeventoseventservice.infrastructure.web;

import co.edu.univalle.vivaeventoseventservice.application.dto.CreateDiscountCodeRequest;
import co.edu.univalle.vivaeventoseventservice.application.dto.DiscountCodeResponse;
import co.edu.univalle.vivaeventoseventservice.application.usecase.ValidateDiscountCodeUseCase;
import co.edu.univalle.vivaeventoseventservice.domain.model.DiscountType;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.DiscountCodeEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.DiscountCodeJpaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/discount-codes")
public class DiscountCodeController {

    private final DiscountCodeJpaRepository discountCodeJpaRepository;

    private final ValidateDiscountCodeUseCase validateDiscountCodeUseCase;

    public DiscountCodeController(DiscountCodeJpaRepository discountCodeJpaRepository,
                                  ValidateDiscountCodeUseCase validateDiscountCodeUseCase) {
        this.discountCodeJpaRepository = discountCodeJpaRepository;
        this.validateDiscountCodeUseCase = validateDiscountCodeUseCase;
    }

    // Nuevo endpoint
    @PostMapping("/{code}/validate")
    public ResponseEntity<DiscountCodeResponse> validate(@PathVariable String code) {
        return ResponseEntity.ok(validateDiscountCodeUseCase.validate(code));
    }
    @PostMapping
    public ResponseEntity<DiscountCodeEntity> create(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateDiscountCodeRequest request
    ) {
        if (request.getEndsAt().isBefore(request.getStartsAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endsAt must be after startsAt");
        }

        if (request.getDiscountType() == DiscountType.PERCENTAGE) {
            if (request.getDiscountValue().compareTo(new java.math.BigDecimal("0.01")) < 0
                    || request.getDiscountValue().compareTo(new java.math.BigDecimal("100")) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Percentage discountValue must be between 0.01 and 100");
            }
        } else {
            if (request.getDiscountValue().compareTo(new java.math.BigDecimal("0.01")) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fixed discountValue must be >= 0.01");
            }
        }

        String normalizedCode = request.getCode().trim().toUpperCase();

        discountCodeJpaRepository.findByCode(normalizedCode).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Discount code already exists");
        });

        DiscountCodeEntity entity = new DiscountCodeEntity();
        entity.setCode(normalizedCode);
        entity.setDiscountType(request.getDiscountType());
        entity.setDiscountValue(request.getDiscountValue());
        entity.setStartsAt(request.getStartsAt());
        entity.setEndsAt(request.getEndsAt());
        entity.setUsageLimit(request.getUsageLimit());
        entity.setUsageCount(0);
        entity.setActive(true);

        Instant now = Instant.now();
        entity.setCreatedBy(userId);
        entity.setCreatedAt(now);
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(now);

        DiscountCodeEntity saved = discountCodeJpaRepository.save(entity);
        return ResponseEntity.status(201).body(saved);
    }

    @PatchMapping("/{code}/activate")
    public ResponseEntity<DiscountCodeEntity> activate(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String code
    ) {
        DiscountCodeEntity entity = discountCodeJpaRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Discount code not found"));

        entity.setActive(true);
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(Instant.now());

        return ResponseEntity.ok(discountCodeJpaRepository.save(entity));
    }

    @PatchMapping("/{code}/deactivate")
    public ResponseEntity<DiscountCodeEntity> deactivate(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String code
    ) {
        DiscountCodeEntity entity = discountCodeJpaRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Discount code not found"));

        entity.setActive(false);
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(Instant.now());

        return ResponseEntity.ok(discountCodeJpaRepository.save(entity));
    }
}