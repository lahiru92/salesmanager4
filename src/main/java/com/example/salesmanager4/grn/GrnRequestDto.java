package com.example.salesmanager4.grn;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Valid
public class GrnRequestDto {

    @NotNull
    Long id;

    @NotNull
    Long purchaseOrderId;
    
    String status;
    LocalDate receivedDate;

    @NotNull
    Long supplierId;
    String supplierName;

    Long employeeId;
    String employeeName;

    @NotEmpty
    @Valid
    List<GrnRequestLineDto> items;

    public BigDecimal getGrandTotal() {
        if (items != null) {
            return items.stream()
                        .map(GrnRequestLineDto::getSubTotal)
                        .filter(subTotal -> subTotal != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return BigDecimal.ZERO;
    }
}
