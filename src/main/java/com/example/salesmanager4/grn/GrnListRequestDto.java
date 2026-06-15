package com.example.salesmanager4.grn;

import java.time.LocalDate;

import lombok.Data;

@Data
public class GrnListRequestDto {
    private String status;
    private LocalDate fromReceivedDate;
    private LocalDate toReceivedDate;
    private Long supplierId;
    private Long employeeId;
}
