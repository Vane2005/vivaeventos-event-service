package co.edu.univalle.vivaeventoseventservice.application.dto;

import co.edu.univalle.vivaeventoseventservice.domain.model.DiscountType;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.DiscountCodeEntity;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class DiscountCodeResponse {
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;

    public static DiscountCodeResponse from(DiscountCodeEntity e) {
        DiscountCodeResponse r = new DiscountCodeResponse();
        r.code = e.getCode();
        r.discountType = e.getDiscountType();
        r.discountValue = e.getDiscountValue();
        return r;
    }

}