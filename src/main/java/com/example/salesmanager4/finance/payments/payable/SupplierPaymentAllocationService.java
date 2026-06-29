package com.example.salesmanager4.finance.payments.payable;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierPaymentAllocationService {

    private final SupplierPaymentAllocationRepository repo;

    public List<SupplierPaymentAllocation> getAllocations(Long supplierId) {
        return repo.findAllById(supplierId);
    }   

}
