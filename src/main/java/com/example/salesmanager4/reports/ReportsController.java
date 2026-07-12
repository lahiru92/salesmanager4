package com.example.salesmanager4.reports;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.salesmanager4.reports.table.ReportView;
import com.example.salesmanager4.util.Breadcrumb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportsController {

    private final ReportBuilderService reportBuilderService;
    private final ReportPdfService reportPdfService;
    private final ReportCsvService reportCsvService;

    @GetMapping
    public String hub(Model model) {

        Map<String, List<ReportType>> groups = new LinkedHashMap<>();
        for (ReportType type : ReportType.values()) {
            groups.computeIfAbsent(type.getGroup(), g -> new java.util.ArrayList<>()).add(type);
        }

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Reports", null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("groups", groups);
        return "reports/hub::content";
    }

    @GetMapping("/{key}")
    public String view(@PathVariable String key,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String grouping,
            @RequestParam(required = false) Integer horizon,
            Model model) {

        ReportType type = ReportType.fromKey(key);
        ReportParams params = ReportParams.of(from, to, grouping, horizon);
        ReportView view = reportBuilderService.build(type, params);

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Reports", "/reports"),
            new Breadcrumb(type.getTitle(), null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("type", type);
        model.addAttribute("params", params);
        model.addAttribute("view", view);
        model.addAttribute("queryString", params.queryString());
        return "reports/view::content";
    }

    @GetMapping("/{key}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable String key,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String grouping,
            @RequestParam(required = false) Integer horizon) {

        ReportType type = ReportType.fromKey(key);
        ReportParams params = ReportParams.of(from, to, grouping, horizon);
        ReportView view = reportBuilderService.build(type, params);

        byte[] pdf = reportPdfService.render(view);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName(type, "pdf") + "\"")
                .body(pdf);
    }

    @GetMapping("/{key}/csv")
    public ResponseEntity<byte[]> csv(@PathVariable String key,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String grouping,
            @RequestParam(required = false) Integer horizon) {

        ReportType type = ReportType.fromKey(key);
        ReportParams params = ReportParams.of(from, to, grouping, horizon);
        ReportView view = reportBuilderService.build(type, params);

        byte[] csv = reportCsvService.render(view);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName(type, "csv") + "\"")
                .body(csv);
    }

    private String fileName(ReportType type, String extension) {
        return type.getKey() + "-" + LocalDate.now() + "." + extension;
    }
}
