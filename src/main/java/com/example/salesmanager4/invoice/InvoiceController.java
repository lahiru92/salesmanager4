package com.example.salesmanager4.invoice;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    @GetMapping
    public String listInvoices(Model model) {
        return "invoice/list";
    }

    @GetMapping("/create")
    public String createInvoice(Model model) {
        List<InvoiceLine> lines = new ArrayList<>();
        InvoiceLine newItem = new InvoiceLine(0L, 0L, "", 0.0, 0.0, 0, 0);
        InvoiceDto invoice = new InvoiceDto("", newItem, lines);
        model.addAttribute("invoiceLineDto", invoice);
        return "invoice/create::content";
    }

    @PostMapping("/addline")
    @ResponseBody
    public String addLine(@ModelAttribute InvoiceDto invoice, Model model) {
        return "hello";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute @Valid InvoiceDto invoice, BindingResult result, Model model) {
        model.addAttribute("invoiceLineDto", invoice);
        if (result.hasErrors()) {
            return "invoice_create::invoice-create-form";
        }
        return "redirect:/invoices/create";
    }

    @PostMapping("/recalculate")
    public String recalculate(@ModelAttribute InvoiceLine line,  Model model) {

        Logger.getLogger(InvoiceController.class.getName()).info("Recalculating line: " + line.toString());

        InvoiceLine updatedLine = new InvoiceLine(
            line.itemId(),
            line.batchId(),
            line.itemName(),
            line.price(),
            line.price() == null?0:line.price() / 10d,
            line.qty(),
            line.qty() == null?0:line.qty() * 2
        );

        model.addAttribute("item", updatedLine);
        return "fragments/invoice_new_item_row::newItemRow";
    }

}

record InvoiceDto(

    @Size(min = 5, max = 10) @NotBlank String invoiceNo,
    InvoiceLine newItem,
    List<InvoiceLine> lines) {

    void addLine() {
        if (lines != null && newItem != null) {
            lines.add(newItem);
        }
    }
}

record InvoiceLine(
        Long itemId,
        Long batchId,
        String itemName,
        Double price,
        Double discount,
        Integer qty,
        Integer freeQty) {
}