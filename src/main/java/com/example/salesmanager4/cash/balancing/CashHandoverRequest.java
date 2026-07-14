package com.example.salesmanager4.cash.balancing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class CashHandoverRequest {

    private Long employeeId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate handoverDate;

    private BigDecimal declaredCash;
    private String remarks;
    private List<DepositLine> deposits = new ArrayList<>();

    @Data
    public static class DepositLine {
        private String bank;
        private String referenceNumber;
        private BigDecimal amount;
    }
}
