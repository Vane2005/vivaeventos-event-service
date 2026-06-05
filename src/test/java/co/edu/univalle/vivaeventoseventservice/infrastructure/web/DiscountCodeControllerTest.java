package co.edu.univalle.vivaeventoseventservice.infrastructure.web;

import co.edu.univalle.vivaeventoseventservice.application.dto.DiscountCodeResponse;
import co.edu.univalle.vivaeventoseventservice.application.usecase.ValidateDiscountCodeUseCase;
import co.edu.univalle.vivaeventoseventservice.domain.model.DiscountType;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.DiscountCodeEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.DiscountCodeJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiscountCodeController.class)
class DiscountCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DiscountCodeJpaRepository discountCodeJpaRepository;

    @MockitoBean
    private ValidateDiscountCodeUseCase validateDiscountCodeUseCase;

    // ─── Helper ───────────────────────────────────────────────────────────────

    private DiscountCodeEntity buildEntity(String code) {
        DiscountCodeEntity e = new DiscountCodeEntity();
        e.setId(UUID.randomUUID());
        e.setCode(code);
        e.setDiscountType(DiscountType.PERCENTAGE);
        e.setDiscountValue(new BigDecimal("10"));
        e.setStartsAt(OffsetDateTime.now().minusDays(1));
        e.setEndsAt(OffsetDateTime.now().plusDays(1));
        e.setUsageLimit(100);
        e.setUsageCount(0);
        e.setActive(true);
        e.setCreatedBy("user-1");
        e.setCreatedAt(Instant.now());
        e.setUpdatedBy("user-1");
        e.setUpdatedAt(Instant.now());
        return e;
    }

    private DiscountCodeResponse buildResponse(String code) {
        DiscountCodeResponse r = new DiscountCodeResponse();
        r.setCode(code);
        r.setDiscountType(DiscountType.PERCENTAGE);
        r.setDiscountValue(new BigDecimal("10"));
        return r;
    }

    // ─── POST /{code}/validate ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /{code}/validate → 200 cuando el código es válido")
    void validate_ok() throws Exception {
        when(validateDiscountCodeUseCase.validate("PROMO10"))
                .thenReturn(buildResponse("PROMO10"));

        mockMvc.perform(post("/api/v1/discount-codes/PROMO10/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PROMO10"));
    }

    @Test
    @DisplayName("POST /{code}/validate → 404 cuando el código no existe")
    void validate_notFound() throws Exception {
        when(validateDiscountCodeUseCase.validate(anyString()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Código de descuento no encontrado"));

        mockMvc.perform(post("/api/v1/discount-codes/NOEXISTE/validate"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /{code}/validate → 409 cuando el código está inactivo")
    void validate_conflict() throws Exception {
        when(validateDiscountCodeUseCase.validate(anyString()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Código inactivo"));

        mockMvc.perform(post("/api/v1/discount-codes/PROMO10/validate"))
                .andExpect(status().isConflict());
    }

    // ─── POST / (create) ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST / → 201 cuando el código se crea exitosamente (PERCENTAGE)")
    void create_ok_percentage() throws Exception {
        when(discountCodeJpaRepository.findByCode("SAVE10")).thenReturn(Optional.empty());
        when(discountCodeJpaRepository.save(any())).thenReturn(buildEntity("SAVE10"));

        String body = """
                {
                  "code": "SAVE10",
                  "discountType": "PERCENTAGE",
                  "discountValue": 10,
                  "startsAt": "2025-01-01T00:00:00Z",
                  "endsAt":   "2030-01-01T00:00:00Z",
                  "usageLimit": 100
                }
                """;

        mockMvc.perform(post("/api/v1/discount-codes")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST / → 201 cuando el código se crea exitosamente (FIXED)")
    void create_ok_fixed() throws Exception {
        when(discountCodeJpaRepository.findByCode("FIXED5")).thenReturn(Optional.empty());
        when(discountCodeJpaRepository.save(any())).thenReturn(buildEntity("FIXED5"));

        String body = """
                {
                  "code": "FIXED5",
                  "discountType": "FIXED",
                  "discountValue": 5.00,
                  "startsAt": "2025-01-01T00:00:00Z",
                  "endsAt":   "2030-01-01T00:00:00Z",
                  "usageLimit": 50
                }
                """;

        mockMvc.perform(post("/api/v1/discount-codes")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST / → 400 cuando endsAt es anterior a startsAt")
    void create_badRequest_endsAtBeforeStartsAt() throws Exception {
        String body = """
                {
                  "code": "BAD",
                  "discountType": "PERCENTAGE",
                  "discountValue": 10,
                  "startsAt": "2030-01-01T00:00:00Z",
                  "endsAt":   "2025-01-01T00:00:00Z",
                  "usageLimit": 10
                }
                """;

        mockMvc.perform(post("/api/v1/discount-codes")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST / → 400 cuando PERCENTAGE discountValue > 100")
    void create_badRequest_percentageOver100() throws Exception {
        String body = """
                {
                  "code": "OVER",
                  "discountType": "PERCENTAGE",
                  "discountValue": 150,
                  "startsAt": "2025-01-01T00:00:00Z",
                  "endsAt":   "2030-01-01T00:00:00Z",
                  "usageLimit": 10
                }
                """;

        mockMvc.perform(post("/api/v1/discount-codes")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST / → 400 cuando FIXED discountValue < 0.01")
    void create_badRequest_fixedNegative() throws Exception {
        String body = """
                {
                  "code": "NEG",
                  "discountType": "FIXED",
                  "discountValue": 0,
                  "startsAt": "2025-01-01T00:00:00Z",
                  "endsAt":   "2030-01-01T00:00:00Z",
                  "usageLimit": 10
                }
                """;

        mockMvc.perform(post("/api/v1/discount-codes")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST / → 409 cuando el código ya existe")
    void create_conflict_codeAlreadyExists() throws Exception {
        when(discountCodeJpaRepository.findByCode("PROMO10"))
                .thenReturn(Optional.of(buildEntity("PROMO10")));

        String body = """
                {
                  "code": "PROMO10",
                  "discountType": "PERCENTAGE",
                  "discountValue": 10,
                  "startsAt": "2025-01-01T00:00:00Z",
                  "endsAt":   "2030-01-01T00:00:00Z",
                  "usageLimit": 100
                }
                """;

        mockMvc.perform(post("/api/v1/discount-codes")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    // ─── PATCH /{code}/activate ───────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /{code}/activate → 200 cuando el código existe")
    void activate_ok() throws Exception {
        when(discountCodeJpaRepository.findByCode("PROMO10"))
                .thenReturn(Optional.of(buildEntity("PROMO10")));
        when(discountCodeJpaRepository.save(any())).thenReturn(buildEntity("PROMO10"));

        mockMvc.perform(patch("/api/v1/discount-codes/PROMO10/activate")
                        .header("X-User-Id", "user-1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /{code}/activate → 404 cuando el código no existe")
    void activate_notFound() throws Exception {
        when(discountCodeJpaRepository.findByCode(anyString()))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/v1/discount-codes/NOEXISTE/activate")
                        .header("X-User-Id", "user-1"))
                .andExpect(status().isNotFound());
    }

    // ─── PATCH /{code}/deactivate ─────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /{code}/deactivate → 200 cuando el código existe")
    void deactivate_ok() throws Exception {
        when(discountCodeJpaRepository.findByCode("PROMO10"))
                .thenReturn(Optional.of(buildEntity("PROMO10")));
        when(discountCodeJpaRepository.save(any())).thenReturn(buildEntity("PROMO10"));

        mockMvc.perform(patch("/api/v1/discount-codes/PROMO10/deactivate")
                        .header("X-User-Id", "user-1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /{code}/deactivate → 404 cuando el código no existe")
    void deactivate_notFound() throws Exception {
        when(discountCodeJpaRepository.findByCode(anyString()))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/v1/discount-codes/NOEXISTE/deactivate")
                        .header("X-User-Id", "user-1"))
                .andExpect(status().isNotFound());
    }
}