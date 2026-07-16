package com.example.salesmanager4.purchase_order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Table("purchase_order")
@Data
public class PurchaseOrder {

    @Id
    private Long id;

    private Long supplierId;
    private LocalDate orderDate;
    private String status; // DRAFT, SENT, APPROVED, RECEIVED
    private Long createdBy;
    private LocalDateTime createdAt;

    @MappedCollection(idColumn = "purchase_order_id")
    private List<PurchaseOrderItem> items = new ArrayList<>();
}
