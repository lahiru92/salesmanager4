package com.example.salesmanager4.purchase_order;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;



public interface PurchaseOrderRepository 
        extends CrudRepository<PurchaseOrder, Long> {

    public Iterable<PurchaseOrder> findAllByOrderByCreatedAtDesc();

    public Iterable<PurchaseOrder> findAllBySupplierIdOrderByCreatedAtDesc(Long supplierId);

    public Iterable<PurchaseOrder> findAllByCreatedByOrderByCreatedAtDesc(Long employeeId);

    public Iterable<PurchaseOrder> findAllByCreatedByAndSupplierIdOrderByCreatedAtDesc(Long employeeId, Long supplierId);

    public Optional<PurchaseOrder> findById(Long id);
}
