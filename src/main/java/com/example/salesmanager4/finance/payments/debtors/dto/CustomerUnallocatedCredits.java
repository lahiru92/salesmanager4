package com.example.salesmanager4.finance.payments.debtors.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CustomerUnallocatedCredits(Long customerId, Long paymentId, LocalDate paymentDate,
        String paymentMethod, BigDecimal totalPaymentAmount, BigDecimal allocated, BigDecimal unallocatedCredit) {
}
