package com.example.salesmanager4.employees;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;


public interface EmployeeRepository extends CrudRepository<Employee, Long>, PagingAndSortingRepository<Employee, Long> {
    public Optional<Employee>  findById(Long id);

    Page<Employee> findByActiveIsTrue(Pageable pageable);

    List<Employee> findByActiveIsTrue();

    @Query("""
        SELECT * FROM employee
        WHERE active = true
          AND known_name ILIKE CONCAT('%', :name, '%')
        ORDER BY known_name ASC
        """)
    List<Employee> findByKnownName(String name);
}
