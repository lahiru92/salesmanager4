package com.example.salesmanager4.grn;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.salesmanager4.inventory.stock.StockTransactionService;
import com.example.salesmanager4.users.CurrentUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GrnService {

    private final GrnRepository grnRepository;
    private final CurrentUserService currentUserService;
    private final StockTransactionService stockTransactionService;

    public void createGrn(GrnRequestDto grnRequest) {

        Long employeeId = currentUserService.getEmployeeId();
        if (employeeId == null) {
            throw new RuntimeException("Employee ID not found for current user");
        };

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

    @Transactional
    public void approveGrn(Long id) {
        Grn grn = findById(id);
        if (!"DRAFT".equals(grn.getStatus())) {
            throw new RuntimeException("Only DRAFT GRNs can be approved");
        }
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
