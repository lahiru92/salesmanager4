package com.example.salesmanager4.reports.table;

import java.util.List;

/**
 * A fully built report, ready to be rendered to screen, PDF or CSV.
 */
public record ReportView(String title, String periodLabel, List<ReportTable> tables) {
}
