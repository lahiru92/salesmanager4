package com.example.salesmanager4.purchase_order;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Table("purchase_order_item")
@Data
public class PurchaseOrderItem {

    @Id
    private Long id;

    private Long itemId;
    private Integer quantity;
    private BigDecimal price;
}
