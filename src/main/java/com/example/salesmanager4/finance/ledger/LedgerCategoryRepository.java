package com.example.salesmanager4.finance.ledger;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface LedgerCategoryRepository extends CrudRepository<LedgerCategory, Long> {

    @Query("SELECT * FROM ledger_category WHERE active = TRUE ORDER BY kind, name")
    List<LedgerCategory> findActive();

    @Query("SELECT * FROM ledger_category ORDER BY kind, name")
    List<LedgerCategory> findAllOrdered();
}
