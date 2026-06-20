package com.example.salesmanager4.cash;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;

import com.example.salesmanager4.common.RefType;

import lombok.Data;

@Data
public class CashTransaction  {
    @Id
    Long id;
    LocalDate txnDate;
    CashTxnType txnType;
    BigDecimal amount;  
    RefType refType;
    Long refId; 

    public CashTransaction(CashTxnType txnType, BigDecimal amount, RefType refType, Long refId) {
        this.id = null;
        this.txnDate = LocalDate.now();
        this.txnType = txnType;
        this.amount = amount;
        this.refType = refType;
        this.refId = refId;
    }
}
