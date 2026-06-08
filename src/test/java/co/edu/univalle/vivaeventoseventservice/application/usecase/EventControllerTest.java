package co.edu.univalle.vivaeventoseventservice.application.usecase;

import co.edu.univalle.vivaeventoseventservice.infrastructure.web.EventController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReserveStockUseCase reserveStockUseCase;

    @Test
    void shouldReleaseStock() throws Exception {

        UUID ticketTypeId = UUID.randomUUID();

        mockMvc.perform(
                        put("/ticket-types/{ticketTypeId}/release", ticketTypeId)
                                .param("quantity", "5")
                )
                .andExpect(status().isOk());

        verify(reserveStockUseCase)
                .release(ticketTypeId, 5);
    }
}

