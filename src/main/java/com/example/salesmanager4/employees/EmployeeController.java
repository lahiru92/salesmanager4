package com.example.salesmanager4.employees;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.salesmanager4.util.Breadcrumb;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;
    private static final String DEFAULT_PAGE_SIZE = "10";

    public EmployeeController(EmployeeService employeeService, EmployeeRepository employeeRepository) {
        this.employeeService = employeeService;
        this.employeeRepository = employeeRepository;
    }

    // List employees with pagination
    @GetMapping
    public String list(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Employees", null)
        );

        Page<Employee> employeesPage = employeeService.findAllByPage(page, size);

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("employees", employeesPage.getContent());
        model.addAttribute("page", employeesPage);
        return "employee/list::content";
    }

    // Show create employee form
    @GetMapping("/create")
    public String createForm(Model model) {
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Employees", "/employees"),
            new Breadcrumb("Create Employee", null)
        );
        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("mode", "create");
        model.addAttribute("employee", new Employee());
        return "employee/form::content";
    }

    // Handle create employee form submission
    @PostMapping
    public String create(Employee employee, RedirectAttributes ra) {
        employeeService.create(employee);
        ra.addFlashAttribute("message", "Employee created");
        return "redirect:/employees/create";
    }

    // Show edit employee form
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id,
                           Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {

        Employee employee = employeeService.findById(id);
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Employees", "/employees"),
            new Breadcrumb("Edit Employee", null)
        );
        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("mode", "edit");
        model.addAttribute("employee", employee);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "employee/form::content";
    }

    // Handle edit employee form submission
    @PutMapping
    public String update(@ModelAttribute Employee employee, RedirectAttributes ra, Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {

        employeeService.update(employee);
        ra.addFlashAttribute("message", "Employee updated");
        return list(model, page, size);
    }

    // Disable employee (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> disable(@PathVariable("id") Long id) {
        employeeService.disable(id);
        return ResponseEntity.ok().build();
    }

    // Endpoint for employee dropdown list (Tom-Select)
    @GetMapping("/api/list")
    @ResponseBody
    public List<Employee> employeeList(@RequestParam String q) {
        return employeeRepository.findByKnownName(q);
    }
}
