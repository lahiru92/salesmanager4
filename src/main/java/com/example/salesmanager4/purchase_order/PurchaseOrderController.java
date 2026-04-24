package com.example.salesmanager4.purchase_order;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.salesmanager4.purchase_order.dto.Po;
import com.example.salesmanager4.purchase_order.dto.PoLine;
import com.example.salesmanager4.purchase_order.dto.PurchaseItemDto;
import com.example.salesmanager4.suppliers.SupplierService;
import com.example.salesmanager4.util.Breadcrumb;

import jakarta.validation.Valid;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService service;
    private final SupplierService supplierService;


    public PurchaseOrderController(PurchaseOrderService service, SupplierService supplierService) {
        this.service = service;
        this.supplierService = supplierService;
    }

    @GetMapping
    public String list(Model model) {

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Purchase Orders", null)
        );
        model.addAttribute("orders", service.findAll());
        model.addAttribute("breadcrumbs", breadcrumbs);
        return "po/list::content";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Purchase Orders", "/purchase-orders"),
            new Breadcrumb("Create Purchase Order", null)
        );

        // List<PoLine> items = List.of(
        //     new PoLine(1,"Butter bonchi", 12, 130.32),
        //     new PoLine(2,"Gova", 23, 132.20),
        //     new PoLine(3, "Rabu", 37, 98.87),
        //     new PoLine(24, "Thumba Karavila", 28, 21.00)
        // );

        // Po po = new Po(4L, "Munchee", null, items);

        model.addAttribute("breadcrumbs", breadcrumbs);
        // model.addAttribute("po", po);
        model.addAttribute("po", new Po(null,null,null,List.of()));
        model.addAttribute("mode", "create");

        
        return "po/form::content";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute Po po,BindingResult bindingResult, Model model,  RedirectAttributes ra) {
        // service.create(po);
        System.out.println(po);

        if (bindingResult.hasErrors() ){
            List<Breadcrumb> breadcrumbs = List.of(
                new Breadcrumb("Home", "/"),
                new Breadcrumb("Purchase Orders", "/purchase-orders"),
                new Breadcrumb("Create Purchase Order", null)
            );
            model.addAttribute("breadcrumbs", breadcrumbs);
            model.addAttribute("mode", "create");
            if (po.supplierId() != null) {
                model.addAttribute("supplierName", supplierService.findById(po.supplierId()).getName());
            }
            return "po/form::content";
        }

        ra.addFlashAttribute("toastMessage", "Purchase order created successfully");
        return "redirect:/purchase-orders";
    }


    
    @PostMapping("/save-json")
    public String saveJString(@RequestParam Long supplierId, @RequestParam String orderDate, @RequestParam String itemsJson) throws Exception {

        List<PurchaseItemDto> items = new ObjectMapper().readValue(itemsJson,
                new TypeReference<List<PurchaseItemDto>>() {
                });

        // validate
        // save
        System.out.println(itemsJson);
        System.out.println(items);
        return "redirect:/purchase-orders";
    }

    @GetMapping("/item-row")
    public String itemRow(Model model, @RequestParam int index) {
        model.addAttribute("index", index+1);
        model.addAttribute("item", new PurchaseOrderItem());
        return "po/item-row::row";
    }
}