package com.example.salesmanager4.grn;

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

}
