package com.example.salesmanager4.customers;

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
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final String DEFAULT_PAGE_SIZE = "10";

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // List customers with pagination
    @GetMapping
    public String list(Model model,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Customers", null)
        );

        Page<Customer> customersPage = customerService.findAllByPage(0, 10);

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("mode", "create");
        model.addAttribute("customers", customersPage.getContent());
        model.addAttribute("page", customersPage);
        return "customer/list::content";
    }

    // Show create customer form
    @GetMapping("/create")
    public String createForm(Model model) {
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Customers", "/customers"),
            new Breadcrumb("Create Customer", null)
        );
        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("mode", "create");
        model.addAttribute("customer", new Customer());
        return "customer/form::content";
    }

    // Handle create customer form submission
    @PostMapping
    public String create(Customer customer, RedirectAttributes ra) {

        customer.setActive(true);
        customerService.create(customer);

        ra.addFlashAttribute("message", "Customer created");
        return "redirect:/customers/create";

    }

    // Show edit customer form
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id,
                            Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {

        Customer customer = customerService.findById(id);
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Customers", "/customers"),
            new Breadcrumb("Edit Customer", null)
        );
        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("mode", "edit");
        model.addAttribute("customer", customer);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "customer/form::content";
    }

    // Handle edit customer form submission
    @PutMapping()
    public String update(@ModelAttribute Customer customer, RedirectAttributes ra, Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        customerService.update(customer);
        ra.addFlashAttribute("message", "Customer updated");

        return list(model, page, size);

    }

    // Disable customer (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> disable(@PathVariable("id") Long id) {
        customerService.disable(id);
        return ResponseEntity.ok().build();
    }

    // Endpoint for customer dropdown list (Tom-Select)
    @GetMapping("/api/list")
    @ResponseBody
    public List<Customer> customerList(@RequestParam String q) {
        return customerService.findByName(q);
    }
}
