package com.example.salesmanager4.reports;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * All report queries. Rows come back as column-name -> value maps and are
 * formatted into display strings by ReportBuilderService.
 *
 * The grouping unit interpolated into date_trunc() is whitelisted to
 * "month" / "quarter" by ReportParams.
 */
@Repository
@RequiredArgsConstructor
public class ReportQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    // ------------------------------------------------------------------
    // Purchasing
    // ------------------------------------------------------------------

    /** 1. Purchase value per month/quarter, split cash/cheque/credit, with GRN count. */
    public List<Map<String, Object>> purchasesSummary(LocalDate from, LocalDate to, String grouping) {
        String sql = """
            SELECT
                date_trunc('%s', g.received_date::timestamp)::date AS period,
                COUNT(*)       AS grn_count,
                SUM(g.total)   AS total,
                SUM(g.cash)    AS cash,
                SUM(g.cheque)  AS cheque,
                SUM(g.credit)  AS credit
            FROM grn g
            WHERE g.status = 'APPROVED'
              AND g.received_date BETWEEN ? AND ?
            GROUP BY 1
            ORDER BY 1
            """.formatted(grouping);
        return jdbcTemplate.queryForList(sql, from, to);
    }

    /** 2. Total purchased, paid and outstanding per supplier, ranked. */
    public List<Map<String, Object>> purchasesBySupplier(LocalDate from, LocalDate to) {
        String sql = """
            WITH alloc AS (
                SELECT spa.grn_id,
                       SUM(CASE sp.direction WHEN 'OUT' THEN spa.allocated_amount ELSE -spa.allocated_amount END) AS paid
                FROM supplier_payment_allocation spa
                JOIN supplier_payment sp ON sp.id = spa.payment_id
                GROUP BY spa.grn_id
            )
            SELECT
                s.name                                        AS supplier_name,
                COUNT(g.id)                                   AS grn_count,
                SUM(g.total)                                  AS total_purchased,
                SUM(COALESCE(a.paid, 0))                      AS total_paid,
                SUM(g.total) - SUM(COALESCE(a.paid, 0))       AS outstanding
            FROM grn g
            JOIN supplier s ON s.supplier_id = g.supplier_id
            LEFT JOIN alloc a ON a.grn_id = g.id
            WHERE g.status = 'APPROVED'
              AND g.received_date BETWEEN ? AND ?
            GROUP BY s.name
            ORDER BY total_purchased DESC
            """;
        return jdbcTemplate.queryForList(sql, from, to);
    }

    /** 3a. Purchased value and quantity per category. */
    public List<Map<String, Object>> purchasesByCategory(LocalDate from, LocalDate to) {
        String sql = """
            SELECT
                c.name AS category_name,
                SUM(gi.received_qty - COALESCE(gi.rejected_qty, 0)) AS qty,
                SUM((gi.received_qty - COALESCE(gi.rejected_qty, 0)) * gi.unit_price) AS value
            FROM grn_item gi
            JOIN grn g       ON g.id = gi.grn_id
            JOIN item i      ON i.item_id = gi.item_id
            JOIN category c  ON c.category_id = i.category_id
            WHERE g.status = 'APPROVED'
              AND g.received_date BETWEEN ? AND ?
            GROUP BY c.name
            ORDER BY value DESC
            """;
        return jdbcTemplate.queryForList(sql, from, to);
    }

    /** 3b. Top purchased items by value. */
    public List<Map<String, Object>> purchasesTopItems(LocalDate from, LocalDate to) {
        String sql = """
            SELECT
                i.code,
                i.name AS item_name,
                c.name AS category_name,
                SUM(gi.received_qty - COALESCE(gi.rejected_qty, 0)) AS qty,
                SUM((gi.received_qty - COALESCE(gi.rejected_qty, 0)) * gi.unit_price) AS value
            FROM grn_item gi
            JOIN grn g       ON g.id = gi.grn_id
            JOIN item i      ON i.item_id = gi.item_id
            JOIN category c  ON c.category_id = i.category_id
            WHERE g.status = 'APPROVED'
              AND g.received_date BETWEEN ? AND ?
            GROUP BY i.code, i.name, c.name
            ORDER BY value DESC
            LIMIT 20
            """;
        return jdbcTemplate.queryForList(sql, from, to);
    }

    /** 4. Rejected quantity and value by supplier and item. */
    public List<Map<String, Object>> rejectedGoods(LocalDate from, LocalDate to) {
        String sql = """
            SELECT
                s.name AS supplier_name,
                i.code,
                i.name AS item_name,
                COUNT(DISTINCT g.id)                      AS grn_count,
                SUM(gi.rejected_qty)                      AS rejected_qty,
                SUM(gi.rejected_qty * gi.unit_price)      AS rejected_value
            FROM grn_item gi
            JOIN grn g       ON g.id = gi.grn_id
            JOIN supplier s  ON s.supplier_id = g.supplier_id
            JOIN item i      ON i.item_id = gi.item_id
            WHERE g.received_date BETWEEN ? AND ?
              AND COALESCE(gi.rejected_qty, 0) > 0
            GROUP BY s.name, i.code, i.name
            ORDER BY rejected_value DESC
            """;
        return jdbcTemplate.queryForList(sql, from, to);
    }

    // ------------------------------------------------------------------
    // Creditors / payables
    // ------------------------------------------------------------------

    /** 5. Total owed per supplier with open-GRN count. */
    public List<Map<String, Object>> supplierOutstanding() {
        String sql = """
            SELECT
                s.name AS supplier_name,
                v.open_grn_count,
                v.total_credit_issued,
                v.total_paid,
                v.total_outstanding
            FROM outstanding_balance_per_supplier v
            JOIN supplier s ON s.supplier_id = v.supplier_id
            ORDER BY v.total_outstanding DESC
            """;
        return jdbcTemplate.queryForList(sql);
    }

    /** 6. Supplier aging buckets. */
    public List<Map<String, Object>> supplierAging() {
        String sql = """
            SELECT
                supplier_name, total_outstanding, current_amount,
                overdue_1_30, overdue_31_60, overdue_61_90, overdue_90_plus
            FROM supplier_aging
            ORDER BY total_outstanding DESC
            """;
        return jdbcTemplate.queryForList(sql);
    }

    /** 7. Payments made to suppliers per month/quarter, split by method. */
    public List<Map<String, Object>> supplierPayments(LocalDate from, LocalDate to, String grouping) {
        String sql = """
            SELECT
                date_trunc('%s', sp.payment_date::timestamp)::date AS period,
                COUNT(*) AS payment_count,
                SUM(CASE WHEN sp.payment_method = 'CASH'          THEN sp.total_payment_amount ELSE 0 END) AS cash,
                SUM(CASE WHEN sp.payment_method = 'CHEQUE'        THEN sp.total_payment_amount ELSE 0 END) AS cheque,
                SUM(CASE WHEN sp.payment_method = 'BANK_TRANSFER' THEN sp.total_payment_amount ELSE 0 END) AS bank_transfer,
                SUM(sp.total_payment_amount) AS total
            FROM supplier_payment sp
            WHERE sp.direction = 'OUT'
              AND sp.payment_date BETWEEN ? AND ?
            GROUP BY 1
            ORDER BY 1
            """.formatted(grouping);
        return jdbcTemplate.queryForList(sql, from, to);
    }

    /** 8. Open GRNs whose credit_due falls within the horizon (overdue included). */
    public List<Map<String, Object>> paymentsDue(int horizonDays) {
        String sql = """
            SELECT
                s.name AS supplier_name,
                v.grn_id,
                v.received_date,
                v.credit_due,
                v.credit_due - CURRENT_DATE AS days_left,
                v.outstanding_balance
            FROM outstanding_grns_per_supplier v
            JOIN supplier s ON s.supplier_id = v.supplier_id
            WHERE v.credit_due IS NOT NULL
              AND v.credit_due <= CURRENT_DATE + ?
            ORDER BY v.credit_due, s.name
            """;
        return jdbcTemplate.queryForList(sql, horizonDays);
    }

    // ------------------------------------------------------------------
    // Inventory
    // ------------------------------------------------------------------

    /** 9a. Stock value per category (qty on hand x weighted-average cost). */
    public List<Map<String, Object>> inventoryValuationByCategory() {
        String sql = """
            SELECT category_name,
                   SUM(stock_qty) AS stock_qty,
                   SUM(stock_qty * avg_cost) AS stock_value
            FROM (%s) item_stock
            GROUP BY category_name
            ORDER BY stock_value DESC
            """.formatted(itemStockSql());
        return jdbcTemplate.queryForList(sql);
    }

    /** 9b. Stock value per item. */
    public List<Map<String, Object>> inventoryValuationByItem() {
        String sql = """
            SELECT code, item_name, category_name, stock_qty, avg_cost,
                   stock_qty * avg_cost AS stock_value
            FROM (%s) item_stock
            ORDER BY category_name, item_name
            """.formatted(itemStockSql());
        return jdbcTemplate.queryForList(sql);
    }

    /** 10. Items at or below their reorder level. */
    public List<Map<String, Object>> reorderReport() {
        String sql = """
            SELECT code, item_name, category_name, stock_qty, reorder_level
            FROM (%s) item_stock
            WHERE stock_qty <= reorder_level
            ORDER BY stock_qty - reorder_level
            """.formatted(itemStockSql());
        return jdbcTemplate.queryForList(sql);
    }

    /** 11. IN vs OUT movement per item over a period, slow movers last... fast movers first. */
    public List<Map<String, Object>> stockMovement(LocalDate from, LocalDate to) {
        String sql = """
            WITH movement AS (
                SELECT item_id,
                       SUM(CASE WHEN txn_type = 'IN'  THEN quantity ELSE 0 END) AS qty_in,
                       SUM(CASE WHEN txn_type = 'IN'  THEN quantity * COALESCE(unit_cost, 0) ELSE 0 END) AS value_in,
                       SUM(CASE WHEN txn_type = 'OUT' THEN quantity ELSE 0 END) AS qty_out
                FROM stock_transaction
                WHERE txn_date::date BETWEEN ? AND ?
                GROUP BY item_id
            )
            SELECT
                st.code,
                st.item_name,
                st.category_name,
                COALESCE(m.qty_in, 0)                    AS qty_in,
                COALESCE(m.value_in, 0)                  AS value_in,
                COALESCE(m.qty_out, 0)                   AS qty_out,
                COALESCE(m.qty_out, 0) * st.avg_cost     AS est_out_value,
                st.stock_qty                             AS on_hand
            FROM (%s) st
            LEFT JOIN movement m ON m.item_id = st.item_id
            ORDER BY COALESCE(m.qty_out, 0) DESC, st.item_name
            """.formatted(itemStockSql());
        return jdbcTemplate.queryForList(sql, from, to);
    }

    /**
     * Shared per-item stock position: quantity on hand and weighted-average
     * cost of all IN transactions (same basis as the inventory summary page).
     */
    private String itemStockSql() {
        return """
            SELECT
                i.item_id,
                i.code,
                i.name AS item_name,
                c.name AS category_name,
                i.reorder_level,
                COALESCE(SUM(CASE WHEN st.txn_type = 'IN' THEN st.quantity ELSE -st.quantity END), 0) AS stock_qty,
                COALESCE(
                    SUM(CASE WHEN st.txn_type = 'IN' THEN st.quantity * st.unit_cost ELSE 0 END)
                    / NULLIF(SUM(CASE WHEN st.txn_type = 'IN' THEN st.quantity ELSE 0 END), 0), 0) AS avg_cost
            FROM item i
            JOIN category c ON c.category_id = i.category_id
            LEFT JOIN stock_transaction st ON st.item_id = i.item_id
            WHERE i.active = TRUE
            GROUP BY i.item_id, i.code, i.name, c.name, i.reorder_level
            """;
    }

    // ------------------------------------------------------------------
    // Finance
    // ------------------------------------------------------------------

    /**
     * 16a. Cash flow per month/quarter: every payment record in and out,
     * regardless of method (cash, cheque, bank transfer).
     */
    public List<Map<String, Object>> cashFlow(LocalDate from, LocalDate to, String grouping) {
        String sql = """
            WITH customer_cf AS (
                SELECT
                    date_trunc('%1$s', payment_date::timestamp)::date AS period,
                    SUM(CASE direction WHEN 'IN'  THEN total_payment_amount ELSE 0 END) AS customer_receipts,
                    SUM(CASE direction WHEN 'OUT' THEN total_payment_amount ELSE 0 END) AS customer_refunds
                FROM customer_payment
                WHERE payment_date BETWEEN ? AND ?
                GROUP BY 1
            ),
            supplier_cf AS (
                SELECT
                    date_trunc('%1$s', payment_date::timestamp)::date AS period,
                    SUM(CASE direction WHEN 'OUT' THEN total_payment_amount ELSE 0 END) AS supplier_payments,
                    SUM(CASE direction WHEN 'IN'  THEN total_payment_amount ELSE 0 END) AS supplier_refunds
                FROM supplier_payment
                WHERE payment_date BETWEEN ? AND ?
                GROUP BY 1
            )
            SELECT
                COALESCE(c.period, s.period)          AS period,
                COALESCE(c.customer_receipts, 0)      AS customer_receipts,
                COALESCE(s.supplier_refunds, 0)       AS supplier_refunds,
                COALESCE(s.supplier_payments, 0)      AS supplier_payments,
                COALESCE(c.customer_refunds, 0)       AS customer_refunds,
                COALESCE(c.customer_receipts, 0) + COALESCE(s.supplier_refunds, 0)
                    - COALESCE(s.supplier_payments, 0) - COALESCE(c.customer_refunds, 0) AS net_cash_flow
            FROM customer_cf c
            FULL OUTER JOIN supplier_cf s ON s.period = c.period
            ORDER BY 1
            """.formatted(grouping);
        return jdbcTemplate.queryForList(sql, from, to, from, to);
    }

    /**
     * 16b. Gross profit per month/quarter: invoice revenue minus cost of the
     * goods shipped (sold + free issue) at weighted-average cost.
     */
    public List<Map<String, Object>> profit(LocalDate from, LocalDate to, String grouping) {
        String sql = """
            WITH avg_cost AS (
                SELECT item_id,
                       COALESCE(
                           SUM(CASE WHEN txn_type = 'IN' THEN quantity * unit_cost ELSE 0 END)
                           / NULLIF(SUM(CASE WHEN txn_type = 'IN' THEN quantity ELSE 0 END), 0), 0) AS avg_cost
                FROM stock_transaction
                GROUP BY item_id
            )
            SELECT
                date_trunc('%s', i.invoice_date::timestamp)::date AS period,
                COUNT(DISTINCT i.id) AS invoice_count,
                SUM(ii.quantity * ii.unit_price - COALESCE(ii.discount, 0)) AS revenue,
                SUM((ii.quantity + COALESCE(ii.free_qty, 0)) * COALESCE(ac.avg_cost, 0)) AS cogs,
                SUM(ii.quantity * ii.unit_price - COALESCE(ii.discount, 0))
                    - SUM((ii.quantity + COALESCE(ii.free_qty, 0)) * COALESCE(ac.avg_cost, 0)) AS gross_profit
            FROM invoice_item ii
            JOIN invoice i ON i.id = ii.invoice_id
            LEFT JOIN avg_cost ac ON ac.item_id = ii.item_id
            WHERE i.status = 'APPROVED'
              AND i.invoice_date BETWEEN ? AND ?
            GROUP BY 1
            ORDER BY 1
            """.formatted(grouping);
        return jdbcTemplate.queryForList(sql, from, to);
    }

    // ------------------------------------------------------------------
    // Sales & performance
    // ------------------------------------------------------------------

    /** 12. Sales value per month/quarter, split cash/cheque/credit, with invoice count. */
    public List<Map<String, Object>> salesSummary(LocalDate from, LocalDate to, String grouping) {
        String sql = """
            SELECT
                date_trunc('%s', i.invoice_date::timestamp)::date AS period,
                COUNT(*)       AS invoice_count,
                SUM(i.total)   AS total,
                SUM(i.cash)    AS cash,
                SUM(i.cheque)  AS cheque,
                SUM(i.credit)  AS credit
            FROM invoice i
            WHERE i.status = 'APPROVED'
              AND i.invoice_date BETWEEN ? AND ?
            GROUP BY 1
            ORDER BY 1
            """.formatted(grouping);
        return jdbcTemplate.queryForList(sql, from, to);
    }

    /** 13a. Sales per customer, ranked. */
    public List<Map<String, Object>> salesByCustomer(LocalDate from, LocalDate to) {
        String sql = """
            SELECT
                c.name         AS customer_name,
                COUNT(i.id)    AS invoice_count,
                SUM(i.total)   AS total,
                SUM(i.cash)    AS cash,
                SUM(i.cheque)  AS cheque,
                SUM(i.credit)  AS credit
            FROM invoice i
            JOIN customer c ON c.customer_id = i.customer_id
            WHERE i.status = 'APPROVED'
              AND i.invoice_date BETWEEN ? AND ?
            GROUP BY c.name
            ORDER BY total DESC
            """;
        return jdbcTemplate.queryForList(sql, from, to);
    }

    /** 13b. Best-selling items by value. */
    public List<Map<String, Object>> salesByItem(LocalDate from, LocalDate to) {
        String sql = """
            SELECT
                it.code,
                it.name AS item_name,
                SUM(ii.quantity)                    AS qty,
                SUM(COALESCE(ii.free_qty, 0))       AS free_qty,
                SUM(ii.quantity * ii.unit_price - COALESCE(ii.discount, 0)) AS value
            FROM invoice_item ii
            JOIN invoice i ON i.id = ii.invoice_id
            JOIN item it   ON it.item_id = ii.item_id
            WHERE i.status = 'APPROVED'
              AND i.invoice_date BETWEEN ? AND ?
            GROUP BY it.code, it.name
            ORDER BY value DESC
            LIMIT 20
            """;
        return jdbcTemplate.queryForList(sql, from, to);
    }

    /** 14. Debtor outstanding and aging per customer. */
    public List<Map<String, Object>> debtorAging() {
        String sql = """
            SELECT
                ca.customer_name,
                ob.open_invoice_count,
                ca.total_outstanding,
                ca.current_amount,
                ca.overdue_1_30, ca.overdue_31_60, ca.overdue_61_90, ca.overdue_90_plus
            FROM customer_aging ca
            LEFT JOIN outstanding_balance_per_customer ob ON ob.customer_id = ca.customer_id
            ORDER BY ca.total_outstanding DESC
            """;
        return jdbcTemplate.queryForList(sql);
    }

    /** 15. Sales made and money collected per salesman. */
    public List<Map<String, Object>> salesmanPerformance(LocalDate from, LocalDate to) {
        String sql = """
            WITH sales AS (
                SELECT employee_id, COUNT(*) AS invoice_count, SUM(total) AS sales_value
                FROM invoice
                WHERE status = 'APPROVED' AND invoice_date BETWEEN ? AND ?
                GROUP BY employee_id
            ),
            collections AS (
                SELECT collected_by,
                       SUM(CASE WHEN payment_method = 'CASH' AND direction = 'IN' THEN total_payment_amount
                                WHEN payment_method = 'CASH' THEN -total_payment_amount ELSE 0 END) AS cash_collected,
                       SUM(CASE direction WHEN 'IN' THEN total_payment_amount ELSE -total_payment_amount END) AS total_collected
                FROM customer_payment
                WHERE collected_by IS NOT NULL AND payment_date BETWEEN ? AND ?
                GROUP BY collected_by
            )
            SELECT
                e.known_name AS employee_name,
                COALESCE(s.invoice_count, 0)   AS invoice_count,
                COALESCE(s.sales_value, 0)     AS sales_value,
                COALESCE(c.cash_collected, 0)  AS cash_collected,
                COALESCE(c.total_collected, 0) AS total_collected
            FROM employee e
            LEFT JOIN sales s       ON s.employee_id = e.id
            LEFT JOIN collections c ON c.collected_by = e.id
            WHERE s.employee_id IS NOT NULL OR c.collected_by IS NOT NULL
            ORDER BY sales_value DESC
            """;
        return jdbcTemplate.queryForList(sql, from, to, from, to);
    }
}
