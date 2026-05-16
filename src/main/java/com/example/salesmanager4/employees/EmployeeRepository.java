package com.example.salesmanager4.employees;

import org.springframework.data.repository.CrudRepository;
import java.util.Optional;


public interface EmployeeRepository extends CrudRepository<Employee, Long> {
    public Optional<Employee>  findById(Long id);
}
