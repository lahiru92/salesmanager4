package com.example.salesmanager4.finance.payments.payable;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.salesmanager4.finance.payments.PaymentDirection;
import com.example.salesmanager4.finance.payments.PaymentType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SupplierPaymentRequest {

    private Long supplierId;
    private Long grnId;
    private PaymentType paymentMethod;
    private PaymentDirection direction;
    private BigDecimal totalPaymentAmount;
    private String chequeNumber;
    private String bank;
    private String bankAccount;
    private String referenceNumber;
    private LocalDate paymentDate;

}
