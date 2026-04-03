package com.example.salesmanager4.suppliers;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepo;

    public SupplierService(SupplierRepository supplierRepo) {
        this.supplierRepo = supplierRepo;
    }

    void create(Supplier supplier) {
        supplierRepo.save(supplier);
    }

    void update(Supplier supplier) {
        Optional<Supplier> existingOpt = supplierRepo.findById(supplier.getSupplierId());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Supplier not found");
        }
        supplierRepo.save(supplier);
    }

    void disable(Long supplierId) {
        Optional<Supplier> existingOpt = supplierRepo.findById(supplierId);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Supplier not found");
        }
        Supplier existing = existingOpt.get();
        existing.setActive(false);
        supplierRepo.save(existing);
    }

    Page<Supplier> findAllByPage(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        return supplierRepo.findByActiveIsTrue(pageable);
    }

    public Supplier findById(Long supplierId) {
        return supplierRepo.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
    }

    public List<Supplier> findAllActive() {
        return supplierRepo.findByActiveIsTrue();
    }
    
    public List<Supplier> findByName(String name) {
        return supplierRepo.findByName(name);
    }
}
