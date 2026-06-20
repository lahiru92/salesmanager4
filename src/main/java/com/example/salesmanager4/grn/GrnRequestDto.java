package com.example.salesmanager4.grn;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Valid
public class GrnRequestDto {

    Long id;

    Long purchaseOrderId;
    
    String status;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
    LocalDate receivedDate;

    @NotNull
    Long supplierId;
    String supplierName;

    Long employeeId;
    String employeeName;

    BigDecimal cash;
    BigDecimal credit;
    BigDecimal cheque;
    LocalDate creditDue;

    @NotEmpty
    @Valid
    List<GrnRequestLineDto> items;


    public BigDecimal getGrandTotal() {
        if (items != null) {
            BigDecimal grandTotal = items.stream()
                        .map(GrnRequestLineDto::getSubTotal)
                        .filter(subTotal -> subTotal != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            return grandTotal.setScale(2, RoundingMode.HALF_UP);

        }
        return BigDecimal.ZERO;
    }

    public boolean isBalanced() {
        BigDecimal grandTotal = this.getGrandTotal();
        BigDecimal payment = Stream.of(cash, cheque, credit)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return grandTotal.compareTo(payment) == 0;

    }
}
