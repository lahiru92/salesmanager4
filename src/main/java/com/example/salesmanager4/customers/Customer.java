package com.example.salesmanager4.customers;

import org.springframework.data.annotation.Id;

import lombok.Data;


@Data
public class Customer {
    @Id
    private Long customerId;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private boolean active;
}
