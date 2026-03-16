package com.example.salesmanager4.inventory.stock;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.salesmanager4.inventory.item.ItemService;
import com.example.salesmanager4.util.Breadcrumb;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/stock-adjustments")
public class StockAdjustmentController {

    private final StockTransactionService stockService;
    private final ItemService itemService;

    public StockAdjustmentController(
            StockTransactionService stockService,
            ItemService itemService) {
        this.stockService = stockService;
        this.itemService = itemService;
    }

    @GetMapping
    public String form(Model model) {
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Inventory", "/inventory"),
            new Breadcrumb("Adjust Stock",null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("items", itemService.findActiveItems());
        return "stock/adjustment-form::content";
    }

    @PostMapping
    public String adjust(@RequestParam Long itemId,
                         @RequestParam BigDecimal quantity,
                         @RequestParam BigDecimal unitCost,
                         @RequestParam String type,
                         @RequestParam(required = false) String reason,
                         Model model,
                         RedirectAttributes redirectAttributes,
                         HttpServletResponse response) {

        boolean increase = type.equals("IN");

        stockService.adjustStock(itemId, quantity, unitCost, increase, reason);
        // response.setHeader("HX-Trigger", ToastHxTrigger.create("Stock adjusted successfully", "success"));
        // return "stock/adjustment-form::content";

        redirectAttributes.addFlashAttribute("message", "Stock adjusted successfully");
        return "redirect:/stock-adjustments";
    }
}

record Message(String id, String title, String content) {
}