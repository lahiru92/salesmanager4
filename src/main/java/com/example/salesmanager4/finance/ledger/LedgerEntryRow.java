package com.example.salesmanager4.finance.ledger;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One row of the income/expense list page (entry joined with its
 * category, supplier and recording employee names).
 */
public record LedgerEntryRow(Long id, LocalDate entryDate, String kind, String categoryName,
        String description, BigDecimal amount, String paymentMethod, String supplierName, String recordedBy) {
}
