package com.example.salesmanager4.purchase_order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PoListRow {

    Long id;
    String supplierName;
    LocalDate orderDate;
    String status;
    String createdByName;
    LocalDateTime createdAt;
    BigDecimal total;
}
