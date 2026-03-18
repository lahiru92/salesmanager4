package com.example.salesmanager4.purchase_order;

import org.springframework.data.repository.CrudRepository;

public interface PurchaseOrderRepository 
        extends CrudRepository<PurchaseOrder, Long> {
}
