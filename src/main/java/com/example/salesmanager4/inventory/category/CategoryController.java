package com.example.salesmanager4.inventory.category;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", service.findAll());
        return "category/list::content";
    }

    // @PostMapping
    // public String create(@RequestParam String name,
    //                      RedirectAttributes ra) {
    //     service.create(name);
    //     ra.addFlashAttribute("success", "Category created");
    //     return "redirect:/categories";
    // }

    @PostMapping
    public String create(@RequestParam String name, Model model) {
        service.create(name);
        model.addAttribute("categories", service.findAll());
        return "category/list::category-list";
    }
}

