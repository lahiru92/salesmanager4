package com.example.salesmanager4.users;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public record UserDTO(
    @Id String username,
    String password,
    boolean enabled,
    // Spring Data JDBC looks for a table named 'authorities' with 'username' as the FK
    @Column("username")
    Set<Authority> authorities
) {}