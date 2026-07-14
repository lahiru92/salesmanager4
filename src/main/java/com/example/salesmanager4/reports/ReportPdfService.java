package com.example.salesmanager4.reports;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import com.example.salesmanager4.reports.table.ReportView;

/**
 * Renders a ReportView to PDF by processing the reports/pdf Thymeleaf
 * template through openhtmltopdf.
 *
 * Uses its own XML-mode template engine (not the Spring HTML5 one) because
 * openhtmltopdf requires well-formed XML input.
 */
@Service
public class ReportPdfService {

    private final SpringTemplateEngine templateEngine;

    public ReportPdfService() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.XML);
        resolver.setCharacterEncoding("UTF-8");

        this.templateEngine = new SpringTemplateEngine();
        this.templateEngine.setTemplateResolver(resolver);
    }

    public byte[] render(ReportView view) {

        Context context = new Context();
        context.setVariable("view", view);
        context.setVariable("generatedAt",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        String html = templateEngine.process("reports/pdf", context);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to render PDF for report: " + view.title(), e);
        }
    }
}
