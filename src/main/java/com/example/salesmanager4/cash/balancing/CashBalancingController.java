package com.example.salesmanager4.cash.balancing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.salesmanager4.cash.balancing.dto.CollectionLine;
import com.example.salesmanager4.cash.balancing.dto.SalesmanCashSummary;
import com.example.salesmanager4.cash.balancing.dto.SupplierCashMovement;
import com.example.salesmanager4.employees.EmployeeRepository;
import com.example.salesmanager4.util.Breadcrumb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/cash-balancing")
public class CashBalancingController {

    private final CashBalancingService cashBalancingService;
    private final CashBalancingRepository cashBalancingRepository;
    private final CashHandoverRepository cashHandoverRepository;
    private final CashDrawerSessionRepository cashDrawerSessionRepository;
    private final EmployeeRepository employeeRepository;

    @GetMapping
    public String dashboard(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        populateDashboard(model, date != null ? date : LocalDate.now());
        return "cash/dashboard::content";
    }

    @GetMapping("/handovers/create")
    public String handoverForm(@RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        var existing = cashHandoverRepository.findByEmployeeIdAndHandoverDate(employeeId, date);

        if (existing.isPresent()) {
            model.addAttribute("mode", "view");
            model.addAttribute("handover", existing.get());
        } else {
            CashHandoverRequest request = new CashHandoverRequest();
            request.setEmployeeId(employeeId);
            request.setHandoverDate(date);

            model.addAttribute("mode", "create");
            model.addAttribute("handoverRequest", request);
        }

        populateHandoverForm(model, employeeId, date);
        return "cash/handover-form::content";
    }

    @PostMapping("/handovers")
    public String verifyHandover(@ModelAttribute("handoverRequest") CashHandoverRequest request,
            BindingResult bindingResult, Model model) {

        log.info("Cash handover request: {}", request);

        if (request.getEmployeeId() == null || request.getHandoverDate() == null) {
            bindingResult.reject("handover.invalid", "Salesman and date are required.");
        }
        if (request.getDeclaredCash() != null && request.getDeclaredCash().signum() < 0) {
            bindingResult.reject("declaredCash.negative", "Physical cash cannot be negative.");
        }
        for (CashHandoverRequest.DepositLine deposit : request.getDeposits()) {
            boolean hasContent = StringUtils.hasText(deposit.getBank())
                    || StringUtils.hasText(deposit.getReferenceNumber())
                    || (deposit.getAmount() != null && deposit.getAmount().signum() != 0);
            if (hasContent && (!StringUtils.hasText(deposit.getBank())
                    || deposit.getAmount() == null || deposit.getAmount().signum() <= 0)) {
                bindingResult.reject("deposit.invalid", "Each CDM deposit needs a bank and a positive amount.");
                break;
            }
        }

        if (!bindingResult.hasErrors()) {
            try {
                CashHandover handover = cashBalancingService.verifyHandover(request);

                populateDashboard(model, request.getHandoverDate());
                model.addAttribute("message", "Handover #" + handover.getId() + " verified.");
                return "cash/dashboard::content";
            } catch (RuntimeException e) {
                log.error("Error verifying handover", e);
                bindingResult.reject("handover.failed", e.getMessage());
            }
        }

        log.warn("Handover validation errors: {}", bindingResult.getAllErrors());
        model.addAttribute("mode", "create");
        populateHandoverForm(model, request.getEmployeeId(), request.getHandoverDate());
        return "cash/handover-form::content";
    }

    @PostMapping("/close")
    public String closeDrawer(@ModelAttribute("drawerClose") DrawerCloseRequest request,
            BindingResult bindingResult, Model model) {

        log.info("Drawer close request: {}", request);

        LocalDate date = request.getSessionDate() != null ? request.getSessionDate() : LocalDate.now();

        if (request.getSessionDate() == null) {
            bindingResult.reject("sessionDate.required", "Session date is required.");
        }
        if (request.getOpeningBalance() == null || request.getOpeningBalance().signum() < 0) {
            bindingResult.reject("openingBalance.invalid", "Opening balance must be zero or more.");
        }
        if (request.getCountedClosing() == null || request.getCountedClosing().signum() < 0) {
            bindingResult.reject("countedClosing.invalid", "Counted closing cash must be zero or more.");
        }

        if (!bindingResult.hasErrors()) {
            try {
                CashDrawerSession session = cashBalancingService.closeDrawer(request);

                populateDashboard(model, date);
                model.addAttribute("message", "Cash drawer closed for " + session.getSessionDate() + ".");
                return "cash/dashboard::content";
            } catch (RuntimeException e) {
                log.error("Error closing drawer", e);
                bindingResult.reject("close.failed", e.getMessage());
            }
        }

        log.warn("Drawer close validation errors: {}", bindingResult.getAllErrors());
        populateDashboard(model, date);
        return "cash/dashboard::content";
    }

    private void populateDashboard(Model model, LocalDate date) {

        List<SalesmanCashSummary> summaries = cashBalancingRepository.getSalesmanCashSummaries(date);

        BigDecimal expectedTotal = summaries.stream()
                .map(s -> s.expectedCash() != null ? s.expectedCash() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal receivedTotal = summaries.stream()
                .filter(SalesmanCashSummary::isVerified)
                .map(s -> nvl(s.declaredCash()).add(nvl(s.cdmTotal())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal varianceTotal = summaries.stream()
                .filter(SalesmanCashSummary::isVerified)
                .map(s -> nvl(s.variance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long unverifiedCount = summaries.stream().filter(s -> !s.isVerified()).count();

        CashDrawerSession session = cashDrawerSessionRepository.findBySessionDate(date).orElse(null);
        BigDecimal handoverCash = cashBalancingService.handoverCash(date);
        SupplierCashMovement supplierCash = cashBalancingService.supplierCashMovement(date);

        if (!model.containsAttribute("drawerClose")) {
            DrawerCloseRequest drawerClose = new DrawerCloseRequest();
            drawerClose.setSessionDate(date);
            drawerClose.setOpeningBalance(cashBalancingService.defaultOpeningBalance(date));
            model.addAttribute("drawerClose", drawerClose);
        }

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Cash Balancing", null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("date", date);
        model.addAttribute("summaries", summaries);
        model.addAttribute("expectedTotal", expectedTotal);
        model.addAttribute("receivedTotal", receivedTotal);
        model.addAttribute("varianceTotal", varianceTotal);
        model.addAttribute("unverifiedCount", unverifiedCount);
        model.addAttribute("drawerSession", session);
        model.addAttribute("handoverCash", handoverCash);
        model.addAttribute("otherCashIn", supplierCash.cashIn());
        model.addAttribute("cashOut", supplierCash.cashOut());
    }

    private void populateHandoverForm(Model model, Long employeeId, LocalDate date) {

        String employeeName = employeeRepository.findById(employeeId)
                .map(e -> e.getKnownName())
                .orElse("Employee #" + employeeId);

        List<CollectionLine> cashCollections = cashBalancingService.getCashCollections(employeeId, date);
        List<CollectionLine> chequeCollections = cashBalancingService.getChequeCollections(employeeId, date);

        BigDecimal expectedCash = cashCollections.stream()
                .map(CollectionLine::signedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal chequeTotal = chequeCollections.stream()
                .map(CollectionLine::signedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Cash Balancing", "/cash-balancing?date=" + date),
            new Breadcrumb("Verify Handover", null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("date", date);
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("employeeName", employeeName);
        model.addAttribute("cashCollections", cashCollections);
        model.addAttribute("chequeCollections", chequeCollections);
        model.addAttribute("expectedCash", expectedCash);
        model.addAttribute("chequeTotal", chequeTotal);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
