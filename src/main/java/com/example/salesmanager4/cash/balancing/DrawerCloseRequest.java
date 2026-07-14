package com.example.salesmanager4.cash.balancing;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class DrawerCloseRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate sessionDate;

    private BigDecimal openingBalance;
    private BigDecimal countedClosing;
    private String remarks;
}
