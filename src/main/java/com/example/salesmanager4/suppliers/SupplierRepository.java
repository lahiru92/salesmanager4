package com.example.salesmanager4.suppliers;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface SupplierRepository extends CrudRepository<Supplier, Long>, PagingAndSortingRepository<Supplier, Long> {

    Page<Supplier> findByActiveIsTrue(Pageable pageable);

    List<Supplier> findByActiveIsTrue();

    @Query("""
        SELECT * FROM supplier
        WHERE active = true
        AND name ILIKE CONCAT('%', :name, '%')
        ORDER BY name ASC
        """)
    List<Supplier> findByName(String name);
    
}
