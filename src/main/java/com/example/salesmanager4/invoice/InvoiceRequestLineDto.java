package com.example.salesmanager4.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
@Valid
public class InvoiceRequestLineDto {

    @NotNull
    Long itemId;

    String itemName;

    @NotNull
    @Positive
    BigDecimal quantity;

    @PositiveOrZero
    BigDecimal freeQty;

    @NotNull
    @Positive
    BigDecimal unitPrice;

    @PositiveOrZero
    BigDecimal discount;


    public BigDecimal getSubTotal() {
        if (quantity != null && unitPrice != null) {
            BigDecimal disc = discount != null ? discount : BigDecimal.ZERO;
            return quantity.multiply(unitPrice).subtract(disc).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
