package com.example.salesmanager4.reports;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.salesmanager4.reports.table.ReportColumn;
import com.example.salesmanager4.reports.table.ReportTable;
import com.example.salesmanager4.reports.table.ReportView;

/**
 * Renders a sample ReportView through the real PDF and CSV pipelines
 * (no database or Spring context required).
 */
@DisplayName("Report rendering")
class ReportRenderTest {

    private ReportView sampleView() {
        ReportTable table = new ReportTable(
                "By category",
                List.of(ReportColumn.text("Category"), ReportColumn.number("Quantity"), ReportColumn.number("Value")),
                List.of(
                        List.of("Beverages", "120.00", "45,600.00"),
                        List.of("Snacks, \"bulk\"", "80.00", "12,000.00")),
                List.of("Total", "200.00", "57,600.00"));

        return new ReportView("Purchases Summary", "2026-05-01 to 2026-07-11", List.of(table));
    }

    @Test
    @DisplayName("PDF renderer produces a valid PDF document")
    void rendersPdf() {
        byte[] pdf = new ReportPdfService().render(sampleView());

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("CSV renderer escapes fields and includes headers, rows and totals")
    void rendersCsv() {
        byte[] csv = new ReportCsvService().render(sampleView());
        String content = new String(csv, StandardCharsets.UTF_8);

        assertThat(content).contains("Purchases Summary");
        assertThat(content).contains("Category,Quantity,Value");
        assertThat(content).contains("Beverages,120.00,\"45,600.00\"");
        assertThat(content).contains("\"Snacks, \"\"bulk\"\"\"");
        assertThat(content).contains("Total,200.00,\"57,600.00\"");
    }
}
