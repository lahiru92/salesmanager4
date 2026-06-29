package com.example.salesmanager4.grn;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.salesmanager4.suppliers.SupplierService;
import com.example.salesmanager4.util.Breadcrumb;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/grn")
public class GrnController {

    private final GrnService grnService;
    private final SupplierService supplierService;

    @GetMapping
    public String list(GrnListRequestDto requestDto,
        @PageableDefault(page=0, size=10, sort="id", direction = Sort.Direction.DESC) Pageable pageable, 
        Model model,
        HttpServletRequest req) {

        Page<GrnListResponseDto> grns = grnService.listGrns(requestDto, pageable);

        if ("grn-table".equals(req.getHeader("Hx-Target"))) {
            model.addAttribute("grns", grns);
            return "grn/list::grn-table";
        }

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("GRNs", null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("grns", grns);
        return "grn/list::content";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {

        GrnRequestDto grn = grnService.findRequestDtoById(id);
        System.out.println(grn);

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("GRNs", "/grn"),
            new Breadcrumb("GRN #" + grn.getId(), null)
        );


        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("grn", grn);
        model.addAttribute("mode", "view");

        
        return "grn/form::content";
    }

    @GetMapping("/{id}/edit")
    public String editView(@PathVariable Long id, Model model) {

        GrnRequestDto grn = grnService.findRequestDtoById(id);
        System.out.println(grn);

        String mode;
        if ("DRAFT".equals(grn.getStatus())) {
            mode = "edit";
        } else {
            mode = "view";
        }

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("GRNs", "/grn"),
            new Breadcrumb("Edit GRN", null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("grn", grn);
        model.addAttribute("mode", mode);

        return "grn/form::content";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("GRNs", "/grn"),
            new Breadcrumb("New GRN", null)
        );


        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("grn", new GrnRequestDto());
        model.addAttribute("mode", "create");

        
        return "grn/form::content";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("grn") GrnRequestDto grnRequest, BindingResult bindingResult, Model model) {
        log.info("Received GRN Header: {}", grnRequest);

        if (!grnRequest.isBalanced()) {
            bindingResult.reject("payments.mismatch","Paid amounts not balanced with the total.");
        }

        if (bindingResult.hasErrors()) {
            log.error("Validation errors: {}", bindingResult.getAllErrors());
            // return "grn/form::content";
            
            List<Breadcrumb> breadcrumbs = List.of(
                new Breadcrumb("Home", "/"),
                new Breadcrumb("GRNs", "/grns"),
                new Breadcrumb("New GRN", null)
            );

            model.addAttribute("breadcrumbs", breadcrumbs);
            model.addAttribute("mode", "create");

            if (grnRequest.getSupplierId() != null) {
                model.addAttribute("supplierName", supplierService.findById(grnRequest.getSupplierId()).getName());
            }

            return "grn/form::content"; 

        }

        grnService.createGrn(grnRequest);

        return "redirect:/grn/create";
    }


    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("grn") GrnRequestDto grnRequest, BindingResult bindingResult, Model model) {
        
        log.info("Update request for grn {}", id);

        boolean error = false;

        if (!id.equals(grnRequest.getId())) {
            log.error("Path variable ID {} does not match GRN request ID {}", id, grnRequest.getId());
            error = true;
        }

        if (bindingResult.hasErrors()) {
            log.error("Validation errors: {}", bindingResult.getAllErrors());
            error = true;
        }

        
        Grn existingGrn = grnService.findById(id);
        if (existingGrn == null) {
            log.error("GRN not found with id: {}", id);
            error = true;
        }

        if (existingGrn != null && !existingGrn.getStatus().equals("DRAFT")) {
            log.error("Cannot edit GRN with status: {}", existingGrn.getStatus());
            error = true;
        }

        if (!grnRequest.isBalanced()) {
            bindingResult.reject("payments.mismatch", "Paid amounts not balanced with the total.");
            bindingResult.reject("test1", "Test error 1");
            bindingResult.reject("test2", "Test error 2");
            bindingResult.reject("test3", "Test error 3");
            error = true;
        }

        if (error) {
            log.error("Validation errors: {}", bindingResult.getAllErrors());
            // return "grn/form::content";
            
            List<Breadcrumb> breadcrumbs = List.of(
                new Breadcrumb("Home", "/"),
                new Breadcrumb("GRNs", "/grns"),
                new Breadcrumb("New GRN", null)
            );

            model.addAttribute("breadcrumbs", breadcrumbs);
            model.addAttribute("mode", "edit");
            model.addAttribute("message", "Validation errors occurred. Please correct the errors and try again.");

            if (grnRequest.getSupplierId() != null) {
                model.addAttribute("supplierName", supplierService.findById(grnRequest.getSupplierId()).getName());
            }

            return "grn/form::content"; 

        }

        log.info("\n\nGRN REQ FOR UPDATE****\n{}\n", grnRequest);
        grnService.createGrn(grnRequest);

        return "redirect:/grn";
    }
    

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, HttpServletRequest req, HttpServletResponse res, Model model) {
        try {
            grnService.approveGrn(id);

            if ("inline".equals(req.getHeader("X-Approvemode"))) {
                model.addAttribute("grnId", id);
                model.addAttribute("oobTarget", req.getHeader("X-oob-target"));
                return "/grn/inline-approve-response";
            }

            return "/grn/approve-response::approved-response";
        } catch (RuntimeException e) {
            log.error("Error approving GRN: {}", e);

            if ("inline".equals(req.getHeader("X-Approvemode"))) {
                model.addAttribute("toast","Approval failed");
                res.setHeader("HX-Retarget", "#toast");
                return "/grn/approve-response::inline-approve-error";
            }

            return "/grn/approve-response::approve-error";
        }

    }
}
