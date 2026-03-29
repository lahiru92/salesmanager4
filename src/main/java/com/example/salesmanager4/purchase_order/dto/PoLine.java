package com.example.salesmanager4.purchase_order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Valid
public record PoLine(
    @NotNull
    @Positive
    Integer itemId, 
    
    String name,
    
    @NotNull
    @Positive
    Integer qty, 
    
    @NotNull
    @PositiveOrZero
    Double price) {
    
}
