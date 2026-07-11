package com.example.salesmanager4.finance.payments.receivable;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.salesmanager4.finance.payments.AllocationRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerPaymentService {

    private final CustomerPaymentRepository customerPaymentRepository;
    private final CustomerPaymentAllocationRepository customerPaymentAllocationRepository;

    public Long createPayment(CustomerPaymentRequest paymentRequest) {

        CustomerPayment payment = new CustomerPayment();
        payment.setCustomerId(paymentRequest.getCustomerId());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setDirection(paymentRequest.getDirection());
        payment.setTotalPaymentAmount(paymentRequest.getTotalPaymentAmount());
        payment.setChequeNumber(paymentRequest.getChequeNumber());
        payment.setBank(paymentRequest.getBank());
        payment.setBankAccount(paymentRequest.getBankAccount());
        payment.setReferenceNumber(paymentRequest.getReferenceNumber());
        payment.setPaymentDate(paymentRequest.getPaymentDate());

        return customerPaymentRepository.save(payment).getId();
    }

    public void createPaymentAndAllocate(Long invoiceId, CustomerPaymentRequest paymentRequest) {

        Long paymentId = createPayment(paymentRequest);
        customerPaymentAllocationRepository.save(CustomerPaymentAllocation.of(paymentId, invoiceId, paymentRequest.getTotalPaymentAmount()));
    }

    @Transactional
    public Long createPaymentWithAllocations(CustomerPaymentRequest paymentRequest) {

        Long paymentId = createPayment(paymentRequest);

        List<CustomerPaymentAllocation> entities = paymentRequest.getAllocations().stream()
                .filter(a -> a.getDocumentId() != null && a.getAmount() != null && a.getAmount().signum() > 0)
                .map(a -> CustomerPaymentAllocation.of(paymentId, a.getDocumentId(), a.getAmount()))
                .toList();

        if (!entities.isEmpty()) {
            customerPaymentAllocationRepository.saveAll(entities);
        }

        return paymentId;
    }

    public void allocatePayment(Long paymentId, List<AllocationRequest> allocations) {

        List<CustomerPaymentAllocation> entities = allocations.stream()
                .map(a -> CustomerPaymentAllocation.of(paymentId, a.documentId(), a.allocatedAmount()))
                .toList();

        customerPaymentAllocationRepository.saveAll(entities);
    }
}
