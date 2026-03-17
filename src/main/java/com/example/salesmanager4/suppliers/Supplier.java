package com.example.salesmanager4.suppliers;

import org.springframework.data.annotation.Id;

import lombok.Data;


@Data
public class Supplier {
    @Id
    private Long supplierId;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private boolean active;
}
