package com.example.salesmanager4.employees;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepo;

    public EmployeeService(EmployeeRepository employeeRepo) {
        this.employeeRepo = employeeRepo;
    }

    void create(Employee employee) {
        employee.setActive(true);
        employeeRepo.save(employee);
    }

    void update(Employee employee) {
        Optional<Employee> existingOpt = employeeRepo.findById(employee.getId());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Employee not found");
        }
        employee.setActive(true);
        employeeRepo.save(employee);
    }

    void disable(Long id) {
        Optional<Employee> existingOpt = employeeRepo.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Employee not found");
        }
        Employee existing = existingOpt.get();
        existing.setActive(false);
        employeeRepo.save(existing);
    }

    Page<Employee> findAllByPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeRepo.findByActiveIsTrue(pageable);
    }

    public Employee findById(Long id) {
        return employeeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    }

    public List<Employee> findAllActive() {
        return employeeRepo.findByActiveIsTrue();
    }

    public List<Employee> findByName(String name) {
        return employeeRepo.findByKnownName(name);
    }
}
