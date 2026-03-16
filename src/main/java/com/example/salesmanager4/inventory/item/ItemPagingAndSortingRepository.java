package com.example.salesmanager4.inventory.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ItemPagingAndSortingRepository extends PagingAndSortingRepository<Item, Long> {
    @Query("""
            SELECT i.item_id, i.code, i.name as item_name, c.name as category_name, i.unit, i.reorder_level, i.active
            FROM item i
            JOIN category c ON i.category_id = c.category_id
            WHERE i.active = true
            ORDER BY i.name ASC
            """)
    Page<ItemListResponseDto> findItemListPaged(Pageable pageable);
}
