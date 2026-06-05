package co.edu.univalle.vivaeventoseventservice.application.usecase;

import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.DiscountCodeEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.DiscountCodeJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidateDiscountCodeUseCaseTest {

    @Mock
    private DiscountCodeJpaRepository discountCodeJpaRepository;

    @InjectMocks
    private ValidateDiscountCodeUseCase validateDiscountCodeUseCase;

    private DiscountCodeEntity validEntity;

    @BeforeEach
    void setUp() {
        validEntity = new DiscountCodeEntity();
        validEntity.setId(UUID.randomUUID());
        validEntity.setCode("PROMO10");
        validEntity.setActive(true);
        validEntity.setStartsAt(OffsetDateTime.now().minusDays(1));
        validEntity.setEndsAt(OffsetDateTime.now().plusDays(1));
        validEntity.setUsageCount(0);
        validEntity.setUsageLimit(100);
        validEntity.setDiscountValue(new BigDecimal("10"));
    }

    @Test
    @DisplayName("Debe validar y retornar el código cuando es válido")
    void shouldValidateAndReturnCodeWhenValid() {
        when(discountCodeJpaRepository.findByCode("PROMO10"))
                .thenReturn(Optional.of(validEntity));
        when(discountCodeJpaRepository.save(any())).thenReturn(validEntity);

        var result = validateDiscountCodeUseCase.validate("PROMO10");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("PROMO10");
        verify(discountCodeJpaRepository).save(validEntity);
    }

    @Test
    @DisplayName("Debe normalizar el código a mayúsculas y sin espacios")
    void shouldNormalizeCodeToUpperCase() {
        when(discountCodeJpaRepository.findByCode("PROMO10"))
                .thenReturn(Optional.of(validEntity));
        when(discountCodeJpaRepository.save(any())).thenReturn(validEntity);

        validateDiscountCodeUseCase.validate("  promo10  ");

        verify(discountCodeJpaRepository).findByCode("PROMO10");
    }

    @Test
    @DisplayName("Debe incrementar el contador de uso al validar")
    void shouldIncrementUsageCountWhenValidated() {
        validEntity.setUsageCount(5);
        when(discountCodeJpaRepository.findByCode("PROMO10"))
                .thenReturn(Optional.of(validEntity));
        when(discountCodeJpaRepository.save(any())).thenReturn(validEntity);

        validateDiscountCodeUseCase.validate("PROMO10");

        assertThat(validEntity.getUsageCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("Debe lanzar 404 cuando el código no existe")
    void shouldThrow404WhenCodeNotFound() {
        when(discountCodeJpaRepository.findByCode("NOEXISTE"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> validateDiscountCodeUseCase.validate("NOEXISTE"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Código de descuento no encontrado");

        verify(discountCodeJpaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar 409 cuando el código está inactivo")
    void shouldThrow409WhenCodeIsInactive() {
        validEntity.setActive(false);
        when(discountCodeJpaRepository.findByCode("PROMO10"))
                .thenReturn(Optional.of(validEntity));

        assertThatThrownBy(() -> validateDiscountCodeUseCase.validate("PROMO10"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("inactivo");

        verify(discountCodeJpaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar 409 cuando el código no ha comenzado su vigencia")
    void shouldThrow409WhenCodeNotStartedYet() {
        validEntity.setStartsAt(OffsetDateTime.now().plusDays(1));
        when(discountCodeJpaRepository.findByCode("PROMO10"))
                .thenReturn(Optional.of(validEntity));

        assertThatThrownBy(() -> validateDiscountCodeUseCase.validate("PROMO10"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("fuera de vigencia");

        verify(discountCodeJpaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar 409 cuando el código ha expirado")
    void shouldThrow409WhenCodeHasExpired() {
        validEntity.setEndsAt(OffsetDateTime.now().minusDays(1));
        when(discountCodeJpaRepository.findByCode("PROMO10"))
                .thenReturn(Optional.of(validEntity));

        assertThatThrownBy(() -> validateDiscountCodeUseCase.validate("PROMO10"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("fuera de vigencia");

        verify(discountCodeJpaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar 409 cuando el código ha alcanzado su límite de uso")
    void shouldThrow409WhenCodeReachedUsageLimit() {
        validEntity.setUsageCount(100);
        validEntity.setUsageLimit(100);
        when(discountCodeJpaRepository.findByCode("PROMO10"))
                .thenReturn(Optional.of(validEntity));

        assertThatThrownBy(() -> validateDiscountCodeUseCase.validate("PROMO10"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("agotado");

        verify(discountCodeJpaRepository, never()).save(any());
    }
}