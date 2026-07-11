package com.example.salesmanager4.employees;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    List<EmployeeDto> employees = List.of(
            new EmployeeDto("E001", "Alice Johnson", "Sales Manager"),
            new EmployeeDto("E002", "Bob Smith", "Sales Associate"),
            new EmployeeDto("E003", "Charlie Brown", "Accountant")
        );

    @GetMapping("/list")
    public String dropdownList(@RequestParam(name="q") String q, Model model) {

        model.addAttribute("employees", employees.stream().filter(e -> e.name().toUpperCase().contains(q.toUpperCase())).toList());
        return "fragments/employee_dropdown_list";
    }

    // Endpoint for employee dropdown list (Tom-Select)
    @GetMapping("/api/list")
    @ResponseBody
    public List<Employee> employeeList(@RequestParam String q) {
        return employeeRepository.findByKnownName(q);
    }

}

record EmployeeDto(
    String id,
    String name,
    String role) {
}