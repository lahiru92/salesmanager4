package com.example.salesmanager4.finance.payments.payable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.salesmanager4.finance.payments.PaymentDirection;
import com.example.salesmanager4.finance.payments.PaymentType;
import com.example.salesmanager4.finance.payments.creditors.CreditorRepository;
import com.example.salesmanager4.finance.payments.creditors.dto.SupplierOutstandingGrn;
import com.example.salesmanager4.suppliers.SupplierService;
import com.example.salesmanager4.util.Breadcrumb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/supplier-payments")
public class SupplierPaymentController {

    private final SupplierPaymentService supplierPaymentService;
    private final CreditorRepository creditorRepository;
    private final SupplierService supplierService;

    @GetMapping("/create")
    public String createForm(@RequestParam(required = false) Long supplierId, Model model) {

        SupplierPaymentRequest payment = new SupplierPaymentRequest();
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod(PaymentType.CASH);
        payment.setSupplierId(supplierId);

        if (supplierId != null) {
            model.addAttribute("supplierName", supplierService.findById(supplierId).getName());
            model.addAttribute("grns", creditorRepository.getSupplierOutstandingGrns(supplierId));
        }

        model.addAttribute("breadcrumbs", breadcrumbs());
        model.addAttribute("payment", payment);
        model.addAttribute("allocAmounts", Map.of());

        return "payable/payment-form::content";
    }

    @GetMapping("/outstanding-grns")
    public String outstandingGrns(@RequestParam Long supplierId, Model model) {
        model.addAttribute("grns", creditorRepository.getSupplierOutstandingGrns(supplierId));
        model.addAttribute("allocAmounts", Map.of());
        return "payable/payment-form::allocation-table";
    }

    @PostMapping
    public String create(@ModelAttribute("payment") SupplierPaymentRequest payment, BindingResult bindingResult, Model model) {

        log.info("Supplier payment request: {}", payment);

        payment.setDirection(PaymentDirection.OUT);

        List<AllocationLine> allocations = payment.getAllocations() == null ? List.of()
                : payment.getAllocations().stream()
                        .filter(a -> a.getGrnId() != null && a.getAmount() != null && a.getAmount().signum() != 0)
                        .toList();

        if (payment.getSupplierId() == null) {
            bindingResult.reject("supplier.required", "Supplier is required.");
        }
        if (payment.getPaymentDate() == null) {
            bindingResult.reject("paymentDate.required", "Payment date is required.");
        }
        if (payment.getPaymentMethod() == null) {
            bindingResult.reject("paymentMethod.required", "Payment method is required.");
        }
        if (payment.getTotalPaymentAmount() == null || payment.getTotalPaymentAmount().signum() <= 0) {
            bindingResult.reject("amount.invalid", "Payment amount must be greater than zero.");
        }
        if (payment.getPaymentMethod() == PaymentType.CHEQUE && !StringUtils.hasText(payment.getChequeNumber())) {
            bindingResult.reject("chequeNumber.required", "Cheque number is required for cheque payments.");
        }

        List<SupplierOutstandingGrn> grns = payment.getSupplierId() == null ? List.of()
                : creditorRepository.getSupplierOutstandingGrns(payment.getSupplierId());
        Map<Long, SupplierOutstandingGrn> outstandingByGrn = grns.stream()
                .collect(Collectors.toMap(SupplierOutstandingGrn::grnId, Function.identity()));

        BigDecimal allocatedTotal = BigDecimal.ZERO;
        for (AllocationLine allocation : allocations) {
            SupplierOutstandingGrn grn = outstandingByGrn.get(allocation.getGrnId());
            if (grn == null) {
                bindingResult.reject("allocation.unknownGrn",
                        "GRN #" + allocation.getGrnId() + " is not an outstanding GRN of the selected supplier.");
                continue;
            }
            if (allocation.getAmount().signum() < 0) {
                bindingResult.reject("allocation.negative",
                        "Allocation for GRN #" + allocation.getGrnId() + " cannot be negative.");
                continue;
            }
            if (allocation.getAmount().compareTo(grn.outstandingBalance()) > 0) {
                bindingResult.reject("allocation.exceedsOutstanding",
                        "Allocation for GRN #" + allocation.getGrnId() + " exceeds its outstanding balance.");
            }
            allocatedTotal = allocatedTotal.add(allocation.getAmount());
        }

        if (payment.getTotalPaymentAmount() != null && allocatedTotal.compareTo(payment.getTotalPaymentAmount()) > 0) {
            bindingResult.reject("allocation.exceedsPayment", "Total allocated amount exceeds the payment amount.");
        }

        if (bindingResult.hasErrors()) {
            log.warn("Supplier payment validation errors: {}", bindingResult.getAllErrors());

            if (payment.getSupplierId() != null) {
                model.addAttribute("supplierName", supplierService.findById(payment.getSupplierId()).getName());
                model.addAttribute("grns", grns);
            }
            model.addAttribute("allocAmounts", allocations.stream()
                    .collect(Collectors.toMap(AllocationLine::getGrnId, AllocationLine::getAmount, BigDecimal::add)));
            model.addAttribute("breadcrumbs", breadcrumbs());

            return "payable/payment-form::content";
        }

        payment.setAllocations(allocations);
        Long paymentId = supplierPaymentService.createPaymentWithAllocations(payment);

        SupplierPaymentRequest fresh = new SupplierPaymentRequest();
        fresh.setPaymentDate(LocalDate.now());
        fresh.setPaymentMethod(PaymentType.CASH);

        model.addAttribute("payment", fresh);
        model.addAttribute("allocAmounts", Map.of());
        model.addAttribute("breadcrumbs", breadcrumbs());
        model.addAttribute("message", "Payment #" + paymentId + " recorded.");

        return "payable/payment-form::content";
    }

    private List<Breadcrumb> breadcrumbs() {
        return List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Creditors", "/creditors"),
            new Breadcrumb("Make a Payment", null)
        );
    }
}
