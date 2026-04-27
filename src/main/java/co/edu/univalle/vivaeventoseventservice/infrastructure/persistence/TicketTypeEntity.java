package co.edu.univalle.vivaeventoseventservice.infrastructure.persistence;

import co.edu.univalle.vivaeventoseventservice.domain.model.TicketType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "ticket_types",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ticket_types_event_type", columnNames = {"event_id", "type"})
        }
)
public class TicketTypeEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public EventEntity getEvent() { return event; }
    public void setEvent(EventEntity event) { this.event = event; }

    public TicketType getType() { return type; }
    public void setType(TicketType type) { this.type = type; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantityAvailable() { return quantityAvailable; }
    public void setQuantityAvailable(Integer quantityAvailable) { this.quantityAvailable = quantityAvailable; }
}