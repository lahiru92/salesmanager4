package com.example.salesmanager4.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class InvoiceListRequestDto {
    private String status;
    private LocalDate fromInvoiceDate;
    private LocalDate toInvoiceDate;
    private Long customerId;
    private Long employeeId;
    private BigDecimal total;
    private BigDecimal cash;
    private BigDecimal cheque;
    private BigDecimal credit;
}
