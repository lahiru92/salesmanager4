package com.example.salesmanager4.finance.payments.debtors.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CustomerUnallocatedRefunds(Long paymentId, Long customerId, LocalDate paymentDate,
        BigDecimal totalPaymentAmount, BigDecimal allocatedToInvoices, BigDecimal unpostedRefund) {
}
