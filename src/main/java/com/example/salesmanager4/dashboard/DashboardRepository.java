package com.example.salesmanager4.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardRepository {

    private final JdbcTemplate jdbcTemplate;

    public DashboardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DashboardStats getStats(LocalDate date) {
        Map<String, Object> sales = jdbcTemplate.queryForMap("""
                SELECT COUNT(*) AS invoice_count, COALESCE(SUM(total), 0) AS sales_total
                FROM invoice
                WHERE status = 'APPROVED' AND invoice_date = ?
                """, date);

        Map<String, Object> collections = jdbcTemplate.queryForMap("""
                SELECT
                    COALESCE(SUM(total_payment_amount), 0) AS collections_total,
                    COALESCE(SUM(CASE WHEN payment_method = 'CASH' THEN total_payment_amount ELSE 0 END), 0) AS cash_total
                FROM customer_payment
                WHERE direction = 'IN' AND payment_date = ?
                """, date);

        Map<String, Object> receivables = jdbcTemplate.queryForMap("""
                SELECT
                    COALESCE(SUM(total_outstanding), 0) AS outstanding,
                    COALESCE(SUM(open_invoice_count), 0) AS open_count
                FROM outstanding_balance_per_customer
                """);

        Map<String, Object> payables = jdbcTemplate.queryForMap("""
                SELECT
                    COALESCE(SUM(total_outstanding), 0) AS outstanding,
                    COALESCE(SUM(open_grn_count), 0) AS open_count
                FROM outstanding_balance_per_supplier
                """);

        String drawerStatus = jdbcTemplate.query("""
                SELECT status FROM cash_drawer_session WHERE session_date = ?
                """, rs -> rs.next() ? rs.getString("status") : "OPEN", date);

        Long pendingHandovers = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM (
                    SELECT cp.collected_by
                    FROM customer_payment cp
                    WHERE cp.payment_date = ? AND cp.direction = 'IN' AND cp.collected_by IS NOT NULL
                    GROUP BY cp.collected_by
                ) c
                WHERE NOT EXISTS (
                    SELECT 1 FROM cash_handover ch
                    WHERE ch.employee_id = c.collected_by AND ch.handover_date = ?
                )
                """, Long.class, date, date);

        return new DashboardStats(
                ((Number) sales.get("invoice_count")).longValue(),
                (BigDecimal) sales.get("sales_total"),
                (BigDecimal) collections.get("collections_total"),
                (BigDecimal) collections.get("cash_total"),
                (BigDecimal) receivables.get("outstanding"),
                ((Number) receivables.get("open_count")).longValue(),
                (BigDecimal) payables.get("outstanding"),
                ((Number) payables.get("open_count")).longValue(),
                drawerStatus,
                pendingHandovers != null ? pendingHandovers : 0);
    }
}
