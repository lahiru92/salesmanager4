package com.example.salesmanager4.reports;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.salesmanager4.reports.table.ReportColumn;
import com.example.salesmanager4.reports.table.ReportTable;
import com.example.salesmanager4.reports.table.ReportView;

import lombok.RequiredArgsConstructor;

/**
 * Builds a ready-to-render ReportView (formatted display strings) for each
 * report type. The same view is rendered to the page, to PDF and to CSV.
 */
@Service
@RequiredArgsConstructor
public class ReportBuilderService {

    private final ReportQueryRepository repo;

    public ReportView build(ReportType type, ReportParams params) {

        List<ReportTable> tables = switch (type) {
            case PURCHASES_SUMMARY -> purchasesSummary(params);
            case PURCHASES_BY_SUPPLIER -> purchasesBySupplier(params);
            case PURCHASES_BY_ITEM -> purchasesByItem(params);
            case REJECTED_GOODS -> rejectedGoods(params);
            case SUPPLIER_OUTSTANDING -> supplierOutstanding();
            case SUPPLIER_AGING -> supplierAging();
            case SUPPLIER_PAYMENTS -> supplierPayments(params);
            case PAYMENTS_DUE -> paymentsDue(params);
            case INVENTORY_VALUATION -> inventoryValuation();
            case REORDER -> reorder();
            case STOCK_MOVEMENT -> stockMovement(params);
            case CASH_FLOW_PROFIT -> cashFlowProfit(params);
            case SALES_SUMMARY -> salesSummary(params);
            case SALES_BY_CUSTOMER -> salesByCustomer(params);
            case DEBTOR_AGING -> debtorAging();
            case SALESMAN_PERFORMANCE -> salesmanPerformance(params);
                default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };

        return new ReportView(type.getTitle(), params.label(type), tables);
    }

    // ------------------------------------------------------------------
    // Purchasing
    // ------------------------------------------------------------------

    private List<ReportTable> purchasesSummary(ReportParams p) {
        List<Map<String, Object>> rows = repo.purchasesSummary(p.from(), p.to(), p.grouping());

        List<ReportColumn> columns = List.of(
                ReportColumn.text("Period"), ReportColumn.number("GRNs"), ReportColumn.number("Total"),
                ReportColumn.number("Cash"), ReportColumn.number("Cheque"), ReportColumn.number("Credit"));

        List<List<String>> data = rows.stream().map(r -> List.of(
                period(r.get("period"), p.grouping()), integer(r.get("grn_count")), money(r.get("total")),
                money(r.get("cash")), money(r.get("cheque")), money(r.get("credit")))).toList();

        List<String> totals = List.of("Total", sumInt(rows, "grn_count"), sumMoney(rows, "total"),
                sumMoney(rows, "cash"), sumMoney(rows, "cheque"), sumMoney(rows, "credit"));

        return List.of(new ReportTable(null, columns, data, totals));
    }

    private List<ReportTable> purchasesBySupplier(ReportParams p) {
        List<Map<String, Object>> rows = repo.purchasesBySupplier(p.from(), p.to());

        List<ReportColumn> columns = List.of(
                ReportColumn.text("Supplier"), ReportColumn.number("GRNs"), ReportColumn.number("Purchased"),
                ReportColumn.number("Paid"), ReportColumn.number("Outstanding"));

        List<List<String>> data = rows.stream().map(r -> List.of(
                str(r.get("supplier_name")), integer(r.get("grn_count")), money(r.get("total_purchased")),
                money(r.get("total_paid")), money(r.get("outstanding")))).toList();

        List<String> totals = List.of("Total", sumInt(rows, "grn_count"), sumMoney(rows, "total_purchased"),
                sumMoney(rows, "total_paid"), sumMoney(rows, "outstanding"));

        return List.of(new ReportTable(null, columns, data, totals));
    }

    private List<ReportTable> purchasesByItem(ReportParams p) {
        List<Map<String, Object>> byCategory = repo.purchasesByCategory(p.from(), p.to());
        List<Map<String, Object>> topItems = repo.purchasesTopItems(p.from(), p.to());

        ReportTable categoryTable = new ReportTable("By category",
                List.of(ReportColumn.text("Category"), ReportColumn.number("Quantity"), ReportColumn.number("Value")),
                byCategory.stream().map(r -> List.of(
                        str(r.get("category_name")), qty(r.get("qty")), money(r.get("value")))).toList(),
                List.of("Total", qty(sum(byCategory, "qty")), money(sum(byCategory, "value"))));

        ReportTable itemTable = new ReportTable("Top items (by value)",
                List.of(ReportColumn.text("Code"), ReportColumn.text("Item"), ReportColumn.text("Category"),
                        ReportColumn.number("Quantity"), ReportColumn.number("Value")),
                topItems.stream().map(r -> List.of(
                        str(r.get("code")), str(r.get("item_name")), str(r.get("category_name")),
                        qty(r.get("qty")), money(r.get("value")))).toList(),
                null);

        return List.of(categoryTable, itemTable);
    }

    private List<ReportTable> rejectedGoods(ReportParams p) {
        List<Map<String, Object>> rows = repo.rejectedGoods(p.from(), p.to());

        List<ReportColumn> columns = List.of(
                ReportColumn.text("Supplier"), ReportColumn.text("Code"), ReportColumn.text("Item"),
                ReportColumn.number("GRNs"), ReportColumn.number("Rejected Qty"), ReportColumn.number("Rejected Value"));

        List<List<String>> data = rows.stream().map(r -> List.of(
                str(r.get("supplier_name")), str(r.get("code")), str(r.get("item_name")),
                integer(r.get("grn_count")), qty(r.get("rejected_qty")), money(r.get("rejected_value")))).toList();

        List<String> totals = List.of("Total", "", "", sumInt(rows, "grn_count"),
                qty(sum(rows, "rejected_qty")), sumMoney(rows, "rejected_value"));

        return List.of(new ReportTable(null, columns, data, totals));
    }

    // ------------------------------------------------------------------
    // Creditors / payables
    // ------------------------------------------------------------------

    private List<ReportTable> supplierOutstanding() {
        List<Map<String, Object>> rows = repo.supplierOutstanding();

        List<ReportColumn> columns = List.of(
                ReportColumn.text("Supplier"), ReportColumn.number("Open GRNs"), ReportColumn.number("Credit Issued"),
                ReportColumn.number("Paid"), ReportColumn.number("Outstanding"));

        List<List<String>> data = rows.stream().map(r -> List.of(
                str(r.get("supplier_name")), integer(r.get("open_grn_count")), money(r.get("total_credit_issued")),
                money(r.get("total_paid")), money(r.get("total_outstanding")))).toList();

        List<String> totals = List.of("Total", sumInt(rows, "open_grn_count"), sumMoney(rows, "total_credit_issued"),
                sumMoney(rows, "total_paid"), sumMoney(rows, "total_outstanding"));

        return List.of(new ReportTable(null, columns, data, totals));
    }

    private List<ReportTable> supplierAging() {
        List<Map<String, Object>> rows = repo.supplierAging();
        return List.of(agingTable(rows, "supplier_name", "Supplier", null));
    }

    private List<ReportTable> supplierPayments(ReportParams p) {
        List<Map<String, Object>> rows = repo.supplierPayments(p.from(), p.to(), p.grouping());

        List<ReportColumn> columns = List.of(
                ReportColumn.text("Period"), ReportColumn.number("Payments"), ReportColumn.number("Cash"),
                ReportColumn.number("Cheque"), ReportColumn.number("Bank Transfer"), ReportColumn.number("Total"));

        List<List<String>> data = rows.stream().map(r -> List.of(
                period(r.get("period"), p.grouping()), integer(r.get("payment_count")), money(r.get("cash")),
                money(r.get("cheque")), money(r.get("bank_transfer")), money(r.get("total")))).toList();

        List<String> totals = List.of("Total", sumInt(rows, "payment_count"), sumMoney(rows, "cash"),
                sumMoney(rows, "cheque"), sumMoney(rows, "bank_transfer"), sumMoney(rows, "total"));

        return List.of(new ReportTable(null, columns, data, totals));
    }

    private List<ReportTable> paymentsDue(ReportParams p) {
        List<Map<String, Object>> rows = repo.paymentsDue(p.horizon());

        List<ReportColumn> columns = List.of(
                ReportColumn.text("Supplier"), ReportColumn.number("GRN #"), ReportColumn.text("Received"),
                ReportColumn.text("Due Date"), ReportColumn.number("Days Left"), ReportColumn.number("Outstanding"));

        List<List<String>> data = rows.stream().map(r -> List.of(
                str(r.get("supplier_name")), integer(r.get("grn_id")), date(r.get("received_date")),
                date(r.get("credit_due")), integer(r.get("days_left")), money(r.get("outstanding_balance")))).toList();

        List<String> totals = List.of("Total", "", "", "", "", sumMoney(rows, "outstanding_balance"));

        return List.of(new ReportTable(null, columns, data, totals));
    }

    // ------------------------------------------------------------------
    // Inventory
    // ------------------------------------------------------------------

    private List<ReportTable> inventoryValuation() {
        List<Map<String, Object>> byCategory = repo.inventoryValuationByCategory();
        List<Map<String, Object>> byItem = repo.inventoryValuationByItem();

        ReportTable categoryTable = new ReportTable("By category",
                List.of(ReportColumn.text("Category"), ReportColumn.number("Quantity"), ReportColumn.number("Stock Value")),
                byCategory.stream().map(r -> List.of(
                        str(r.get("category_name")), qty(r.get("stock_qty")), money(r.get("stock_value")))).toList(),
                List.of("Total", qty(sum(byCategory, "stock_qty")), money(sum(byCategory, "stock_value"))));

        ReportTable itemTable = new ReportTable("By item",
                List.of(ReportColumn.text("Code"), ReportColumn.text("Item"), ReportColumn.text("Category"),
                        ReportColumn.number("Quantity"), ReportColumn.number("Avg Cost"), ReportColumn.number("Stock Value")),
                byItem.stream().map(r -> List.of(
                        str(r.get("code")), str(r.get("item_name")), str(r.get("category_name")),
                        qty(r.get("stock_qty")), money(r.get("avg_cost")), money(r.get("stock_value")))).toList(),
                List.of("Total", "", "", "", "", sumMoney(byItem, "stock_value")));

        return List.of(categoryTable, itemTable);
    }

    private List<ReportTable> reorder() {
        List<Map<String, Object>> rows = repo.reorderReport();

        List<ReportColumn> columns = List.of(
                ReportColumn.text("Code"), ReportColumn.text("Item"), ReportColumn.text("Category"),
                ReportColumn.number("On Hand"), ReportColumn.number("Reorder Level"), ReportColumn.number("Shortfall"));

        List<List<String>> data = rows.stream().map(r -> {
            BigDecimal onHand = decimal(r.get("stock_qty"));
            BigDecimal reorderLevel = decimal(r.get("reorder_level"));
            return List.of(str(r.get("code")), str(r.get("item_name")), str(r.get("category_name")),
                    qty(onHand), qty(reorderLevel), qty(reorderLevel.subtract(onHand)));
        }).toList();

        return List.of(new ReportTable(null, columns, data, null));
    }

    private List<ReportTable> stockMovement(ReportParams p) {
        List<Map<String, Object>> rows = repo.stockMovement(p.from(), p.to());

        List<ReportColumn> columns = List.of(
                ReportColumn.text("Code"), ReportColumn.text("Item"), ReportColumn.text("Category"),
                ReportColumn.number("Qty In"), ReportColumn.number("Value In"), ReportColumn.number("Qty Out"),
                ReportColumn.number("Est. Value Out"), ReportColumn.number("On Hand"), ReportColumn.text("Movement"));

        List<List<String>> data = rows.stream().map(r -> {
            BigDecimal qtyOut = decimal(r.get("qty_out"));
            BigDecimal onHand = decimal(r.get("on_hand"));
            String movement = qtyOut.signum() > 0 ? "MOVING"
                    : onHand.signum() > 0 ? "DEAD STOCK" : "-";
            return List.of(str(r.get("code")), str(r.get("item_name")), str(r.get("category_name")),
                    qty(r.get("qty_in")), money(r.get("value_in")), qty(qtyOut),
                    money(r.get("est_out_value")), qty(onHand), movement);
        }).toList();

        return List.of(new ReportTable(null, columns, data, null));
    }

    // ------------------------------------------------------------------
    // Finance
    // ------------------------------------------------------------------

    private List<ReportTable> cashFlowProfit(ReportParams p) {
        List<Map<String, Object>> cashFlow = repo.cashFlow(p.from(), p.to(), p.grouping());
        List<Map<String, Object>> profit = repo.profit(p.from(), p.to(), p.grouping());

        ReportTable cashFlowTable = new ReportTable("Cash flow (all payment methods, in vs out)",
                List.of(ReportColumn.text("Period"), ReportColumn.number("Customer Receipts"),
                        ReportColumn.number("Supplier Refunds"), ReportColumn.number("Supplier Payments"),
                        ReportColumn.number("Customer Refunds"), ReportColumn.number("Net Cash Flow")),
                cashFlow.stream().map(r -> List.of(
                        period(r.get("period"), p.grouping()), money(r.get("customer_receipts")),
                        money(r.get("supplier_refunds")), money(r.get("supplier_payments")),
                        money(r.get("customer_refunds")), money(r.get("net_cash_flow")))).toList(),
                List.of("Total", sumMoney(cashFlow, "customer_receipts"), sumMoney(cashFlow, "supplier_refunds"),
                        sumMoney(cashFlow, "supplier_payments"), sumMoney(cashFlow, "customer_refunds"),
                        sumMoney(cashFlow, "net_cash_flow")));

        ReportTable profitTable = new ReportTable(
                "Gross profit (revenue minus goods shipped at weighted-average cost; expenses not tracked)",
                List.of(ReportColumn.text("Period"), ReportColumn.number("Invoices"), ReportColumn.number("Revenue"),
                        ReportColumn.number("Cost of Goods"), ReportColumn.number("Gross Profit"),
                        ReportColumn.number("Margin %")),
                profit.stream().map(r -> List.of(
                        period(r.get("period"), p.grouping()), integer(r.get("invoice_count")),
                        money(r.get("revenue")), money(r.get("cogs")), money(r.get("gross_profit")),
                        percent(decimal(r.get("gross_profit")), decimal(r.get("revenue"))))).toList(),
                List.of("Total", sumInt(profit, "invoice_count"), sumMoney(profit, "revenue"),
                        sumMoney(profit, "cogs"), sumMoney(profit, "gross_profit"),
                        percent(sum(profit, "gross_profit"), sum(profit, "revenue"))));

        return List.of(cashFlowTable, profitTable);
    }

    // ------------------------------------------------------------------
    // Sales & performance
    // ------------------------------------------------------------------

    private List<ReportTable> salesSummary(ReportParams p) {
        List<Map<String, Object>> rows = repo.salesSummary(p.from(), p.to(), p.grouping());

        List<ReportColumn> columns = List.of(
                ReportColumn.text("Period"), ReportColumn.number("Invoices"), ReportColumn.number("Total"),
                ReportColumn.number("Cash"), ReportColumn.number("Cheque"), ReportColumn.number("Credit"));

        List<List<String>> data = rows.stream().map(r -> List.of(
                period(r.get("period"), p.grouping()), integer(r.get("invoice_count")), money(r.get("total")),
                money(r.get("cash")), money(r.get("cheque")), money(r.get("credit")))).toList();

        List<String> totals = List.of("Total", sumInt(rows, "invoice_count"), sumMoney(rows, "total"),
                sumMoney(rows, "cash"), sumMoney(rows, "cheque"), sumMoney(rows, "credit"));

        return List.of(new ReportTable(null, columns, data, totals));
    }

    private List<ReportTable> salesByCustomer(ReportParams p) {
        List<Map<String, Object>> byCustomer = repo.salesByCustomer(p.from(), p.to());
        List<Map<String, Object>> byItem = repo.salesByItem(p.from(), p.to());

        ReportTable customerTable = new ReportTable("Top customers",
                List.of(ReportColumn.text("Customer"), ReportColumn.number("Invoices"), ReportColumn.number("Total"),
                        ReportColumn.number("Cash"), ReportColumn.number("Cheque"), ReportColumn.number("Credit")),
                byCustomer.stream().map(r -> List.of(
                        str(r.get("customer_name")), integer(r.get("invoice_count")), money(r.get("total")),
                        money(r.get("cash")), money(r.get("cheque")), money(r.get("credit")))).toList(),
                List.of("Total", sumInt(byCustomer, "invoice_count"), sumMoney(byCustomer, "total"),
                        sumMoney(byCustomer, "cash"), sumMoney(byCustomer, "cheque"), sumMoney(byCustomer, "credit")));

        ReportTable itemTable = new ReportTable("Best-selling items (by value)",
                List.of(ReportColumn.text("Code"), ReportColumn.text("Item"), ReportColumn.number("Qty Sold"),
                        ReportColumn.number("Free Qty"), ReportColumn.number("Value")),
                byItem.stream().map(r -> List.of(
                        str(r.get("code")), str(r.get("item_name")), qty(r.get("qty")),
                        qty(r.get("free_qty")), money(r.get("value")))).toList(),
                null);

        return List.of(customerTable, itemTable);
    }

    private List<ReportTable> debtorAging() {
        List<Map<String, Object>> rows = repo.debtorAging();
        return List.of(agingTable(rows, "customer_name", "Customer", "open_invoice_count"));
    }

    private List<ReportTable> salesmanPerformance(ReportParams p) {
        List<Map<String, Object>> rows = repo.salesmanPerformance(p.from(), p.to());

        List<ReportColumn> columns = List.of(
                ReportColumn.text("Salesman"), ReportColumn.number("Invoices"), ReportColumn.number("Sales Value"),
                ReportColumn.number("Cash Collected"), ReportColumn.number("Total Collected"));

        List<List<String>> data = rows.stream().map(r -> List.of(
                str(r.get("employee_name")), integer(r.get("invoice_count")), money(r.get("sales_value")),
                money(r.get("cash_collected")), money(r.get("total_collected")))).toList();

        List<String> totals = List.of("Total", sumInt(rows, "invoice_count"), sumMoney(rows, "sales_value"),
                sumMoney(rows, "cash_collected"), sumMoney(rows, "total_collected"));

        return List.of(new ReportTable(null, columns, data, totals));
    }

    /** Shared aging layout used by both the supplier and debtor aging reports. */
    private ReportTable agingTable(List<Map<String, Object>> rows, String nameColumn, String nameHeader, String countColumn) {

        List<ReportColumn> columns = new ArrayList<>();
        columns.add(ReportColumn.text(nameHeader));
        if (countColumn != null) {
            columns.add(ReportColumn.number("Open Docs"));
        }
        columns.addAll(List.of(ReportColumn.number("Outstanding"), ReportColumn.number("Current"),
                ReportColumn.number("1-30"), ReportColumn.number("31-60"),
                ReportColumn.number("61-90"), ReportColumn.number("90+")));

        List<List<String>> data = rows.stream().map(r -> {
            List<String> row = new ArrayList<>();
            row.add(str(r.get(nameColumn)));
            if (countColumn != null) {
                row.add(integer(r.get(countColumn)));
            }
            row.addAll(List.of(money(r.get("total_outstanding")), money(r.get("current_amount")),
                    money(r.get("overdue_1_30")), money(r.get("overdue_31_60")),
                    money(r.get("overdue_61_90")), money(r.get("overdue_90_plus"))));
            return row;
        }).map(List::copyOf).toList();

        List<String> totals = new ArrayList<>();
        totals.add("Total");
        if (countColumn != null) {
            totals.add(sumInt(rows, countColumn));
        }
        totals.addAll(List.of(sumMoney(rows, "total_outstanding"), sumMoney(rows, "current_amount"),
                sumMoney(rows, "overdue_1_30"), sumMoney(rows, "overdue_31_60"),
                sumMoney(rows, "overdue_61_90"), sumMoney(rows, "overdue_90_plus")));

        return new ReportTable(null, columns, data, List.copyOf(totals));
    }

    // ------------------------------------------------------------------
    // Formatting helpers
    // ------------------------------------------------------------------

    private String str(Object value) {
        return value != null ? value.toString() : "";
    }

    private String money(Object value) {
        return String.format("%,.2f", decimal(value));
    }

    private String qty(Object value) {
        return String.format("%,.2f", decimal(value));
    }

    private String integer(Object value) {
        return value instanceof Number n ? String.valueOf(n.longValue()) : "0";
    }

    private String date(Object value) {
        return value != null ? value.toString() : "";
    }

    private String period(Object value, String grouping) {
        if (!(value instanceof Date sqlDate)) {
            return str(value);
        }
        LocalDate date = sqlDate.toLocalDate();
        if ("quarter".equals(grouping)) {
            return date.getYear() + "-Q" + ((date.getMonthValue() - 1) / 3 + 1);
        }
        return String.format("%d-%02d", date.getYear(), date.getMonthValue());
    }

    /** Gross margin as "12.3%"; blank when there is no revenue to divide by. */
    private String percent(BigDecimal part, BigDecimal whole) {
        if (whole == null || whole.signum() == 0) {
            return "";
        }
        BigDecimal ratio = part.multiply(BigDecimal.valueOf(100))
                .divide(whole, 1, java.math.RoundingMode.HALF_UP);
        return ratio + "%";
    }

    private BigDecimal decimal(Object value) {
        if (value instanceof BigDecimal b) {
            return b;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal sum(List<Map<String, Object>> rows, String column) {
        return rows.stream().map(r -> decimal(r.get(column))).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String sumMoney(List<Map<String, Object>> rows, String column) {
        return money(sum(rows, column));
    }

    private String sumInt(List<Map<String, Object>> rows, String column) {
        long total = rows.stream()
                .map(r -> r.get(column))
                .filter(v -> v instanceof Number)
                .mapToLong(v -> ((Number) v).longValue())
                .sum();
        return String.valueOf(total);
    }
}
