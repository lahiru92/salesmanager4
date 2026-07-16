package com.example.salesmanager4.purchase_order;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.salesmanager4.purchase_order.dto.Po;
import com.example.salesmanager4.suppliers.SupplierService;
import com.example.salesmanager4.util.Breadcrumb;

import jakarta.validation.Valid;

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

        // TODO Add pagination, sorting, filtering
        model.addAttribute("purchaseOrders", service.listRows());
        model.addAttribute("breadcrumbs", breadcrumbs);

        return "po/list::content";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {

        Po po = service.findById(id).orElseThrow();

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Purchase Orders", "/purchase-orders"),
            new Breadcrumb("PO #" + po.id(), null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("po", po);
        model.addAttribute("mode", "view");

        return "po/form::content";
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
        model.addAttribute("po", new Po());
        model.addAttribute("mode", "create");

        
        return "po/form::content";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {

        Po poEntity = service.findById(id).orElseThrow();

        String mode = "DRAFT".equals(poEntity.status()) ? "edit" : "view";

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Purchase Orders", "/purchase-orders"),
            new Breadcrumb("Edit Purchase Order", null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("po", poEntity);
        model.addAttribute("mode", mode);
        model.addAttribute("supplierName", poEntity.supplierName());


        return "po/form::content";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute Po po,BindingResult bindingResult, Model model,  RedirectAttributes ra) {

        // TODO handle proper error if list is empty
        if (bindingResult.hasErrors() || po.items().isEmpty() || po.items() == null) {
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

        service.create(po);
        System.out.println(po);

        ra.addFlashAttribute("toastMessage", "Purchase order created successfully");
        return "redirect:/purchase-orders";
    }



    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id, RedirectAttributes ra) {
        service.submit(id);
        ra.addFlashAttribute("toastMessage", "Purchase order submitted for approval");
        return "redirect:/purchase-orders";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        service.approve(id);
        ra.addFlashAttribute("toastMessage", "Purchase order approved");
        return "redirect:/purchase-orders";
    }

    @GetMapping("/item-row")
    public String itemRow(Model model, @RequestParam int index) {
        model.addAttribute("index", index+1);
        model.addAttribute("item", new PurchaseOrderItem());
        return "po/item-row::row";
    }
}