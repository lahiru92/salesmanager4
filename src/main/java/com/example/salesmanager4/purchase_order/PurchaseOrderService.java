package com.example.salesmanager4.purchase_order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.salesmanager4.purchase_order.dto.Po;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository repo;

    public PurchaseOrderService(PurchaseOrderRepository repo) {
        this.repo = repo;
    }

    public PurchaseOrder create(Po po) {
        return repo.save(toPurchaseOrder(po, "DRAFT"));
    }

    public Iterable<PurchaseOrder> findAll() {
        return repo.findAll();
    }

    public Optional<PurchaseOrder> findById(Long id) {
        return repo.findById(id);
    }

    private PurchaseOrder toPurchaseOrder(Po po, String status) {

        PurchaseOrder purchaseOrder = new PurchaseOrder();

        purchaseOrder.setSupplierId(po.supplierId());
        purchaseOrder.setOrderDate(LocalDate.parse(po.orderDate()));
        purchaseOrder.setStatus(status);
        purchaseOrder.setItems(po.items().stream()
                .map(item -> {
                    PurchaseOrderItem i = new PurchaseOrderItem();
                    i.setItemId(item.itemId());
                    i.setPrice(new BigDecimal(item.price()));
                    i.setQuantity(item.qty());
                    return i;
                })
                .collect(Collectors.toList()));

        return purchaseOrder;
    }
}
