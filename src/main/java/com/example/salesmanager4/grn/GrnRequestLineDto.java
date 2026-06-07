package com.example.salesmanager4.grn;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Valid
public class GrnRequestLineDto {

    @NotNull
    Long itemId;

    String itemName;

    BigDecimal orderedQty;

    @NotNull
    @Positive
    BigDecimal receivedQty;

    @NotNull
    BigDecimal acceptedQty;

    BigDecimal rejectedQty;

    @NotNull
    @Positive
    BigDecimal unitPrice;

    BigDecimal orderedPrice;


    public BigDecimal getSubTotal() {
        if (acceptedQty != null && unitPrice != null) {
            return acceptedQty.multiply(unitPrice);
        }
        return BigDecimal.ZERO;
    }
}
