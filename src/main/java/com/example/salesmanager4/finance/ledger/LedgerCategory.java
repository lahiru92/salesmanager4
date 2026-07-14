package com.example.salesmanager4.finance.ledger;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Data
@Table("ledger_category")
public class LedgerCategory {

    @Id
    private Long id;
    private String name;
    private LedgerKind kind;
    private boolean active;
}
