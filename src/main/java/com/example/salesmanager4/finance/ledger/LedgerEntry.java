package com.example.salesmanager4.finance.ledger;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.example.salesmanager4.finance.payments.PaymentType;

import lombok.Data;

@Data
@Table("ledger_entry")
public class LedgerEntry {

    @Id
    private Long id;
    private LocalDate entryDate;
    private LedgerKind kind;
    private Long categoryId;
    private String description;
    private BigDecimal amount;
    private PaymentType paymentMethod;
    private Long supplierId;
    private Long employeeId;
}
