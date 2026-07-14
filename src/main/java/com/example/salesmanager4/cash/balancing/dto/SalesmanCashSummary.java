package com.example.salesmanager4.cash.balancing.dto;

import java.math.BigDecimal;

public record SalesmanCashSummary(Long employeeId, String employeeName, BigDecimal expectedCash,
        Integer receiptCount, Long handoverId, BigDecimal declaredCash, BigDecimal cdmTotal, BigDecimal variance,
        String status) {

    public boolean isVerified() {
        return handoverId != null;
    }
}
