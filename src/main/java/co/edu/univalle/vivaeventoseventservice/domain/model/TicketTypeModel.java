package co.edu.univalle.vivaeventoseventservice.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class TicketTypeModel {
    private UUID id;
    private UUID eventId;
    private TicketType type;
    private BigDecimal price;
    private Integer quantityAvailable;

    public TicketTypeModel(UUID id, UUID eventId, TicketType type,
                           BigDecimal price, Integer quantityAvailable) {
        this.id = id;
        this.eventId = eventId;
        this.type = type;
        this.price = price;
        this.quantityAvailable = quantityAvailable;
    }

    // getters
    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public TicketType getType() { return type; }
    public BigDecimal getPrice() { return price; }
    public Integer getQuantityAvailable() { return quantityAvailable; }

    public boolean hasStock(int requested) {
        return this.quantityAvailable >= requested;
    }
}