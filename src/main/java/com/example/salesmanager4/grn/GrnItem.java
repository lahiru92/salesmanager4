package com.example.salesmanager4.grn;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;


@Data
@Table("grn_item")
public class GrnItem {

    @Id
    private Long id;
    private Long grnId;
    private Long itemId;
    private String itemName;
    private BigDecimal orderedQty;
    private BigDecimal receivedQty;
    private BigDecimal rejectedQty;
    private BigDecimal unitPrice;
    private BigDecimal orderedPrice;

    public BigDecimal getSubTotal() {
        if (receivedQty != null && unitPrice != null) {

            if (rejectedQty == null) {
                rejectedQty = BigDecimal.ZERO;
            }

            return unitPrice.multiply(receivedQty.subtract(rejectedQty));
        }
        return BigDecimal.ZERO;
    }
}
