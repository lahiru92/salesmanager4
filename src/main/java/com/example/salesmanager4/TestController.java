package com.example.salesmanager4;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/test")
public class TestController {

    public String toastTest() {
        return "test/toast-test";
    }

    @GetMapping("my-authorities")
    @ResponseBody
    public Collection<? extends GrantedAuthority> checkMyAuthorities(Authentication authentication) {
        // This returns the exact list of authorities loaded from your DB tables
        return authentication.getAuthorities();
    }
}
