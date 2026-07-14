package com.example.salesmanager4;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.salesmanager4.dashboard.DashboardRepository;
import com.example.salesmanager4.employees.EmployeeRepository;
import com.example.salesmanager4.users.CurrentUserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class HomeController {

    private final DashboardRepository dashboardRepository;
    private final CurrentUserService currentUserService;
    private final EmployeeRepository employeeRepository;

    @GetMapping
    public String index(Model model, HttpServletRequest request) {
        LocalDate today = LocalDate.now();
        Long employeeId = currentUserService.getEmployeeId();
        String employeeName = employeeRepository.findById(employeeId).get().getKnownName();

        model.addAttribute("today", today);
        model.addAttribute("stats", dashboardRepository.getStats(today));
        model.addAttribute("employeeName", employeeName);

        //return partial only if it's an HTMX request
        if (request.getHeader("HX-Request") != null) {
            return "dashboard :: content";
        }
        
        model.addAttribute("content", "dashboard");
        return "index";
    }


}

