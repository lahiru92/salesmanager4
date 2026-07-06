package com.example.salesmanager4.finance.payments.creditors.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SupplierOutstandingGrn(Long grnId, Long supplierId, LocalDate receivedDate, LocalDate creditDue,
        BigDecimal creditAtDelivery, BigDecimal paidAgainstGrn, BigDecimal outstandingBalance, Integer daysOverdue,
        String agingBucket) {
}