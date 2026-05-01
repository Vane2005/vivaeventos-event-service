package co.edu.univalle.vivaeventoseventservice.application.dto;

import co.edu.univalle.vivaeventoseventservice.domain.model.TicketType;
import co.edu.univalle.vivaeventoseventservice.domain.model.TicketTypeModel;

import java.math.BigDecimal;
import java.util.UUID;

public class TicketTypeResponse {
    private UUID id;
    private TicketType type;
    private BigDecimal price;
    private Integer quantityAvailable;

    public static TicketTypeResponse from(TicketTypeModel model) {
        TicketTypeResponse r = new TicketTypeResponse();
        r.id = model.getId();
        r.type = model.getType();
        r.price = model.getPrice();
        r.quantityAvailable = model.getQuantityAvailable();
        return r;
    }

    public UUID getId() { return id; }
    public TicketType getType() { return type; }
    public BigDecimal getPrice() { return price; }
    public Integer getQuantityAvailable() { return quantityAvailable; }
}