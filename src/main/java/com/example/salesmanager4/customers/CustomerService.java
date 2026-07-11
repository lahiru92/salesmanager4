package com.example.salesmanager4.customers;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepo;

    public CustomerService(CustomerRepository customerRepo) {
        this.customerRepo = customerRepo;
    }

    void create(Customer customer) {
        customerRepo.save(customer);
    }

    void update(Customer customer) {
        Optional<Customer> existingOpt = customerRepo.findById(customer.getCustomerId());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Customer not found");
        }
        customerRepo.save(customer);
    }

    void disable(Long customerId) {
        Optional<Customer> existingOpt = customerRepo.findById(customerId);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Customer not found");
        }
        Customer existing = existingOpt.get();
        existing.setActive(false);
        customerRepo.save(existing);
    }

    Page<Customer> findAllByPage(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        return customerRepo.findByActiveIsTrue(pageable);
    }

    public Customer findById(Long customerId) {
        return customerRepo.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }

    public List<Customer> findAllActive() {
        return customerRepo.findByActiveIsTrue();
    }

    public List<Customer> findByName(String name) {
        return customerRepo.findByName(name);
    }
}
