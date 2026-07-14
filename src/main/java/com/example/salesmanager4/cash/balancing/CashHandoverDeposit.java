package com.example.salesmanager4.cash.balancing;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Data
@Table("cash_handover_deposit")
public class CashHandoverDeposit {

    @Id
    private Long id;
    private Long cashHandoverId;
    private String bank;
    private String referenceNumber;
    private BigDecimal amount;
}
