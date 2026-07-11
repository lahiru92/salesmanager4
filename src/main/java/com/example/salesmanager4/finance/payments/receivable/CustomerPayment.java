package com.example.salesmanager4.finance.payments.receivable;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;

import com.example.salesmanager4.finance.payments.PaymentDirection;
import com.example.salesmanager4.finance.payments.PaymentType;

import lombok.Data;

@Data
public class CustomerPayment {

    @Id
    private Long id;
    private Long customerId;
    private PaymentType paymentMethod;
    private PaymentDirection direction;
    private BigDecimal totalPaymentAmount;
    private String chequeNumber;
    private String bank;
    private String bankAccount;
    private String referenceNumber;
    private LocalDate paymentDate;

}
