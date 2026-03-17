package com.example.salesmanager4.Test1;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.salesmanager4.suppliers.SupplierRepository;
import com.example.salesmanager4.suppliers.SupplierService;

@SpringBootTest
public class SupplierTest {
    
    @Autowired
    SupplierRepository supplierRepo;

    @Autowired
    SupplierService supplierService;

    @Test
    public void repoFindAllActive() {
        System.out.println("===Find all active suppliers=======================");
        supplierRepo.findByActiveIsTrue().forEach(System.out::println);
        System.out.println("===Find all========================================");
        supplierRepo.findAll().forEach(System.out::println);
    }

    @Test
    public void serviceFindAllActive() {
        System.out.println("===Find all active suppliers=======================");
        supplierService.findAllActive().forEach(System.out::println);
        System.out.println("===Find all========================================");
        supplierService.findAllActive().forEach(System.out::println);
    }
}
