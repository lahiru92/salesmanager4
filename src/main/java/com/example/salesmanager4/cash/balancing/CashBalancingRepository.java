package com.example.salesmanager4.cash.balancing;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.salesmanager4.cash.balancing.dto.CollectionLine;
import com.example.salesmanager4.cash.balancing.dto.SalesmanCashSummary;
import com.example.salesmanager4.cash.balancing.dto.SupplierCashMovement;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CashBalancingRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * One row per salesman who either collected cash on the given day
     * or already has a handover record for it.
     */
    public List<SalesmanCashSummary> getSalesmanCashSummaries(LocalDate date) {
        String sql = """
            SELECT
                e.id                            AS employee_id,
                e.known_name                    AS employee_name,
                COALESCE(exp.expected_cash, 0)  AS expected_cash,
                COALESCE(exp.receipt_count, 0)  AS receipt_count,
                ch.id                           AS handover_id,
                ch.declared_cash,
                ch.cdm_total,
                ch.variance,
                ch.status
            FROM employee e
            LEFT JOIN (
                SELECT
                    collected_by,
                    SUM(CASE direction WHEN 'IN' THEN total_payment_amount ELSE -total_payment_amount END) AS expected_cash,
                    COUNT(*) AS receipt_count
                FROM customer_payment
                WHERE payment_method = 'CASH'
                  AND payment_date = ?
                  AND collected_by IS NOT NULL
                GROUP BY collected_by
            ) exp ON exp.collected_by = e.id
            LEFT JOIN cash_handover ch ON ch.employee_id = e.id AND ch.handover_date = ?
            WHERE exp.collected_by IS NOT NULL OR ch.id IS NOT NULL
            ORDER BY e.known_name
            """;
        return jdbcTemplate.query(sql, new DataClassRowMapper<>(SalesmanCashSummary.class), date, date);
    }

    /**
     * Collections of one salesman on one day for the given payment method (CASH / CHEQUE).
     */
    public List<CollectionLine> getCollections(Long employeeId, LocalDate date, String paymentMethod) {
        String sql = """
            SELECT
                cp.id                   AS payment_id,
                c.name                  AS customer_name,
                cp.direction,
                cp.total_payment_amount AS amount,
                cp.cheque_number,
                cp.bank,
                cp.reference_number
            FROM customer_payment cp
            LEFT JOIN customer c ON c.customer_id = cp.customer_id
            WHERE cp.payment_method = ?
              AND cp.collected_by = ?
              AND cp.payment_date = ?
            ORDER BY cp.id
            """;
        return jdbcTemplate.query(sql, new DataClassRowMapper<>(CollectionLine.class), paymentMethod, employeeId, date);
    }

    /**
     * Cash paid to / received from suppliers on the given day (drawer outflow / inflow).
     */
    public SupplierCashMovement getSupplierCashMovement(LocalDate date) {
        String sql = """
            SELECT
                COALESCE(SUM(CASE direction WHEN 'IN'  THEN total_payment_amount ELSE 0 END), 0) AS cash_in,
                COALESCE(SUM(CASE direction WHEN 'OUT' THEN total_payment_amount ELSE 0 END), 0) AS cash_out
            FROM supplier_payment
            WHERE payment_method = 'CASH'
              AND payment_date = ?
            """;
        return jdbcTemplate.queryForObject(sql, new DataClassRowMapper<>(SupplierCashMovement.class), date);
    }
}
