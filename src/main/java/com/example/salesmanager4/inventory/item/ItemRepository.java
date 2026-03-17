package com.example.salesmanager4.inventory.item;

import java.util.List;
import java.util.Optional;


import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface ItemRepository extends CrudRepository<Item, Long> {

    Optional<Item> findByCode(String code);

    List<Item> findByActiveTrueOrderByNameAsc();

    List<Item> findByCategoryId(Long categoryId);

    // List<Item> findByActiveTrueOrderByNameAscAndNameContainingIgnoreCase(String name);

    @Query("""
        SELECT * FROM item
        WHERE active = true
        AND name ILIKE CONCAT('%', :name, '%')
        ORDER BY name ASC
        """)
    List<Item> findByName(String name);


    @Query("""
        SELECT i.item_id, i.code, s.name as supplier_name, i.name as item_name, c.name as category_name, i.unit, i.reorder_level, i.active
        FROM item i
        JOIN category c ON i.category_id = c.category_id
        LEFT JOIN supplier s ON i.supplier_id = s.supplier_id
        WHERE i.active = true
        ORDER BY i.name ASC
        """)
    List<ItemListResponseDto> findItemList();

    @Query("""
            SELECT i.item_id, i.code, s.name as supplier_name, i.name as item_name, c.name as category_name, i.unit, i.reorder_level, i.active
            FROM item i
            JOIN category c ON i.category_id = c.category_id
            LEFT JOIN supplier s ON i.supplier_id = s.supplier_id
            WHERE i.active = true
            ORDER BY i.item_id ASC
            LIMIT :limit OFFSET :offset
            """)
    List<ItemListResponseDto> findItemListPaged(int limit, long offset);

    @Query("""
            SELECT count(*)
            FROM item i
            JOIN category c ON i.category_id = c.category_id
            WHERE i.active = true
            """)
    long findItemListPagedCount();

}

