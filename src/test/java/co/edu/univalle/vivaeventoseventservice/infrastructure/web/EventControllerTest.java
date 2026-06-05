package co.edu.univalle.vivaeventoseventservice.infrastructure.web;

import co.edu.univalle.vivaeventoseventservice.application.dto.*;
import co.edu.univalle.vivaeventoseventservice.application.usecase.CancelEventUseCase;
import co.edu.univalle.vivaeventoseventservice.application.usecase.GetTicketTypesUseCase;
import co.edu.univalle.vivaeventoseventservice.application.usecase.ReserveStockUseCase;
import co.edu.univalle.vivaeventoseventservice.application.usecase.UpdateEventUseCase;
import co.edu.univalle.vivaeventoseventservice.domain.model.EventStatus;
import co.edu.univalle.vivaeventoseventservice.domain.model.TicketType;
import co.edu.univalle.vivaeventoseventservice.domain.model.TicketTypeModel;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventJpaRepository;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeJpaRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventJpaRepository eventJpaRepository;

    @MockitoBean
    private TicketTypeJpaRepository ticketTypeJpaRepository;

    @MockitoBean
    private GetTicketTypesUseCase getTicketTypesUseCase;

    @MockitoBean
    private ReserveStockUseCase reserveStockUseCase;

    @MockitoBean
    private CancelEventUseCase cancelEventUseCase;

    @MockitoBean
    private UpdateEventUseCase updateEventUseCase;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private EventEntity buildEvent(UUID id, EventStatus status) {
        EventEntity e = new EventEntity();
        e.setId(id);
        e.setName("Concierto Test");
        e.setDescription("Descripción");
        e.setLocation("Cali");
        e.setCapacity(500);
        e.setStatus(status);
        e.setEventDate(OffsetDateTime.now().plusDays(1));
        e.setCreatedBy("user-1");
        e.setCreatedAt(Instant.now());
        return e;
    }

    private TicketTypeModel buildTicketTypeModel() {
        return new TicketTypeModel(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TicketType.VIP,
                new BigDecimal("50000"),
                100
        );
    }

    private TicketTypeEntity buildTicketTypeEntity(EventEntity event) {
        TicketTypeEntity t = new TicketTypeEntity();
        t.setId(UUID.randomUUID());
        t.setEvent(event);
        t.setType(TicketType.GENERAL);
        t.setPrice(new BigDecimal("30000"));
        t.setQuantityAvailable(200);
        return t;
    }

    // ─── POST / (createEvent) ─────────────────────────────────────────────────

    @Test
    @DisplayName("POST /events → 201 cuando el evento se crea exitosamente")
    void createEvent_ok() throws Exception {
        UUID id = UUID.randomUUID();
        when(eventJpaRepository.save(any())).thenReturn(buildEvent(id, EventStatus.ACTIVE));

        String body = """
                {
                  "name": "Concierto Test",
                  "description": "Descripción",
                  "eventDate": "2030-06-15T20:00:00Z",
                  "location": "Cali",
                  "capacity": 500
                }
                """;

        mockMvc.perform(post("/api/v1/events")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    // ─── POST /{eventId}/ticket-types (defineTicketTypes) ────────────────────

    @Test
    @DisplayName("POST /{eventId}/ticket-types → 201 cuando se crean los tipos de boleta")
    void defineTicketTypes_ok() throws Exception {
        UUID eventId = UUID.randomUUID();
        EventEntity event = buildEvent(eventId, EventStatus.ACTIVE);

        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(ticketTypeJpaRepository.findByEvent_Id(eventId)).thenReturn(List.of());
        when(ticketTypeJpaRepository.saveAll(any())).thenReturn(List.of(buildTicketTypeEntity(event)));

        String body = """
                {
                  "ticketTypes": [
                    { "type": "GENERAL", "price": 30000, "quantityAvailable": 200 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/events/" + eventId + "/ticket-types")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /{eventId}/ticket-types → 404 cuando el evento no existe")
    void defineTicketTypes_eventNotFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.empty());

        String body = """
                {
                  "ticketTypes": [
                    { "type": "GENERAL", "price": 30000, "quantityAvailable": 100 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/events/" + eventId + "/ticket-types")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /{eventId}/ticket-types → 409 cuando el evento está cancelado")
    void defineTicketTypes_eventCancelled() throws Exception {
        UUID eventId = UUID.randomUUID();
        EventEntity event = buildEvent(eventId, EventStatus.CANCELLED);

        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.of(event));

        String body = """
                {
                  "ticketTypes": [
                    { "type": "GENERAL", "price": 30000, "quantityAvailable": 100 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/events/" + eventId + "/ticket-types")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /{eventId}/ticket-types → 400 cuando la cantidad supera el aforo")
    void defineTicketTypes_exceedsCapacity() throws Exception {
        UUID eventId = UUID.randomUUID();
        EventEntity event = buildEvent(eventId, EventStatus.ACTIVE); // capacity = 500

        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(ticketTypeJpaRepository.findByEvent_Id(eventId)).thenReturn(List.of());

        String body = """
                {
                  "ticketTypes": [
                    { "type": "GENERAL", "price": 30000, "quantityAvailable": 600 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/events/" + eventId + "/ticket-types")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /{eventId}/ticket-types ─────────────────────────────────────────

    @Test
    @DisplayName("GET /{eventId}/ticket-types → 200 con lista de tipos")
    void getTicketTypes_ok() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(getTicketTypesUseCase.getByEvent(eventId))
                .thenReturn(List.of(buildTicketTypeModel()));

        mockMvc.perform(get("/api/v1/events/" + eventId + "/ticket-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /{eventId}/ticket-types → 200 con lista vacía si no hay tipos")
    void getTicketTypes_empty() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(getTicketTypesUseCase.getByEvent(eventId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/events/" + eventId + "/ticket-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ─── GET /ticket-types/{ticketTypeId} ────────────────────────────────────

    @Test
    @DisplayName("GET /ticket-types/{id} → 200 cuando existe el tipo")
    void getTicketType_ok() throws Exception {
        UUID ticketTypeId = UUID.randomUUID();
        when(getTicketTypesUseCase.getById(ticketTypeId)).thenReturn(buildTicketTypeModel());

        mockMvc.perform(get("/api/v1/events/ticket-types/" + ticketTypeId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /ticket-types/{id} → 404 cuando no existe el tipo")
    void getTicketType_notFound() throws Exception {
        UUID ticketTypeId = UUID.randomUUID();
        when(getTicketTypesUseCase.getById(ticketTypeId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de boleta no encontrado"));

        mockMvc.perform(get("/api/v1/events/ticket-types/" + ticketTypeId))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /ticket-types/{ticketTypeId}/reserve ─────────────────────────────

    @Test
    @DisplayName("PUT /ticket-types/{id}/reserve → 200 cuando se reserva exitosamente")
    void reserveStock_ok() throws Exception {
        UUID ticketTypeId = UUID.randomUUID();
        doNothing().when(reserveStockUseCase).execute(ticketTypeId, 3);

        mockMvc.perform(put("/api/v1/events/ticket-types/" + ticketTypeId + "/reserve")
                        .param("quantity", "3"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /ticket-types/{id}/reserve → 409 cuando stock insuficiente")
    void reserveStock_insufficient() throws Exception {
        UUID ticketTypeId = UUID.randomUUID();
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente"))
                .when(reserveStockUseCase).execute(eq(ticketTypeId), anyInt());

        mockMvc.perform(put("/api/v1/events/ticket-types/" + ticketTypeId + "/reserve")
                        .param("quantity", "9999"))
                .andExpect(status().isConflict());
    }

    // ─── GET / (getActiveEvents) ──────────────────────────────────────────────

    @Test
    @DisplayName("GET /events → 200 con lista de eventos activos")
    void getActiveEvents_ok() throws Exception {
        UUID id = UUID.randomUUID();
        when(eventJpaRepository.findByStatus(EventStatus.ACTIVE))
                .thenReturn(List.of(buildEvent(id, EventStatus.ACTIVE)));

        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /events → 200 con lista vacía cuando no hay eventos activos")
    void getActiveEvents_empty() throws Exception {
        when(eventJpaRepository.findByStatus(EventStatus.ACTIVE)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ─── GET /{eventId} (getEventDetail) ─────────────────────────────────────

    @Test
    @DisplayName("GET /{eventId} → 200 cuando el evento existe y está activo")
    void getEventDetail_ok() throws Exception {
        UUID eventId = UUID.randomUUID();
        EventEntity event = buildEvent(eventId, EventStatus.ACTIVE);

        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(getTicketTypesUseCase.getByEvent(eventId)).thenReturn(List.of(buildTicketTypeModel()));

        mockMvc.perform(get("/api/v1/events/" + eventId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /{eventId} → 404 cuando el evento no existe")
    void getEventDetail_notFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/events/" + eventId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /{eventId} → 404 cuando el evento está cancelado")
    void getEventDetail_cancelled() throws Exception {
        UUID eventId = UUID.randomUUID();
        EventEntity event = buildEvent(eventId, EventStatus.CANCELLED);

        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.of(event));

        mockMvc.perform(get("/api/v1/events/" + eventId))
                .andExpect(status().isNotFound());
    }

    // ─── PATCH /{eventId}/cancel ──────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /{eventId}/cancel → 204 cuando se cancela exitosamente")
    void cancelEvent_ok() throws Exception {
        UUID eventId = UUID.randomUUID();
        doNothing().when(cancelEventUseCase).execute(eq(eventId), anyString(), anyString());

        String body = """
                { "reason": "Fuerza mayor" }
                """;

        mockMvc.perform(patch("/api/v1/events/" + eventId + "/cancel")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /{eventId}/cancel → 404 cuando el evento no existe")
    void cancelEvent_notFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"))
                .when(cancelEventUseCase).execute(eq(eventId), anyString(), anyString());

        String body = """
                { "reason": "Motivo suficientemente largo para pasar validación" }
                """;

        mockMvc.perform(patch("/api/v1/events/" + eventId + "/cancel")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ─── PATCH /{eventId} (updateEvent) ──────────────────────────────────────

    @Test
    @DisplayName("PATCH /{eventId} → 200 cuando el evento se actualiza exitosamente")
    void updateEvent_ok() throws Exception {
        UUID eventId = UUID.randomUUID();
        EventEntity updated = buildEvent(eventId, EventStatus.ACTIVE);
        updated.setName("Nombre actualizado");

        when(updateEventUseCase.execute(eq(eventId), anyString(), any()))
                .thenReturn(updated);

        String body = """
                {
                  "name": "Nombre actualizado",
                  "description": "Descripción nueva",
                  "eventDate": "2030-12-01T20:00:00Z",
                  "location": "Medellín",
                  "capacity": 600
                }
                """;

        mockMvc.perform(patch("/api/v1/events/" + eventId)
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nombre actualizado"));
    }

    @Test
    @DisplayName("PATCH /{eventId} → 404 cuando el evento no existe")
    void updateEvent_notFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(updateEventUseCase.execute(eq(eventId), anyString(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        String body = """
                {
                  "name": "Nuevo nombre",
                  "description": "Descripción suficientemente larga",
                  "eventDate": "2030-12-01T20:00:00Z",
                  "location": "Bogotá",
                  "capacity": 300
                }
                """;

        mockMvc.perform(patch("/api/v1/events/" + eventId)
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}