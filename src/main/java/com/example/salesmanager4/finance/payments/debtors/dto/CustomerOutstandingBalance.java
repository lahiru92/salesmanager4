package com.example.salesmanager4.finance.payments.debtors.dto;

import java.math.BigDecimal;

public record CustomerOutstandingBalance(Long customerId, Integer openInvoiceCount, BigDecimal totalCreditIssued,
        BigDecimal totalPaid, BigDecimal totalOutstanding) {
}
