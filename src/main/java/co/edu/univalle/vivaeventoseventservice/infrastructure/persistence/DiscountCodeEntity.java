package co.edu.univalle.vivaeventoseventservice.infrastructure.persistence;

import co.edu.univalle.vivaeventoseventservice.domain.model.DiscountType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "discount_codes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_discount_codes_code", columnNames = {"code"})
        }
)
public class DiscountCodeEntity {

        @Id
        @GeneratedValue
        private UUID id;

        @Column(nullable = false, length = 50)
        private String code;

        @Enumerated(EnumType.STRING)
        @Column(name = "discount_type", nullable = false, length = 20)
        private DiscountType discountType;

        @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
        private BigDecimal discountValue;

        @Column(name = "starts_at", nullable = false)
        private OffsetDateTime startsAt;

        @Column(name = "ends_at", nullable = false)
        private OffsetDateTime endsAt;

        @Column(name = "usage_limit", nullable = false)
        private Integer usageLimit;

        @Column(name = "usage_count", nullable = false)
        private Integer usageCount = 0;

        @Column(nullable = false)
        private Boolean active = true;

        @Column(name = "created_by", nullable = false, length = 100)
        private String createdBy;

        @Column(name = "created_at", nullable = false)
        private Instant createdAt;

        @Column(name = "updated_by", nullable = false, length = 100)
        private String updatedBy;

        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public DiscountType getDiscountType() { return discountType; }
        public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }

        public BigDecimal getDiscountValue() { return discountValue; }
        public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

        public OffsetDateTime getStartsAt() { return startsAt; }
        public void setStartsAt(OffsetDateTime startsAt) { this.startsAt = startsAt; }

        public OffsetDateTime getEndsAt() { return endsAt; }
        public void setEndsAt(OffsetDateTime endsAt) { this.endsAt = endsAt; }

        public Integer getUsageLimit() { return usageLimit; }
        public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }

        public Integer getUsageCount() { return usageCount; }
        public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }

        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

        public String getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

        public Instant getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}