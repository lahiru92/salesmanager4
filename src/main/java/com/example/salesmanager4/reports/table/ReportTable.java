package com.example.salesmanager4.reports.table;

import java.util.List;

/**
 * One table of an already-formatted report: every cell is a display string.
 * totalsRow is optional (null when the table has no totals line).
 */
public record ReportTable(String title, List<ReportColumn> columns, List<List<String>> rows, List<String> totalsRow) {
}
