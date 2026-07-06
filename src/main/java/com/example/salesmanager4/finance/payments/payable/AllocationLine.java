package com.example.salesmanager4.finance.payments.payable;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AllocationLine {

    private Long grnId;
    private BigDecimal amount;

}
