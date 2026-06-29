package com.example.salesmanager4.finance.payments.payable;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;

public record SupplierPaymentAllocation(
    @Id Long id,
    Long paymentId,
    Long grnId,
    BigDecimal allocatedAmount
) {
    public static SupplierPaymentAllocation of(Long paymentId, Long grnId, BigDecimal allocatedAmount) {
        return new SupplierPaymentAllocation(null , paymentId,  grnId, allocatedAmount);
    }
}