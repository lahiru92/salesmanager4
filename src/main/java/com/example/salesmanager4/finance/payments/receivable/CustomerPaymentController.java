package com.example.salesmanager4.finance.payments.receivable;

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

import com.example.salesmanager4.customers.CustomerService;
import com.example.salesmanager4.employees.EmployeeRepository;
import com.example.salesmanager4.finance.payments.AllocationLine;
import com.example.salesmanager4.finance.payments.PaymentDirection;
import com.example.salesmanager4.finance.payments.PaymentType;
import com.example.salesmanager4.finance.payments.debtors.DebtorRepository;
import com.example.salesmanager4.finance.payments.debtors.dto.CustomerOutstandingInvoice;
import com.example.salesmanager4.util.Breadcrumb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/customer-payments")
public class CustomerPaymentController {

    private final CustomerPaymentService customerPaymentService;
    private final DebtorRepository debtorRepository;
    private final CustomerService customerService;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/create")
    public String createForm(@RequestParam(required = false) Long customerId, Model model) {

        CustomerPaymentRequest payment = new CustomerPaymentRequest();
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod(PaymentType.CASH);
        payment.setCustomerId(customerId);

        if (customerId != null) {
            model.addAttribute("customerName", customerService.findById(customerId).getName());
            model.addAttribute("invoices", debtorRepository.getCustomerOutstandingInvoices(customerId));
        }

        model.addAttribute("breadcrumbs", breadcrumbs());
        model.addAttribute("payment", payment);
        model.addAttribute("allocAmounts", Map.of());

        return "receivable/payment-form::content";
    }

    @GetMapping("/outstanding-invoices")
    public String outstandingInvoices(@RequestParam Long customerId, Model model) {
        model.addAttribute("invoices", debtorRepository.getCustomerOutstandingInvoices(customerId));
        model.addAttribute("allocAmounts", Map.of());
        return "receivable/payment-form::allocation-table";
    }

    @PostMapping
    public String create(@ModelAttribute("payment") CustomerPaymentRequest payment, BindingResult bindingResult, Model model) {

        log.info("Customer payment request: {}", payment);

        payment.setDirection(PaymentDirection.IN);

        List<AllocationLine> allocations = payment.getAllocations() == null ? List.of()
                : payment.getAllocations().stream()
                        .filter(a -> a.getDocumentId() != null && a.getAmount() != null && a.getAmount().signum() != 0)
                        .toList();

        if (payment.getCustomerId() == null) {
            bindingResult.reject("customer.required", "Customer is required.");
        }
        if (payment.getCollectedBy() == null) {
            bindingResult.reject("collectedBy.required", "Collected by is required.");
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

        List<CustomerOutstandingInvoice> invoices = payment.getCustomerId() == null ? List.of()
                : debtorRepository.getCustomerOutstandingInvoices(payment.getCustomerId());
        Map<Long, CustomerOutstandingInvoice> outstandingByInvoice = invoices.stream()
                .collect(Collectors.toMap(CustomerOutstandingInvoice::invoiceId, Function.identity()));

        BigDecimal allocatedTotal = BigDecimal.ZERO;
        for (AllocationLine allocation : allocations) {
            CustomerOutstandingInvoice invoice = outstandingByInvoice.get(allocation.getDocumentId());
            if (invoice == null) {
                bindingResult.reject("allocation.unknownInvoice",
                        "Invoice #" + allocation.getDocumentId() + " is not an outstanding invoice of the selected customer.");
                continue;
            }
            if (allocation.getAmount().signum() < 0) {
                bindingResult.reject("allocation.negative",
                        "Allocation for invoice #" + allocation.getDocumentId() + " cannot be negative.");
                continue;
            }
            if (allocation.getAmount().compareTo(invoice.outstandingBalance()) > 0) {
                bindingResult.reject("allocation.exceedsOutstanding",
                        "Allocation for invoice #" + allocation.getDocumentId() + " exceeds its outstanding balance.");
            }
            allocatedTotal = allocatedTotal.add(allocation.getAmount());
        }

        if (payment.getTotalPaymentAmount() != null && allocatedTotal.compareTo(payment.getTotalPaymentAmount()) > 0) {
            bindingResult.reject("allocation.exceedsPayment", "Total allocated amount exceeds the payment amount.");
        }

        if (bindingResult.hasErrors()) {
            log.warn("Customer payment validation errors: {}", bindingResult.getAllErrors());

            if (payment.getCustomerId() != null) {
                model.addAttribute("customerName", customerService.findById(payment.getCustomerId()).getName());
                model.addAttribute("invoices", invoices);
            }
            if (payment.getCollectedBy() != null) {
                employeeRepository.findById(payment.getCollectedBy())
                        .ifPresent(e -> model.addAttribute("collectorName", e.getKnownName()));
            }
            model.addAttribute("allocAmounts", allocations.stream()
                    .collect(Collectors.toMap(AllocationLine::getDocumentId, AllocationLine::getAmount, BigDecimal::add)));
            model.addAttribute("breadcrumbs", breadcrumbs());

            return "receivable/payment-form::content";
        }

        payment.setAllocations(allocations);
        Long paymentId = customerPaymentService.createPaymentWithAllocations(payment);

        CustomerPaymentRequest fresh = new CustomerPaymentRequest();
        fresh.setPaymentDate(LocalDate.now());
        fresh.setPaymentMethod(PaymentType.CASH);

        model.addAttribute("payment", fresh);
        model.addAttribute("allocAmounts", Map.of());
        model.addAttribute("breadcrumbs", breadcrumbs());
        model.addAttribute("message", "Payment #" + paymentId + " recorded.");

        return "receivable/payment-form::content";
    }

    private List<Breadcrumb> breadcrumbs() {
        return List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Debtors", "/debtors"),
            new Breadcrumb("Receive a Payment", null)
        );
    }
}
