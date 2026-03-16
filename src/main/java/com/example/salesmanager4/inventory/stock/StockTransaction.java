package com.example.salesmanager4.inventory.stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Table("stock_transaction")
@Data
public class StockTransaction {

    @Id
    private Long stockTxnId;

    private Long itemId;
    private LocalDateTime txnDate;
    private String txnType;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private String referenceType;
    private Long referenceId;
    private String remarks;
}
