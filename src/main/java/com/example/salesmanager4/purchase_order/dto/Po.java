package com.example.salesmanager4.purchase_order.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Valid
public record Po(

    Long id,
    
    @NotNull
    Long supplierId, 
    String supplierName,
    
    @NotBlank
    String orderDate, 

    String status,

    Long createdBy,
    String createdByName,

    LocalDateTime createdAt,
    
    @NotEmpty
    @Valid
    List<PoLine> items) {

    public Po() {
        this(null, null, null, null, null, null, null, null, List.of());
    }

    public double getGrandTotal() {

        if (items == null || items.isEmpty()) {
            return 0.0;
        }

        return items.stream()
            .map(line -> line.qty() * line.price())
            .reduce(0.0, (res,elem) -> res + elem);
    }
    
}
