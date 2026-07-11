package com.example.salesmanager4.cash.balancing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.salesmanager4.cash.balancing.dto.CollectionLine;
import com.example.salesmanager4.cash.balancing.dto.SupplierCashMovement;
import com.example.salesmanager4.users.CurrentUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CashBalancingService {

    private final CashBalancingRepository cashBalancingRepository;
    private final CashHandoverRepository cashHandoverRepository;
    private final CashDrawerSessionRepository cashDrawerSessionRepository;
    private final CurrentUserService currentUserService;

    public List<CollectionLine> getCashCollections(Long employeeId, LocalDate date) {
        return cashBalancingRepository.getCollections(employeeId, date, "CASH");
    }

    public List<CollectionLine> getChequeCollections(Long employeeId, LocalDate date) {
        return cashBalancingRepository.getCollections(employeeId, date, "CHEQUE");
    }

    /** System-expected cash for a salesman/day: receipts (IN) minus cash refunds (OUT). */
    public BigDecimal expectedCash(Long employeeId, LocalDate date) {
        return getCashCollections(employeeId, date).stream()
                .map(CollectionLine::signedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public CashHandover verifyHandover(CashHandoverRequest request) {

        Long verifierEmployeeId = currentUserService.getEmployeeId();
        if (verifierEmployeeId == null) {
            throw new RuntimeException("Employee ID not found for current user");
        }

        cashHandoverRepository.findByEmployeeIdAndHandoverDate(request.getEmployeeId(), request.getHandoverDate())
                .ifPresent(h -> {
                    throw new RuntimeException("A handover for this salesman and date is already verified");
                });

        BigDecimal expected = expectedCash(request.getEmployeeId(), request.getHandoverDate());
        BigDecimal declared = request.getDeclaredCash() != null ? request.getDeclaredCash() : BigDecimal.ZERO;

        List<CashHandoverDeposit> deposits = request.getDeposits().stream()
                .filter(d -> StringUtils.hasText(d.getBank()) || StringUtils.hasText(d.getReferenceNumber())
                        || (d.getAmount() != null && d.getAmount().signum() != 0))
                .map(d -> {
                    CashHandoverDeposit deposit = new CashHandoverDeposit();
                    deposit.setBank(d.getBank());
                    deposit.setReferenceNumber(d.getReferenceNumber());
                    deposit.setAmount(d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO);
                    return deposit;
                })
                .toList();

        BigDecimal cdmTotal = deposits.stream()
                .map(CashHandoverDeposit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CashHandover handover = new CashHandover();
        handover.setHandoverDate(request.getHandoverDate());
        handover.setEmployeeId(request.getEmployeeId());
        handover.setStatus("VERIFIED");
        handover.setExpectedAmount(expected);
        handover.setDeclaredCash(declared);
        handover.setCdmTotal(cdmTotal);
        handover.setVariance(declared.add(cdmTotal).subtract(expected));
        handover.setRemarks(request.getRemarks());
        handover.setVerifiedBy(verifierEmployeeId);
        handover.setDeposits(deposits);

        log.info("Verifying cash handover: {}", handover);

        return cashHandoverRepository.save(handover);
    }

    /** Physical cash received from verified handovers on the given day. */
    public BigDecimal handoverCash(LocalDate date) {
        return cashHandoverRepository.findByHandoverDate(date).stream()
                .map(h -> h.getDeclaredCash() != null ? h.getDeclaredCash() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public SupplierCashMovement supplierCashMovement(LocalDate date) {
        return cashBalancingRepository.getSupplierCashMovement(date);
    }

    /** Default drawer opening balance: the counted closing of the most recent closed session. */
    public BigDecimal defaultOpeningBalance(LocalDate date) {
        return cashDrawerSessionRepository.findLatestBefore(date)
                .map(CashDrawerSession::getCountedClosing)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional
    public CashDrawerSession closeDrawer(DrawerCloseRequest request) {

        Long closerEmployeeId = currentUserService.getEmployeeId();
        if (closerEmployeeId == null) {
            throw new RuntimeException("Employee ID not found for current user");
        }

        cashDrawerSessionRepository.findBySessionDate(request.getSessionDate())
                .ifPresent(s -> {
                    throw new RuntimeException("The cash drawer is already closed for this date");
                });

        BigDecimal opening = request.getOpeningBalance() != null ? request.getOpeningBalance() : BigDecimal.ZERO;
        BigDecimal counted = request.getCountedClosing() != null ? request.getCountedClosing() : BigDecimal.ZERO;
        BigDecimal handoverCash = handoverCash(request.getSessionDate());
        SupplierCashMovement supplierCash = supplierCashMovement(request.getSessionDate());

        BigDecimal expectedClosing = opening
                .add(handoverCash)
                .add(supplierCash.cashIn())
                .subtract(supplierCash.cashOut());

        CashDrawerSession session = new CashDrawerSession();
        session.setSessionDate(request.getSessionDate());
        session.setStatus("CLOSED");
        session.setOpeningBalance(opening);
        session.setHandoverCash(handoverCash);
        session.setOtherCashIn(supplierCash.cashIn());
        session.setCashOut(supplierCash.cashOut());
        session.setExpectedClosing(expectedClosing);
        session.setCountedClosing(counted);
        session.setVariance(counted.subtract(expectedClosing));
        session.setRemarks(request.getRemarks());
        session.setClosedBy(closerEmployeeId);

        log.info("Closing cash drawer: {}", session);

        return cashDrawerSessionRepository.save(session);
    }
}
