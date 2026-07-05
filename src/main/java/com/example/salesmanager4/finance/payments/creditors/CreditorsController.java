package com.example.salesmanager4.finance.payments.creditors;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.salesmanager4.finance.payments.creditors.dto.SupplierOutstandingBalance;
import com.example.salesmanager4.finance.payments.creditors.dto.SupplierAging;
import com.example.salesmanager4.finance.payments.creditors.dto.SupplierOutstandingGrn;
import com.example.salesmanager4.finance.payments.creditors.dto.SupplierUnallocatedCredits;
import com.example.salesmanager4.finance.payments.creditors.dto.SupplierUnallocatedRefunds;

import com.example.salesmanager4.util.Breadcrumb;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
@RequestMapping("/creditors")
public class CreditorsController {


    private final CreditorRepository creditorRepository;

    @GetMapping
    public String creditors(Model model) {

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Creditors", null)
        );

        List<SupplierOutstandingBalance> supplierOutstandingBalances = creditorRepository.getSupplierOutstandingBalances();
        List<SupplierOutstandingGrn> supplierOutstandingGrns = creditorRepository.getSupplierOutstandingGrns();
        List<SupplierAging> supplierAging = creditorRepository.getSupplierAging();
        List<SupplierUnallocatedCredits> supplierUnallocatedCredits = creditorRepository.getUnallocatedPaymentCrPerSupplier();
        List<SupplierUnallocatedRefunds> supplierUnallocatedRefunds = creditorRepository.getUnpostedSupplierRefunds();

        SupplierOutstandingBalance totalOutstandingBalance = supplierOutstandingBalances.stream()
                .reduce(new SupplierOutstandingBalance(null, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                        (total, current) -> new SupplierOutstandingBalance(
                                null,
                                total.openGrnCount() + current.openGrnCount(),
                                total.totalCreditIssued().add(current.totalCreditIssued()),
                                total.totalPaid().add(current.totalPaid()),
                                total.totalOutstanding().add(current.totalOutstanding())
                        ));
        
        SupplierAging totalAging = supplierAging.stream()
                .reduce(new SupplierAging(null, null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                        (total, current) -> new SupplierAging(
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
        model.addAttribute("supplierOutstandingBalances", supplierOutstandingBalances);
        model.addAttribute("supplierOutstandingGrns", supplierOutstandingGrns);
        model.addAttribute("supplierAging", supplierAging);
        model.addAttribute("supplierUnallocatedCredits", supplierUnallocatedCredits);
        model.addAttribute("supplierUnallocatedRefunds", supplierUnallocatedRefunds);

        model.addAttribute("totalOutstandingBalance", totalOutstandingBalance);
        model.addAttribute("totalAging", totalAging);

        return "creditors/dashboard::content";
    }
}