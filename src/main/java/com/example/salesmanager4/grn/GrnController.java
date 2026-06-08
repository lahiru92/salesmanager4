package com.example.salesmanager4.grn;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.salesmanager4.util.Breadcrumb;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@Controller
@RequestMapping("/grn")
public class GrnController {


    @GetMapping("/create")
    public String createForm(Model model) {
        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("GRNs", "/grns"),
            new Breadcrumb("New GRN", null)
        );


        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("grn", new GrnRequestDto());
        model.addAttribute("mode", "create");

        
        return "grn/form::content";
    }

    @PostMapping
    public String create(@Valid @RequestBody GrnRequestDto grnRequest, BindingResult bindingResult, Model model) {
        //TODO: process POST request
        log.info("Received GRN Header: {}", grnRequest);

         if (bindingResult.hasErrors()) {
            log.error("Validation errors: {}", bindingResult.getAllErrors());
            return "Validation failed: " + bindingResult.getAllErrors().toString();
        }

        return "grn/form::content";
    }
    
}
