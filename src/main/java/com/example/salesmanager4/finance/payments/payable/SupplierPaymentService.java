package com.example.salesmanager4.finance.payments.payable;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.salesmanager4.finance.payments.AllocationRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierPaymentService {

    private final SupplierPaymentRepository supplierPaymentRepository;
    private final SupplierPaymentAllocationRepository supplierPaymentAllocationRepository;

    public Long createPayment(SupplierPaymentRequest paymentRequest) {

        SupplierPayment payment = new SupplierPayment();
        payment.setSupplierId(paymentRequest.getSupplierId());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setDirection(paymentRequest.getDirection());
        payment.setTotalPaymentAmount(paymentRequest.getTotalPaymentAmount());
        payment.setChequeNumber(paymentRequest.getChequeNumber());
        payment.setBank(paymentRequest.getBank());
        payment.setBankAccount(paymentRequest.getBankAccount());
        payment.setReferenceNumber(paymentRequest.getReferenceNumber());
        payment.setPaymentDate(paymentRequest.getPaymentDate());

        return supplierPaymentRepository.save(payment).getId();
    }

    public void createPaymentAndAllocate(Long grnId, SupplierPaymentRequest paymentRequest) {

        SupplierPayment payment = new SupplierPayment();

        payment.setSupplierId(paymentRequest.getSupplierId());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setDirection(paymentRequest.getDirection());
        payment.setTotalPaymentAmount(paymentRequest.getTotalPaymentAmount());
        payment.setChequeNumber(paymentRequest.getChequeNumber());
        payment.setBank(paymentRequest.getBank());
        payment.setBankAccount(paymentRequest.getBankAccount());
        payment.setReferenceNumber(paymentRequest.getReferenceNumber());
        payment.setPaymentDate(paymentRequest.getPaymentDate());

        Long paymentId = supplierPaymentRepository.save(payment).getId();
        supplierPaymentAllocationRepository.save(SupplierPaymentAllocation.of(paymentId, grnId, paymentRequest.getTotalPaymentAmount()));
    }

    @Transactional
    public Long createPaymentWithAllocations(SupplierPaymentRequest paymentRequest) {

        Long paymentId = createPayment(paymentRequest);

        List<SupplierPaymentAllocation> entities = paymentRequest.getAllocations().stream()
                .filter(a -> a.getGrnId() != null && a.getAmount() != null && a.getAmount().signum() > 0)
                .map(a -> SupplierPaymentAllocation.of(paymentId, a.getGrnId(), a.getAmount()))
                .toList();

        if (!entities.isEmpty()) {
            supplierPaymentAllocationRepository.saveAll(entities);
        }

        return paymentId;
    }

    public void allocatePayment(Long paymentId, List<AllocationRequest> allocations) {

        List<SupplierPaymentAllocation> entities = allocations.stream()
                .map(a -> SupplierPaymentAllocation.of(paymentId, a.documentId(), a.allocatedAmount()))
                .toList(); 

        supplierPaymentAllocationRepository.saveAll(entities);
    }
}
