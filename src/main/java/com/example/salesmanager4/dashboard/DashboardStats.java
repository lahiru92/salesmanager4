package com.example.salesmanager4.dashboard;

import java.math.BigDecimal;

/**
 * Aggregated figures shown on the home dashboard KPI cards.
 */
public record DashboardStats(
        long invoicesToday,
        BigDecimal salesToday,
        BigDecimal collectionsToday,
        BigDecimal cashCollectedToday,
        BigDecimal receivablesTotal,
        long openInvoiceCount,
        BigDecimal payablesTotal,
        long openGrnCount,
        String drawerStatus,
        long pendingHandovers) {
}
