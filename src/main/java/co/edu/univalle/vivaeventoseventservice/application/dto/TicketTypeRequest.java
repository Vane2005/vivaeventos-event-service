package co.edu.univalle.vivaeventoseventservice.application.dto;

import co.edu.univalle.vivaeventoseventservice.domain.model.TicketType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TicketTypeRequest {

    @NotNull
    private TicketType type;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @NotNull
    @Min(1)
    private Integer quantityAvailable;

    public TicketType getType() { return type; }
    public void setType(TicketType type) { this.type = type; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantityAvailable() { return quantityAvailable; }
    public void setQuantityAvailable(Integer quantityAvailable) { this.quantityAvailable = quantityAvailable; }
}