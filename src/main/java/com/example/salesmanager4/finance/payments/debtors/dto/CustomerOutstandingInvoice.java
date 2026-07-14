package com.example.salesmanager4.finance.payments.debtors.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CustomerOutstandingInvoice(Long invoiceId, Long customerId, LocalDate invoiceDate, LocalDate creditDue,
        BigDecimal creditAtSale, BigDecimal paidAgainstInvoice, BigDecimal outstandingBalance, Integer daysOverdue,
        String agingBucket) {
}
