package com.example.salesmanager4.cash.balancing.dto;

import java.math.BigDecimal;

public record CollectionLine(Long paymentId, String customerName, String direction, BigDecimal amount,
        String chequeNumber, String bank, String referenceNumber) {

    /** Signed amount: receipts (IN) add, refunds (OUT) subtract. */
    public BigDecimal signedAmount() {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return "OUT".equals(direction) ? amount.negate() : amount;
    }
}
