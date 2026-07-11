package com.example.salesmanager4.invoice;

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
public class InvoiceRequestDto {

    Long id;

    String status;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate invoiceDate;

    @NotNull
    Long customerId;
    String customerName;

    Long employeeId;
    String employeeName;

    BigDecimal cash;
    BigDecimal credit;
    BigDecimal cheque;
    LocalDate creditDue;

    @NotEmpty
    @Valid
    List<InvoiceRequestLineDto> items;


    public BigDecimal getGrandTotal() {
        if (items != null) {
            BigDecimal grandTotal = items.stream()
                        .map(InvoiceRequestLineDto::getSubTotal)
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
