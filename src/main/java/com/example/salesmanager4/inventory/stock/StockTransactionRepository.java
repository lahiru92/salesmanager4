package com.example.salesmanager4.inventory.stock;

import java.math.BigDecimal;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface StockTransactionRepository
        extends CrudRepository<StockTransaction, Long> {

    @Query("""
        SELECT COALESCE(
            SUM(CASE 
                WHEN txn_type = 'IN' THEN quantity 
                ELSE -quantity 
            END), 0)
        FROM stock_transaction
        WHERE item_id = :itemId
    """)
    BigDecimal getCurrentStock(Long itemId);
}
