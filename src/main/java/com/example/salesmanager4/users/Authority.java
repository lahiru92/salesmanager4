package com.example.salesmanager4.users;

import org.springframework.data.relational.core.mapping.Table;

@Table("authorities")
public record Authority(
    String authority
) {

    public String getAuthority() {
        if (authority.startsWith("ROLE_")) {
            return authority.substring(5);
        }
        return authority;
    }
}
