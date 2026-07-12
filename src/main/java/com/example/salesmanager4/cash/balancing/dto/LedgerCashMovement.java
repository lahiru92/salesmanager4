package com.example.salesmanager4.cash.balancing.dto;

import java.math.BigDecimal;

/** Cash-method other income and expenses of one day. */
public record LedgerCashMovement(BigDecimal income, BigDecimal expense) {
}
