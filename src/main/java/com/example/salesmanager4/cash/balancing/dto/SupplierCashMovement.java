package com.example.salesmanager4.cash.balancing.dto;

import java.math.BigDecimal;

public record SupplierCashMovement(BigDecimal cashIn, BigDecimal cashOut) {
}
