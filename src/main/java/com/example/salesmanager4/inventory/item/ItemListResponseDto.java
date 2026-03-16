package com.example.salesmanager4.inventory.item;

// item_id, code, a.name, b.name as category_name, reorder_level, active
public record ItemListResponseDto(
    Long itemId, 
    String code, 
    String itemName, 
    String categoryName, 
    String unit,
    Integer reorderLevel, 
    Boolean active) {

}
