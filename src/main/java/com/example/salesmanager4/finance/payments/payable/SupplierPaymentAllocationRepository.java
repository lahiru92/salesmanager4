package com.example.salesmanager4.finance.payments.payable;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface SupplierPaymentAllocationRepository extends CrudRepository<SupplierPaymentAllocation, Long>{
    
    public List<SupplierPaymentAllocation> findAllById(Long supplierId);
}
