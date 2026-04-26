package co.edu.univalle.vivaeventoseventservice.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class DefineTicketTypesRequest {

    @NotNull
    @NotEmpty
    @Valid
    private List<TicketTypeRequest> ticketTypes;

    public List<TicketTypeRequest> getTicketTypes() { return ticketTypes; }
    public void setTicketTypes(List<TicketTypeRequest> ticketTypes) { this.ticketTypes = ticketTypes; }
}