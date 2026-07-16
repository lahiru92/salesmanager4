package com.example.salesmanager4.grn;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.salesmanager4.finance.payments.PaymentDirection;
import com.example.salesmanager4.finance.payments.PaymentType;
import com.example.salesmanager4.finance.payments.payable.SupplierPaymentRequest;
import com.example.salesmanager4.finance.payments.payable.SupplierPaymentService;
import com.example.salesmanager4.inventory.stock.StockTransactionService;
import com.example.salesmanager4.purchase_order.PurchaseOrderService;
import com.example.salesmanager4.purchase_order.dto.Po;
import com.example.salesmanager4.users.CurrentUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GrnService {

    private final GrnRepository grnRepository;
    private final GrnJdbcRepository grnJdbcRepository;
    private final CurrentUserService currentUserService;
    private final StockTransactionService stockTransactionService;

    private final SupplierPaymentService supplierPaymentService;
    private final PurchaseOrderService purchaseOrderService;

    public void createGrn(GrnRequestDto grnRequest) {

        Long employeeId = currentUserService.getEmployeeId();
        if (employeeId == null) {
            throw new RuntimeException("Employee ID not found for current user");
        };

        if (grnRequest.getPurchaseOrderId() != null) {
            validatePurchaseOrder(grnRequest);
        }

        Grn grn = mapToGrn(grnRequest);
        grn.setStatus("DRAFT");
        grn.setEmployeeId(employeeId);

        log.info("Creating GRN: " + grn);

        grnRepository.save(grn);
    }

    public Grn findById(Long id) {
        return grnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GRN not found with id: " + id));
    }

    public GrnRequestDto findRequestDtoById(Long id) {
        GrnRequestDto grnRequestDto = grnRepository.findRequestDtoById(id);
        List<GrnRequestLineDto> items = grnRepository.findGrnRequestLineDtoById(id);
        grnRequestDto.setItems(items);
        return grnRequestDto;
    }

    public GrnRequestDto prepareFromPurchaseOrder(Long purchaseOrderId) {

        Po po = purchaseOrderService.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found with id: " + purchaseOrderId));

        if (!"APPROVED".equals(po.status())) {
            throw new RuntimeException("Only APPROVED purchase orders can be used to create a GRN. PO #"
                    + purchaseOrderId + " is " + po.status());
        }

        if (!grnRepository.findByPurchaseOrderId(purchaseOrderId).isEmpty()) {
            throw new RuntimeException("A GRN already exists for purchase order #" + purchaseOrderId);
        }

        GrnRequestDto grnRequest = new GrnRequestDto();
        grnRequest.setPurchaseOrderId(purchaseOrderId);
        grnRequest.setSupplierId(po.supplierId());
        grnRequest.setSupplierName(po.supplierName());
        grnRequest.setReceivedDate(LocalDate.now());

        grnRequest.setItems(po.items().stream()
                .map(line -> {
                    GrnRequestLineDto item = new GrnRequestLineDto();
                    item.setItemId(line.itemId());
                    item.setItemName(line.name());
                    item.setOrderedQty(BigDecimal.valueOf(line.qty()));
                    item.setOrderedPrice(BigDecimal.valueOf(line.price()));
                    item.setReceivedQty(BigDecimal.valueOf(line.qty()));
                    item.setRejectedQty(BigDecimal.ZERO);
                    item.setAcceptedQty(BigDecimal.valueOf(line.qty()));
                    item.setUnitPrice(BigDecimal.valueOf(line.price()));
                    return item;
                })
                .toList());

        return grnRequest;
    }

    private void validatePurchaseOrder(GrnRequestDto grnRequest) {

        Long purchaseOrderId = grnRequest.getPurchaseOrderId();

        Po po = purchaseOrderService.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found with id: " + purchaseOrderId));

        if (!"APPROVED".equals(po.status())) {
            throw new RuntimeException("Only APPROVED purchase orders can be used to create a GRN. PO #"
                    + purchaseOrderId + " is " + po.status());
        }

        if (!po.supplierId().equals(grnRequest.getSupplierId())) {
            throw new RuntimeException("GRN supplier does not match purchase order supplier");
        }

        boolean otherGrnExists = grnRepository.findByPurchaseOrderId(purchaseOrderId).stream()
                .anyMatch(existing -> !existing.getId().equals(grnRequest.getId()));
        if (otherGrnExists) {
            throw new RuntimeException("A GRN already exists for purchase order #" + purchaseOrderId);
        }
    }

    @Transactional
    public void approveGrn(Long id) {

        Grn grn = findById(id);

        if (!"DRAFT".equals(grn.getStatus())) {
            throw new RuntimeException("Only DRAFT GRNs can be approved");
        }
        
        // Save GRN
        grnRepository.setStatusById(id, "APPROVED");
        
        // Update stock for each item
        grn.getItems().forEach(item -> {
            stockTransactionService.stockIn(
                item.getItemId(),
                (item.getReceivedQty().subtract(item.getRejectedQty())),
                item.getUnitPrice(),
                "GRN",
                id
            );
        });

        if (grn.getCash() != null && grn.getCash().compareTo(BigDecimal.ZERO) > 0)  {

            supplierPaymentService.createPaymentAndAllocate(grn.getId(), new SupplierPaymentRequest(
                grn.getSupplierId(),
                grn.getId(),
                PaymentType.CASH,
                PaymentDirection.OUT,
                grn.getCash(),
                null,
                null,
                null,
                null,
                grn.getReceivedDate()
            ));
        }

        if (grn.getCheque() != null && grn.getCheque().compareTo(BigDecimal.ZERO) > 0)  {

            supplierPaymentService.createPaymentAndAllocate(grn.getId(), new SupplierPaymentRequest(
                grn.getSupplierId(),
                grn.getId(),
                PaymentType.CHEQUE,
                PaymentDirection.OUT,
                grn.getCheque(),
                null,
                null,
                null,
                null,
                grn.getReceivedDate()
            ));
        }

        if (grn.getPurchaseOrderId() != null) {
            purchaseOrderService.markReceived(grn.getPurchaseOrderId());
        }

    }

    public Page<GrnListResponseDto> listGrns(GrnListRequestDto requestDto, Pageable pageable) {
        return grnJdbcRepository.findAllByPage(requestDto, pageable);
    }

    private Grn mapToGrn(GrnRequestDto grnRequest) {
        Grn grn = new Grn();

        if (grnRequest.getId() != null) {
            grn.setId(grnRequest.getId());
        }

        grn.setPurchaseOrderId(grnRequest.getPurchaseOrderId());
        grn.setStatus(grnRequest.getStatus());
        grn.setReceivedDate(grnRequest.getReceivedDate());
        grn.setSupplierId(grnRequest.getSupplierId());
        grn.setEmployeeId(grnRequest.getEmployeeId());
        grn.setCash(grnRequest.getCash() != null ? grnRequest.getCash() : BigDecimal.ZERO);
        grn.setCredit(grnRequest.getCredit() != null ? grnRequest.getCredit() : BigDecimal.ZERO);
        grn.setCheque(grnRequest.getCheque() != null ? grnRequest.getCheque() : BigDecimal.ZERO);
        grn.setCreditDue(grnRequest.getCreditDue());
        grn.setTotal(grnRequest.getGrandTotal() != null ? grnRequest.getGrandTotal() : BigDecimal.ZERO);
        
        // Map items if needed

        grn.setItems(grnRequest.getItems().stream()
                .map(itemDto -> {
                    GrnItem item = new GrnItem();
                    item.setItemId(itemDto.getItemId());
                    item.setItemName(itemDto.getItemName());
                    item.setOrderedQty(itemDto.getOrderedQty());
                    item.setReceivedQty(itemDto.getReceivedQty());
                    item.setRejectedQty(itemDto.getRejectedQty());
                    item.setUnitPrice(itemDto.getUnitPrice());
                    item.setOrderedPrice(itemDto.getOrderedPrice());
                    return item;
                })
                .toList());

        return grn;
    }
}
