package com.example.salesmanager4.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class InvoiceListResponseDto {

    private Long id;
    private String status;
    private LocalDate invoiceDate;
    private String customerName;
    private String employeeName;
    private BigDecimal total;
    private BigDecimal cash;
    private BigDecimal cheque;
    private BigDecimal credit;

}
