package com.example.salesmanager4.reports;

import java.time.LocalDate;

/**
 * Normalised report parameters with defaults:
 * last three months, monthly grouping, 30-day due horizon.
 */
public record ReportParams(LocalDate from, LocalDate to, String grouping, int horizon) {

    public static ReportParams of(LocalDate from, LocalDate to, String grouping, Integer horizon) {

        LocalDate effectiveTo = to != null ? to : LocalDate.now();
        LocalDate effectiveFrom = from != null ? from : LocalDate.now().withDayOfMonth(1).minusMonths(2);

        // whitelisted: interpolated into date_trunc()
        String effectiveGrouping = "quarter".equals(grouping) ? "quarter" : "month";

        int effectiveHorizon = horizon != null && horizon > 0 ? horizon : 30;

        return new ReportParams(effectiveFrom, effectiveTo, effectiveGrouping, effectiveHorizon);
    }

    public String label(ReportType type) {
        if (type.isHasDateRange()) {
            return from + " to " + to;
        }
        if (type.isHasHorizon()) {
            return "Due within " + horizon + " days";
        }
        return "As at " + LocalDate.now();
    }

    public String queryString() {
        return "from=" + from + "&to=" + to + "&grouping=" + grouping + "&horizon=" + horizon;
    }
}
