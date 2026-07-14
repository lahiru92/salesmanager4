package com.example.salesmanager4.finance.payments;

import java.math.BigDecimal;

import lombok.Data;

/**
 * One allocation row posted from a payment form.
 * documentId is the GRN id for supplier payments and the invoice id for customer payments.
 */
@Data
public class AllocationLine {

    private Long documentId;
    private BigDecimal amount;

}
