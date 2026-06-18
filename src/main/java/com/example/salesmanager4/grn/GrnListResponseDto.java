package com.example.salesmanager4.grn;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class GrnListResponseDto {

    private Long id;
    private Long purchaseOrderId;
    private String status;
    private LocalDate receivedDate;
    private String supplierName;
    private String employeeName;
    private BigDecimal total;
    private BigDecimal cash;
    private BigDecimal cheque;
    private BigDecimal credit;

}
