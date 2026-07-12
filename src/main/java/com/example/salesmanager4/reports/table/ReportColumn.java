package com.example.salesmanager4.reports.table;

public record ReportColumn(String header, boolean numeric) {

    public static ReportColumn text(String header) {
        return new ReportColumn(header, false);
    }

    public static ReportColumn number(String header) {
        return new ReportColumn(header, true);
    }
}
