package com.example.salesmanager4.purchase_order;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository repo;

    public PurchaseOrderService(PurchaseOrderRepository repo) {
        this.repo = repo;
    }

    public PurchaseOrder create(PurchaseOrder po) {
        po.setStatus("DRAFT");
        return repo.save(po);
    }

    public Iterable<PurchaseOrder> findAll() {
        return repo.findAll();
    }

    public Optional<PurchaseOrder> findById(Long id) {
        return repo.findById(id);
    }
}
