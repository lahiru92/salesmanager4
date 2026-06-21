package com.example.salesmanager4.creditors.creditortransaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;

import com.example.salesmanager4.common.RefType;

import lombok.Data;


@Data
public class CreditorTransaction {

    // id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    // supplier_id bigint,
    // txn_date DATE,
    // txn_type VARCHAR,
    // amount numeric(12,2),
    // ref_type varchar,
    // ref_id bigint,

    @Id
    Long id;
    Long supplierId;
    LocalDate txnDate;
    CreditorTxnType txnType;
    BigDecimal amount;
    LocalDate dueDate;
    RefType refType;
    Long refId;

    public CreditorTransaction(
            Long supplierId,
            CreditorTxnType txnType,
            BigDecimal amount,
            LocalDate dueDate,
            RefType refType,
            Long refId) {

        this.id = null;
        this.supplierId = supplierId;
        this.txnDate = LocalDate.now();
        this.txnType = txnType;
        this.amount = amount;
        this.dueDate = dueDate;
        this.refType = refType;
        this.refId = refId;
    }
}
