package com.example.salesmanager4.invoice;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;


@Data
@Table("invoice_item")
public class InvoiceItem {

    @Id
    private Long id;
    private Long invoiceId;
    private Long itemId;
    private String itemName;
    private BigDecimal quantity;
    private BigDecimal freeQty;
    private BigDecimal unitPrice;
    private BigDecimal discount;

    public BigDecimal getSubTotal() {
        if (quantity != null && unitPrice != null) {

            if (discount == null) {
                discount = BigDecimal.ZERO;
            }

            return unitPrice.multiply(quantity).subtract(discount);
        }
        return BigDecimal.ZERO;
    }
}
