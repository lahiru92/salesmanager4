package com.example.salesmanager4;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String index(Model model, HttpServletRequest request) {
        //return partial only if it's an HTMX request
        if (request.getHeader("HX-Request") != null) {
            return "dashboard :: content";
        }
        model.addAttribute("content", "dashboard");
        return "index";
    }
}
