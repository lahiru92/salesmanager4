package com.example.salesmanager4.inventory.stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.salesmanager4.inventory.item.Item;
import com.example.salesmanager4.inventory.item.ItemRepository;

@Service
@Transactional
public class StockTransactionService {

    private final StockTransactionRepository stockRepo;
    private final ItemRepository itemRepo;

    public StockTransactionService(StockTransactionRepository stockRepo,
                                   ItemRepository itemRepo) {
        this.stockRepo = stockRepo;
        this.itemRepo = itemRepo;
    }

    // ---------------- STOCK IN ----------------
    public void stockIn(Long itemId,
                        BigDecimal qty,
                        BigDecimal unitCost,
                        String refType,
                        Long refId) {

        validateItem(itemId);
        validatePositive(qty);

        if (unitCost == null || unitCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit cost required for stock IN");
        }

        StockTransaction txn = new StockTransaction();
        txn.setItemId(itemId);
        txn.setTxnDate(LocalDateTime.now());
        txn.setTxnType("IN");
        txn.setQuantity(qty);
        txn.setUnitCost(unitCost);
        txn.setReferenceType(refType);
        txn.setReferenceId(refId);

        stockRepo.save(txn);
    }

    // ---------------- STOCK OUT ----------------
    public void stockOut(Long itemId,
                         BigDecimal qty,
                         String refType,
                         Long refId) {

        validateItem(itemId);
        validatePositive(qty);

        BigDecimal currentStock = stockRepo.getCurrentStock(itemId);
        if (currentStock.compareTo(qty) < 0) {
            throw new IllegalStateException("Insufficient stock");
        }

        StockTransaction txn = new StockTransaction();
        txn.setItemId(itemId);
        txn.setTxnDate(LocalDateTime.now());
        txn.setTxnType("OUT");
        txn.setQuantity(qty);
        txn.setReferenceType(refType);
        txn.setReferenceId(refId);

        stockRepo.save(txn);
    }


    public void adjustStock(Long itemId,
                        BigDecimal qty,
                        BigDecimal unitCost,
                        boolean increase,
                        String reason) {

        validateItem(itemId);
        validatePositive(qty);

        if (increase) {
            if (unitCost == null || unitCost.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Unit cost required for stock increase");
            }
        } else {
            checkStockAvailable(itemId, qty);
        }

        StockTransaction txn = new StockTransaction();
        txn.setItemId(itemId);
        txn.setTxnDate(LocalDateTime.now());
        txn.setTxnType(increase ?"IN" : "OUT");
        txn.setQuantity(qty);
        txn.setUnitCost(unitCost);
        txn.setReferenceType("ADJUSTMENT");
        txn.setReferenceId(null);
        txn.setRemarks(reason);

        stockRepo.save(txn);
    }

    // ---------------- HELPERS ----------------
    private void validateItem(Long itemId) {
        itemRepo.findById(itemId)
            .filter(Item::isActive)
            .orElseThrow(() ->
                new IllegalArgumentException("Invalid or inactive item"));
    }

    private void validatePositive(BigDecimal qty) {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public void checkStockAvailable(Long itemId, BigDecimal qty) {

        BigDecimal currentStock = stockRepo.getCurrentStock(itemId);

        if (currentStock.compareTo(qty) < 0) {
            throw new IllegalStateException(
                    "Insufficient stock for item ID: " + itemId);
        }
    }
}
