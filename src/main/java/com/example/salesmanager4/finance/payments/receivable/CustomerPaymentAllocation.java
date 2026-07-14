package com.example.salesmanager4.finance.payments.receivable;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;

public record CustomerPaymentAllocation(
    @Id Long id,
    Long paymentId,
    Long invoiceId,
    BigDecimal allocatedAmount
) {
    public static CustomerPaymentAllocation of(Long paymentId, Long invoiceId, BigDecimal allocatedAmount) {
        return new CustomerPaymentAllocation(null, paymentId, invoiceId, allocatedAmount);
    }
}
