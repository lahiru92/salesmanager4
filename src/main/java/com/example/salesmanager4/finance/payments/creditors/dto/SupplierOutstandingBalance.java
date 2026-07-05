package com.example.salesmanager4.finance.payments.creditors.dto;

import java.math.BigDecimal;

public record SupplierOutstandingBalance(Long supplierId, Integer openGrnCount, BigDecimal totalCreditIssued,
        BigDecimal totalPaid, BigDecimal totalOutstanding) {
}
