package com.example.salesmanager4.purchase_order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.example.salesmanager4.purchase_order.dto.Po;
import com.example.salesmanager4.users.CurrentUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository repo;
    private final CurrentUserService currentUserService;

    public PurchaseOrder create(Po po) {

        Long employeeId = currentUserService.getEmployeeId();
        if (employeeId == null) {
            throw new RuntimeException("Employee ID not found for current user");
        };

        PurchaseOrder purchaseOrder = toPurchaseOrder(po);
        purchaseOrder.setStatus("DRAFT");
        purchaseOrder.setCreatedBy(employeeId);
        return repo.save(purchaseOrder);
    }

    public Iterable<PurchaseOrder> findAll() {
        return repo.findAll();
    }

    public Optional<PurchaseOrder> findById(Long id) {
        return repo.findById(id);
    }

    private PurchaseOrder toPurchaseOrder(Po po) {

        PurchaseOrder purchaseOrder = new PurchaseOrder();

        purchaseOrder.setSupplierId(po.supplierId());
        purchaseOrder.setOrderDate(LocalDate.parse(po.orderDate()));
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
