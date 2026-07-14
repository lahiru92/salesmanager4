package com.example.salesmanager4.finance.ledger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.salesmanager4.util.Breadcrumb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/ledger")
public class LedgerController {

    private final LedgerService ledgerService;
    private final LedgerJdbcRepository ledgerJdbcRepository;
    private final LedgerCategoryRepository categoryRepository;

    @GetMapping
    public String list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) LedgerKind kind,
            @RequestParam(required = false) Long categoryId,
            Model model) {

        populateList(model, from, to, kind, categoryId);
        return "ledger/list::content";
    }

    @GetMapping("/create")
    public String createForm(Model model) {

        LedgerEntryRequest entry = new LedgerEntryRequest();
        entry.setEntryDate(LocalDate.now());

        model.addAttribute("entry", entry);
        populateForm(model);
        return "ledger/form::content";
    }

    @PostMapping
    public String create(@ModelAttribute("entry") LedgerEntryRequest entry, BindingResult bindingResult, Model model) {

        log.info("Ledger entry request: {}", entry);

        if (entry.getEntryDate() == null) {
            bindingResult.reject("entryDate.required", "Date is required.");
        }
        if (entry.getCategoryId() == null) {
            bindingResult.reject("category.required", "Category is required.");
        }
        if (entry.getAmount() == null || entry.getAmount().signum() <= 0) {
            bindingResult.reject("amount.invalid", "Amount must be greater than zero.");
        }
        if (entry.getPaymentMethod() == null) {
            bindingResult.reject("paymentMethod.required", "Payment method is required.");
        }

        if (!bindingResult.hasErrors()) {
            try {
                LedgerEntry saved = ledgerService.createEntry(entry);

                populateList(model, null, null, null, null);
                model.addAttribute("message",
                        saved.getKind() + " entry #" + saved.getId() + " recorded.");
                return "ledger/list::content";
            } catch (RuntimeException e) {
                log.error("Error creating ledger entry", e);
                bindingResult.reject("entry.failed", e.getMessage());
            }
        }

        log.warn("Ledger entry validation errors: {}", bindingResult.getAllErrors());
        populateForm(model);
        return "ledger/form::content";
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        ledgerService.deleteEntry(id);
        return ResponseEntity.ok().build();
    }

    // ------------------------------------------------------------------
    // Categories
    // ------------------------------------------------------------------

    @GetMapping("/categories")
    public String categories(Model model) {
        populateCategories(model);
        return "ledger/categories::content";
    }

    @PostMapping("/categories")
    public String createCategory(@RequestParam String name, @RequestParam LedgerKind kind, Model model) {

        if (!StringUtils.hasText(name)) {
            model.addAttribute("message", "Category name is required.");
        } else {
            ledgerService.createCategory(name.trim(), kind);
            model.addAttribute("message", "Category created.");
        }

        populateCategories(model);
        return "ledger/categories::content";
    }

    @DeleteMapping("/categories/{id}")
    public String disableCategory(@PathVariable Long id, Model model) {
        ledgerService.disableCategory(id);
        model.addAttribute("message", "Category disabled.");
        populateCategories(model);
        return "ledger/categories::content";
    }

    // ------------------------------------------------------------------

    private void populateList(Model model, LocalDate from, LocalDate to, LedgerKind kind, Long categoryId) {

        LocalDate effectiveFrom = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate effectiveTo = to != null ? to : LocalDate.now();

        List<LedgerEntryRow> entries = ledgerJdbcRepository.findEntries(effectiveFrom, effectiveTo, kind, categoryId);

        BigDecimal incomeTotal = entries.stream()
                .filter(e -> LedgerKind.INCOME.name().equals(e.kind()))
                .map(LedgerEntryRow::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expenseTotal = entries.stream()
                .filter(e -> LedgerKind.EXPENSE.name().equals(e.kind()))
                .map(LedgerEntryRow::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Income / Expenses", null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("entries", entries);
        model.addAttribute("from", effectiveFrom);
        model.addAttribute("to", effectiveTo);
        model.addAttribute("kind", kind);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", categoryRepository.findActive());
        model.addAttribute("incomeTotal", incomeTotal);
        model.addAttribute("expenseTotal", expenseTotal);
        model.addAttribute("netTotal", incomeTotal.subtract(expenseTotal));
    }

    private void populateForm(Model model) {

        List<LedgerCategory> categories = categoryRepository.findActive();

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Income / Expenses", "/ledger"),
            new Breadcrumb("New Entry", null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("incomeCategories",
                categories.stream().filter(c -> c.getKind() == LedgerKind.INCOME).toList());
        model.addAttribute("expenseCategories",
                categories.stream().filter(c -> c.getKind() == LedgerKind.EXPENSE).toList());
    }

    private void populateCategories(Model model) {

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Income / Expenses", "/ledger"),
            new Breadcrumb("Categories", null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("categories", categoryRepository.findAllOrdered());
    }
}
