package com.example.salesmanager4.finance.payments.debtors;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.salesmanager4.finance.payments.debtors.dto.CustomerAging;
import com.example.salesmanager4.finance.payments.debtors.dto.CustomerOutstandingBalance;
import com.example.salesmanager4.finance.payments.debtors.dto.CustomerOutstandingInvoice;
import com.example.salesmanager4.finance.payments.debtors.dto.CustomerUnallocatedCredits;
import com.example.salesmanager4.finance.payments.debtors.dto.CustomerUnallocatedRefunds;

import com.example.salesmanager4.util.Breadcrumb;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
@RequestMapping("/debtors")
public class DebtorsController {


    private final DebtorRepository debtorRepository;

    @GetMapping
    public String debtors(Model model) {

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Debtors", null)
        );

        List<CustomerOutstandingBalance> customerOutstandingBalances = debtorRepository.getCustomerOutstandingBalances();
        List<CustomerOutstandingInvoice> customerOutstandingInvoices = debtorRepository.getCustomerOutstandingInvoices();
        List<CustomerAging> customerAging = debtorRepository.getCustomerAging();
        List<CustomerUnallocatedCredits> customerUnallocatedCredits = debtorRepository.getUnallocatedPaymentDrPerCustomer();
        List<CustomerUnallocatedRefunds> customerUnallocatedRefunds = debtorRepository.getUnpostedCustomerRefunds();

        CustomerOutstandingBalance totalOutstandingBalance = customerOutstandingBalances.stream()
                .reduce(new CustomerOutstandingBalance(null, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                        (total, current) -> new CustomerOutstandingBalance(
                                null,
                                total.openInvoiceCount() + current.openInvoiceCount(),
                                total.totalCreditIssued().add(current.totalCreditIssued()),
                                total.totalPaid().add(current.totalPaid()),
                                total.totalOutstanding().add(current.totalOutstanding())
                        ));

        CustomerAging totalAging = customerAging.stream()
                .reduce(new CustomerAging(null, null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                        (total, current) -> new CustomerAging(
                                null,
                                null,
                                total.totalOutstanding().add(current.totalOutstanding() != null ? current.totalOutstanding() : BigDecimal.ZERO),
                                total.currentAmount().add(current.currentAmount() != null ? current.currentAmount() : BigDecimal.ZERO),
                                total.overdue_1_30().add(current.overdue_1_30() != null ? current.overdue_1_30() : BigDecimal.ZERO),
                                total.overdue_31_60().add(current.overdue_31_60() != null ? current.overdue_31_60() : BigDecimal.ZERO),
                                total.overdue_61_90().add(current.overdue_61_90() != null ? current.overdue_61_90() : BigDecimal.ZERO),
                                total.overdue_90Plus().add(current.overdue_90Plus() != null ? current.overdue_90Plus() : BigDecimal.ZERO)
                        ));


        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("customerOutstandingBalances", customerOutstandingBalances);
        model.addAttribute("customerOutstandingInvoices", customerOutstandingInvoices);
        model.addAttribute("customerAging", customerAging);
        model.addAttribute("customerUnallocatedCredits", customerUnallocatedCredits);
        model.addAttribute("customerUnallocatedRefunds", customerUnallocatedRefunds);

        model.addAttribute("totalOutstandingBalance", totalOutstandingBalance);
        model.addAttribute("totalAging", totalAging);

        return "debtors/dashboard::content";
    }
}
