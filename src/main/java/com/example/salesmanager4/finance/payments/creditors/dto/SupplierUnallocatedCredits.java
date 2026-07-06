package com.example.salesmanager4.finance.payments.creditors.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SupplierUnallocatedCredits(Long supplierId, Long paymentId, LocalDate paymentDate, String paymentMethod,
        BigDecimal totalPaymentAmount, BigDecimal allocated, BigDecimal unallocatedCredit) {
}
