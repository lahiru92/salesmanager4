package com.example.salesmanager4.inventory.item;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;


@Table("item")
@Data
public class Item {

    @Id
    private Long itemId;

    private String code;
    private String name;
    private Long categoryId;
    private String unit;
    private BigDecimal reorderLevel;
    private boolean active;
}
