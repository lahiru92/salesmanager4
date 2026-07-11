package com.example.salesmanager4.finance.payments.debtors.dto;

import java.math.BigDecimal;

public record CustomerAging(Long customerId, String customerName, BigDecimal totalOutstanding,
        BigDecimal currentAmount, BigDecimal overdue_1_30, BigDecimal overdue_31_60, BigDecimal overdue_61_90,
        BigDecimal overdue_90Plus) {
}
