package com.example.salesmanager4.cash.balancing;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Table("cash_drawer_session")
@Data
public class CashDrawerSession {

    @Id
    private Long id;
    private LocalDate sessionDate;
    private String status;

    private BigDecimal openingBalance;
    private BigDecimal handoverCash;
    private BigDecimal otherCashIn;
    private BigDecimal cashOut;
    private BigDecimal expectedClosing;
    private BigDecimal countedClosing;
    private BigDecimal variance;

    private String remarks;
    private Long closedBy;
}
