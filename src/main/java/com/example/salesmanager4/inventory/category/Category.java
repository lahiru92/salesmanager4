package com.example.salesmanager4.inventory.category;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Table("category")
@Data
public class Category {

    @Id
    private Long categoryId;

    private String name;
    private String normalizedName;

}
