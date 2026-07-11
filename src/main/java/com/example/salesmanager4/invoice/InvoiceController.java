package com.example.salesmanager4.invoice;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.salesmanager4.customers.CustomerService;
import com.example.salesmanager4.util.Breadcrumb;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final CustomerService customerService;

    @GetMapping
    public String list(InvoiceListRequestDto requestDto,
        @PageableDefault(page=0, size=10, sort="id", direction = Sort.Direction.DESC) Pageable pageable,
        Model model,
        HttpServletRequest req) {

        Page<InvoiceListResponseDto> invoices = invoiceService.listInvoices(requestDto, pageable);

        if ("invoice-table".equals(req.getHeader("Hx-Target"))) {
            model.addAttribute("invoices", invoices);
            return "invoice/list::invoice-table";
        }

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Invoices", null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("invoices", invoices);
        return "invoice/list::content";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {

        InvoiceRequestDto invoice = invoiceService.findRequestDtoById(id);

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Invoices", "/invoices"),
            new Breadcrumb("Invoice #" + invoice.getId(), null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("invoice", invoice);
        model.addAttribute("mode", "view");

        return "invoice/form::content";
    }

    @GetMapping("/{id}/edit")
    public String editView(@PathVariable Long id, Model model) {

        InvoiceRequestDto invoice = invoiceService.findRequestDtoById(id);

        String mode;
        if ("DRAFT".equals(invoice.getStatus())) {
            mode = "edit";
        } else {
            mode = "view";
        }

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Invoices", "/invoices"),
            new Breadcrumb("Edit Invoice", null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("invoice", invoice);
        model.addAttribute("mode", mode);

        return "invoice/form::content";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Invoices", "/invoices"),
            new Breadcrumb("New Invoice", null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("invoice", new InvoiceRequestDto());
        model.addAttribute("mode", "create");

        return "invoice/form::content";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("invoice") InvoiceRequestDto invoiceRequest, BindingResult bindingResult, Model model) {
        log.info("Received Invoice Header: {}", invoiceRequest);

        if (!invoiceRequest.isBalanced()) {
            bindingResult.reject("payments.mismatch","Paid amounts not balanced with the total.");
        }

        if (bindingResult.hasErrors()) {
            log.error("Validation errors: {}", bindingResult.getAllErrors());

            List<Breadcrumb> breadcrumbs = List.of(
                new Breadcrumb("Home", "/"),
                new Breadcrumb("Invoices", "/invoices"),
                new Breadcrumb("New Invoice", null)
            );

            model.addAttribute("breadcrumbs", breadcrumbs);
            model.addAttribute("mode", "create");

            if (invoiceRequest.getCustomerId() != null) {
                model.addAttribute("customerName", customerService.findById(invoiceRequest.getCustomerId()).getName());
            }

            return "invoice/form::content";

        }

        invoiceService.createInvoice(invoiceRequest);

        return "redirect:/invoices/create";
    }


    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("invoice") InvoiceRequestDto invoiceRequest, BindingResult bindingResult, Model model) {

        log.info("Update request for invoice {}", id);

        boolean error = false;

        if (!id.equals(invoiceRequest.getId())) {
            log.error("Path variable ID {} does not match invoice request ID {}", id, invoiceRequest.getId());
            error = true;
        }

        if (bindingResult.hasErrors()) {
            log.error("Validation errors: {}", bindingResult.getAllErrors());
            error = true;
        }


        Invoice existingInvoice = invoiceService.findById(id);
        if (existingInvoice == null) {
            log.error("Invoice not found with id: {}", id);
            error = true;
        }

        if (existingInvoice != null && !existingInvoice.getStatus().equals("DRAFT")) {
            log.error("Cannot edit invoice with status: {}", existingInvoice.getStatus());
            error = true;
        }

        if (!invoiceRequest.isBalanced()) {
            bindingResult.reject("payments.mismatch", "Paid amounts not balanced with the total.");
            error = true;
        }

        if (error) {
            log.error("Validation errors: {}", bindingResult.getAllErrors());

            List<Breadcrumb> breadcrumbs = List.of(
                new Breadcrumb("Home", "/"),
                new Breadcrumb("Invoices", "/invoices"),
                new Breadcrumb("Edit Invoice", null)
            );

            model.addAttribute("breadcrumbs", breadcrumbs);
            model.addAttribute("mode", "edit");
            model.addAttribute("message", "Validation errors occurred. Please correct the errors and try again.");

            if (invoiceRequest.getCustomerId() != null) {
                model.addAttribute("customerName", customerService.findById(invoiceRequest.getCustomerId()).getName());
            }

            return "invoice/form::content";

        }

        invoiceService.createInvoice(invoiceRequest);

        return "redirect:/invoices";
    }


    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, HttpServletRequest req, HttpServletResponse res, Model model) {
        try {
            invoiceService.approveInvoice(id);

            if ("inline".equals(req.getHeader("X-Approvemode"))) {
                model.addAttribute("invoiceId", id);
                model.addAttribute("oobTarget", req.getHeader("X-oob-target"));
                return "/invoice/inline-approve-response";
            }

            return "/invoice/approve-response::approved-response";
        } catch (RuntimeException e) {
            log.error("Error approving invoice: {}", e);

            if ("inline".equals(req.getHeader("X-Approvemode"))) {
                model.addAttribute("toast","Approval failed");
                res.setHeader("HX-Retarget", "#toast");
                return "/invoice/approve-response::inline-approve-error";
            }

            return "/invoice/approve-response::approve-error";
        }

    }
}
