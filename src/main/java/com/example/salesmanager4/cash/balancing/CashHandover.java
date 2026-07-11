package com.example.salesmanager4.cash.balancing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Table("cash_handover")
@Data
public class CashHandover {

    @Id
    private Long id;
    private LocalDate handoverDate;
    private Long employeeId;
    private String status;

    private BigDecimal expectedAmount;
    private BigDecimal declaredCash;
    private BigDecimal cdmTotal;
    private BigDecimal variance;

    private String remarks;
    private Long verifiedBy;

    @MappedCollection(idColumn = "cash_handover_id")
    private List<CashHandoverDeposit> deposits;
}
