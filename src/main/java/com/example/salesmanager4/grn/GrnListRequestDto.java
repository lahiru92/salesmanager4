package com.example.salesmanager4.grn;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class GrnListRequestDto {
    private String status;
    private LocalDate fromReceivedDate;
    private LocalDate toReceivedDate;
    private Long supplierId;
    private Long employeeId;
    private BigDecimal total;
    private BigDecimal cash;
    private BigDecimal cheque;
    private BigDecimal credit;
}
