package com.example.salesmanager4.inventory;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.salesmanager4.inventory.item.ItemService;
import com.example.salesmanager4.util.Breadcrumb;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final ItemService itemService;

    public InventoryController(InventoryService inventoryService,
                               ItemService itemService
    ) {
        this.inventoryService = inventoryService;
        this.itemService = itemService;
    }

    // List<ItemsDto> items = List.of(
    //         new ItemsDto(1L, 100L, 10.0, "Apple"),
    //         new ItemsDto(2L, 200L, 20.0, "Banana"),
    //         new ItemsDto(3L, 300L, 20.0, "Peach"),
    //         new ItemsDto(4L, 400L, 20.0, "Blackberry"),
    //         new ItemsDto(5L, 500L, 20.0, "Grapes"),
    //         new ItemsDto(6L, 600L, 30.0, "Orange")
    //     );

    @GetMapping("/itemlist")
    public String dropdownList(@RequestParam(name="q") String q, Model model) {

        // model.addAttribute("items", items.stream().filter(r -> r.name().toUpperCase().contains(q.toUpperCase())).toList());
        model.addAttribute("items", itemService.findItemByName(q));
        return "fragments/items_dropdown_list";
    }


    @GetMapping("/summary")
    @ResponseBody
    public List<InventorySummaryView> summaryTest(Model model, HttpServletResponse response) {
        return inventoryService.getInventorySummary();
    }

    @GetMapping
    public String summary(Model model) {


        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Inventory", null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("inventory",
            inventoryService.getInventorySummary());
        return "inventory/summary";
    }

}

record ItemsDto(
    Long id,
    Long batchId,
    Double price,
    String name) {
}