package com.example.salesmanager4.purchase_order.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Valid
public record Po( 
    
    @NotNull
    Long supplierId, 

    String supplierName,
    
    @NotBlank
    String orderDate, 
    
    @NotEmpty
    @Valid
    List<PoLine> items) {
    
}
