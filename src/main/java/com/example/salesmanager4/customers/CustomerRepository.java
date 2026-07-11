package com.example.salesmanager4.customers;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface CustomerRepository extends CrudRepository<Customer, Long>, PagingAndSortingRepository<Customer, Long> {

    Page<Customer> findByActiveIsTrue(Pageable pageable);

    List<Customer> findByActiveIsTrue();

    @Query("""
        SELECT * FROM customer
        WHERE active = true
        AND name ILIKE CONCAT('%', :name, '%')
        ORDER BY name ASC
        """)
    List<Customer> findByName(String name);

}
