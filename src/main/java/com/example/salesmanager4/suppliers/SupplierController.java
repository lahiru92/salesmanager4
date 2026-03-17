package com.example.salesmanager4.suppliers;

import java.lang.ProcessBuilder.Redirect;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.salesmanager4.inventory.item.Item;
import com.example.salesmanager4.util.Breadcrumb;

@Controller
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService supplierService;
    private final String DEFAULT_PAGE_SIZE = "10";

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    // List suppliers with pagination
    @GetMapping
    public String list(Model model, 
            @RequestParam (defaultValue = "0") int page, 
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Suppliers", null)
        );

        Page<Supplier> suppliersPage = supplierService.findAllByPage(0, 10);

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("mode", "create");
        model.addAttribute("suppliers", suppliersPage.getContent());
        model.addAttribute("page", suppliersPage);
        return "supplier/list::content";
    }

    // Show create supplier form
    @GetMapping("/create")
    public String createForm(Model model) {
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Suppliers", "/suppliers"),
            new Breadcrumb("Create Supplier", null)
        );
        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("mode", "create");
        model.addAttribute("supplier", new Supplier());
        return "supplier/form::content";
    }

    // Handle create supplier form submission
    @PostMapping
    public String create(Supplier supplier, RedirectAttributes ra) {

        supplier.setActive(true);
        supplierService.create(supplier);

        ra.addFlashAttribute("message", "Supplier created");
        return "redirect:/suppliers/create";

    }

    // Show edit supplier form
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, 
                            Model model, 
                            @RequestParam(defaultValue = "0") int page, 
                            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
                                
        Supplier supplier = supplierService.findById(id);
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Suppliers", "/suppliers"),
            new Breadcrumb("Edit Supplier", null)
        );
        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("mode", "edit");
        model.addAttribute("supplier", supplier);
        model.addAttribute("page",page);
        model.addAttribute("size",size);
        model.addAttribute("suppliers", supplierService.findAllActive());
        return "supplier/form::content";
    }

    // Handle edit supplier form submission
    @PutMapping()
    public String update(@ModelAttribute Supplier supplier, RedirectAttributes ra, Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        supplierService.update(supplier);
        ra.addFlashAttribute("message", "Supplier updated");

        return list(model, page, size);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> disable(@PathVariable("id") Long id) {
        supplierService.disable(id);
        return ResponseEntity.ok().build();
    }
}
