package com.example.salesmanager4.reports;

/**
 * Catalogue of all available reports. The flags drive which parameter
 * controls (date range, month/quarter grouping, due horizon) the generic
 * report view renders for each report.
 */
public enum ReportType {

    // Purchasing / procurement
    PURCHASES_SUMMARY("purchases-summary", "Purchases Summary", "Purchasing", true, true, false),
    PURCHASES_BY_SUPPLIER("purchases-by-supplier", "Purchases by Supplier", "Purchasing", true, false, false),
    PURCHASES_BY_ITEM("purchases-by-item", "Purchases by Category & Item", "Purchasing", true, false, false),
    REJECTED_GOODS("rejected-goods", "Rejected / Returned Goods", "Purchasing", true, false, false),

    // Creditors / payables
    SUPPLIER_OUTSTANDING("supplier-outstanding", "Supplier Outstanding Balances", "Creditors / Payables", false, false, false),
    SUPPLIER_AGING("supplier-aging", "Supplier Aging Analysis", "Creditors / Payables", false, false, false),
    SUPPLIER_PAYMENTS("supplier-payments", "Payments to Suppliers", "Creditors / Payables", true, true, false),
    PAYMENTS_DUE("payments-due", "Upcoming Payments Due", "Creditors / Payables", false, false, true),

    // Inventory
    INVENTORY_VALUATION("inventory-valuation", "Inventory Valuation", "Inventory", false, false, false),
    REORDER("reorder", "Reorder / Low Stock", "Inventory", false, false, false),
    STOCK_MOVEMENT("stock-movement", "Stock Movement (Fast / Slow Movers)", "Inventory", true, false, false),

    // Finance
    CASH_FLOW_PROFIT("cash-flow-profit", "Cash Flow & Profit", "Finance", true, true, false),

    // Sales & performance
    SALES_SUMMARY("sales-summary", "Sales Summary", "Sales & Performance", true, true, false),
    SALES_BY_CUSTOMER("sales-by-customer", "Sales by Customer & Item", "Sales & Performance", true, false, false),
    DEBTOR_AGING("debtor-outstanding", "Debtor Outstanding & Aging", "Sales & Performance", false, false, false),
    SALESMAN_PERFORMANCE("salesman-performance", "Salesman Performance", "Sales & Performance", true, false, false);

    private final String key;
    private final String title;
    private final String group;
    private final boolean hasDateRange;
    private final boolean hasGrouping;
    private final boolean hasHorizon;

    ReportType(String key, String title, String group, boolean hasDateRange, boolean hasGrouping, boolean hasHorizon) {
        this.key = key;
        this.title = title;
        this.group = group;
        this.hasDateRange = hasDateRange;
        this.hasGrouping = hasGrouping;
        this.hasHorizon = hasHorizon;
    }

    public String getKey() { return key; }
    public String getTitle() { return title; }
    public String getGroup() { return group; }
    public boolean isHasDateRange() { return hasDateRange; }
    public boolean isHasGrouping() { return hasGrouping; }
    public boolean isHasHorizon() { return hasHorizon; }

    public static ReportType fromKey(String key) {
        for (ReportType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown report: " + key);
    }
}
