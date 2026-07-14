package com.example.salesmanager4.finance.ledger;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.salesmanager4.finance.payments.PaymentType;

import lombok.Data;

@Data
public class LedgerEntryRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate entryDate;

    private Long categoryId;
    private String description;
    private BigDecimal amount;
    private PaymentType paymentMethod;
    private Long supplierId;
}
