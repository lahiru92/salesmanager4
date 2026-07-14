package com.example.salesmanager4.employees;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    /**
     * Search active employees by known name, full name, designation, mobile or NIC.
     * Filters in memory (the employee roster is small) and paginates the result.
     */
    Page<Employee> search(String query, int page, int size) {
        List<Employee> matches = employeeRepo.findByActiveIsTrue().stream()
                .filter(e -> matches(e, query))
                .sorted(Comparator.comparing(
                        e -> e.getKnownName() == null ? "" : e.getKnownName(),
                        String.CASE_INSENSITIVE_ORDER))
                .toList();

        int from = Math.min(page * size, matches.size());
        int to = Math.min(from + size, matches.size());
        return new PageImpl<>(matches.subList(from, to), PageRequest.of(page, size), matches.size());
    }

    private boolean matches(Employee e, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String q = query.toLowerCase();
        return contains(e.getKnownName(), q)
                || contains(e.getFullName(), q)
                || contains(e.getDesignation(), q)
                || contains(e.getPhoneMobile(), q)
                || contains(e.getNicNumber(), q);
    }

    private boolean contains(String value, String lowerQuery) {
        return value != null && value.toLowerCase().contains(lowerQuery);
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
