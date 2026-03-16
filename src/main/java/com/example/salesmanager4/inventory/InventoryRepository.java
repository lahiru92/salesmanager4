package com.example.salesmanager4.inventory;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;

import com.example.salesmanager4.inventory.item.Item;

public interface InventoryRepository extends Repository<Item, Long> {

    @Query("""
        SELECT
            i.item_id AS itemId,
            i.code AS code,
            i.name AS name,
            c.name AS categoryname,

            COALESCE(
                SUM(CASE 
                    WHEN st.txn_type = 'IN' THEN st.quantity 
                    ELSE -st.quantity 
                END), 0
            ) AS stockqty,

            COALESCE(
                SUM(CASE 
                    WHEN st.txn_type = 'IN' THEN st.quantity * st.unit_cost 
                    ELSE 0 
                END)
                /
                NULLIF(
                    SUM(CASE 
                        WHEN st.txn_type = 'IN' THEN st.quantity 
                        ELSE 0 
                    END), 0
                ), 0
            ) AS avgcost

        FROM item i
        JOIN category c ON c.category_id = i.category_id
        LEFT JOIN stock_transaction st ON st.item_id = i.item_id
        WHERE i.active = TRUE
        GROUP BY i.item_id, i.code, i.name, c.name
    """)
    List<InventorySummaryView> findInventorySummary();

    
}

