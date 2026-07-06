package com.example.salesmanager4.finance.payments.creditors.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SupplierUnallocatedRefunds(Long paymentId, Long supplierId, LocalDate paymentDate,
        BigDecimal totalPaymentAmount, BigDecimal allocatedToGrns, BigDecimal unpostedRefund) {
}