package com.example.salesmanager4.users;

import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class LoginController {

    @GetMapping("/login")
    // public String login() {
    //     return "login"; 
    // }

    public String login(HttpServletRequest request, Model model) {
    HttpSession session = request.getSession(false);
    if (session != null) {
        AuthenticationException ex = (AuthenticationException) session
                .getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        if (ex != null) {
            model.addAttribute("errorMessage", ex.getMessage());
            log.info("Login error: " + ex.getMessage());
        }
    }
    return "login";
}
}
