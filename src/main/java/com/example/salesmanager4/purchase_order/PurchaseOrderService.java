package com.example.salesmanager4.purchase_order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.salesmanager4.employees.Employee;
import com.example.salesmanager4.employees.EmployeeRepository;
import com.example.salesmanager4.inventory.item.Item;
import com.example.salesmanager4.inventory.item.ItemRepository;
import com.example.salesmanager4.purchase_order.dto.Po;
import com.example.salesmanager4.purchase_order.dto.PoLine;
import com.example.salesmanager4.suppliers.Supplier;
import com.example.salesmanager4.suppliers.SupplierRepository;
import com.example.salesmanager4.users.CurrentUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository repo;
    private final CurrentUserService currentUserService;
    private final SupplierRepository supplierRepo;
    private final EmployeeRepository employeeRepo;
    private final ItemRepository itemRepo;

    public PurchaseOrder create(Po po) {

        Long employeeId = currentUserService.getEmployeeId();
        if (employeeId == null) {
            throw new RuntimeException("Employee ID not found for current user");
        };

        PurchaseOrder purchaseOrder = toPurchaseOrder(po);
        purchaseOrder.setStatus("DRAFT");
        purchaseOrder.setCreatedBy(employeeId);
        purchaseOrder.setCreatedAt(java.time.LocalDateTime.now());
        return repo.save(purchaseOrder);
    }

    public Iterable<PurchaseOrder> findAll() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Po> findById(Long id) {
        PurchaseOrder purchaseOrder = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found with id: " + id));

        return Optional.of(toPo(purchaseOrder));
    }

    private PurchaseOrder toPurchaseOrder(Po po) {

        PurchaseOrder purchaseOrder = new PurchaseOrder();

        purchaseOrder.setId(po.id());
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

    private Po toPo(PurchaseOrder purchaseOrder) {

        Supplier supplier = supplierRepo.findById(purchaseOrder.getSupplierId()).orElseThrow(() -> new RuntimeException("Supplier not found with id: " + purchaseOrder.getSupplierId()));
        Employee employee = employeeRepo.findById(purchaseOrder.getCreatedBy()).orElseThrow(() -> new RuntimeException("Employee not found with id: " + purchaseOrder.getCreatedBy()));

        // Get distinct list of item ids in purchase order
        List<Long> itemIds = purchaseOrder.getItems()
                .stream()
                .map(PurchaseOrderItem::getItemId)
                .distinct()
                .toList();

        // Get the item object for the itemIds list
        Map<Long, Item> items = itemRepo.findByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.toMap(Item::getItemId, i -> i));

        // Create list of items for the dto
        List<PoLine> poLines = new ArrayList<>();
        for (PurchaseOrderItem i : purchaseOrder.getItems()) {
            Item item = items.get(i.getItemId());
            poLines.add(new PoLine(i.getItemId(), item.getName(), i.getQuantity(), i.getPrice().doubleValue()));
        }
        
        // Construct and return dto
        return new Po(
            purchaseOrder.getId(),
            purchaseOrder.getSupplierId(),
            supplier.getName(),
            purchaseOrder.getOrderDate().toString(),
            purchaseOrder.getStatus(),
            purchaseOrder.getCreatedBy(),
            employee.getFullName(),
            purchaseOrder.getCreatedAt(),
            poLines); 
        }
}
