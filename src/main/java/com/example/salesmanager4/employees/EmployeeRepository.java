package com.example.salesmanager4.employees;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;


public interface EmployeeRepository extends CrudRepository<Employee, Long> {
    public Optional<Employee>  findById(Long id);

    @Query("""
        SELECT * FROM employee
        WHERE known_name ILIKE CONCAT('%', :name, '%')
        ORDER BY known_name ASC
        """)
    List<Employee> findByKnownName(String name);
}
