package com.example.salesmanager4.reports;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.salesmanager4.reports.table.ReportColumn;
import com.example.salesmanager4.reports.table.ReportTable;
import com.example.salesmanager4.reports.table.ReportView;

@Service
public class ReportCsvService {

    public byte[] render(ReportView view) {

        StringBuilder csv = new StringBuilder();
        csv.append('﻿'); // BOM so Excel opens it as UTF-8
        csv.append(escape(view.title())).append("\r\n");
        csv.append(escape(view.periodLabel())).append("\r\n\r\n");

        for (ReportTable table : view.tables()) {
            if (table.title() != null) {
                csv.append(escape(table.title())).append("\r\n");
            }

            csv.append(String.join(",", table.columns().stream().map(ReportColumn::header).map(this::escape).toList()));
            csv.append("\r\n");

            for (List<String> row : table.rows()) {
                csv.append(String.join(",", row.stream().map(this::escape).toList())).append("\r\n");
            }

            if (table.totalsRow() != null) {
                csv.append(String.join(",", table.totalsRow().stream().map(this::escape).toList())).append("\r\n");
            }

            csv.append("\r\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
