package com.example.salesmanager4.inventory.item;

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

import com.example.salesmanager4.inventory.category.CategoryService;
import com.example.salesmanager4.suppliers.SupplierService;
import com.example.salesmanager4.util.Breadcrumb;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/items")
@Slf4j
public class ItemController {

    private final ItemService itemService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;

    private final String DEFAULT_PAGE_SIZE = "10";

    public ItemController(ItemService itemService,
                          CategoryService categoryService,
                          SupplierService supplierService) {
        this.itemService = itemService;
        this.categoryService = categoryService;
        this.supplierService = supplierService;
    }

    @GetMapping("/dropdown")
    public String dropdownList(@RequestParam String q, Model model) {
        model.addAttribute("items", itemService.findItemByName(q));
        return "fragments/items_dropdown_list";
    }

    @GetMapping("/api/list")
    @ResponseBody
    public List<Item> dropdownList(@RequestParam String q) {
        return itemService.findItemByName(q);
    }

    @GetMapping("/search")
    public String searchItems(@RequestParam String query, Model model) {
        List<Item> items = itemService.findItemByName(query);
        model.addAttribute("items", items);
        return "item/search-results";
    }

    

    @GetMapping
    public String list(Model model, 
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Items", null)
        );

        Page<ItemListResponseDto> itemsPage = itemService.listFilterdPaged(page, size);
        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("items", itemsPage.getContent());
        model.addAttribute("page", itemsPage);

        return "item/list::content";
    }

    @GetMapping("/create")
    public String form(Model model) {
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Items", "/items"),
            new Breadcrumb("Create Item", null)
        );
        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("mode", "create");
        model.addAttribute("item", new Item());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("suppliers", supplierService.findAllActive());
        return "item/form::content";
    }

    @PostMapping
    public String save(@ModelAttribute Item item,
                       RedirectAttributes ra) {
        itemService.create(item);
        ra.addFlashAttribute("message", "Item created");
        return "redirect:/items/create";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, 
                            Model model, 
                            @RequestParam(defaultValue = "0") int page, 
                            @RequestParam(defaultValue = "${DEFAULT_PAGE_SIZE}") int size) {
                                
        Item item = itemService.findById(id);
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Items", "/items"),
            new Breadcrumb("Edit Item", null)
        );
        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("mode", "edit");
        model.addAttribute("item", item);
        model.addAttribute("page",page);
        model.addAttribute("size",size);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("suppliers", supplierService.findAllActive());
        return "item/form::content";
    }

    @PutMapping("/edit")
    public String update(@ModelAttribute Item item, RedirectAttributes ra, Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        // TODO: Fix too many redirects
        //       This is caused by the form. Find out how to dynamically
        //       change hx-put or hx-post with thymeleaf.
        // TODO: Implement actual update
        itemService.update(item);
        ra.addFlashAttribute("message", "Item updated");
        
        return list(model, page, size);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> disable(@PathVariable("id") Long id) {
        itemService.disable(id);
        return ResponseEntity.ok().build();
    }

}

