package com.example.salesmanager4.finance.payments.creditors.dto;

import java.math.BigDecimal;

public record SupplierAging(Long supplierId, String supplierName, BigDecimal totalOutstanding, BigDecimal currentAmount,
        BigDecimal overdue_1_30, BigDecimal overdue_31_60, BigDecimal overdue_61_90, BigDecimal overdue_90Plus) {
}