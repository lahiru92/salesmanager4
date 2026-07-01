package com.example.salesmanager4.finance.payments.creditors;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.salesmanager4.util.Breadcrumb;

@Controller
@RequestMapping("/creditors")
public class CreditorsContrller {


    @GetMapping
    public String creditors(Model model) {

        List<Breadcrumb> breadcrumbs = List.of(
            new Breadcrumb("Home", "/"),
            new Breadcrumb("Creditors", null)
        );

        model.addAttribute("breadcrumbs", breadcrumbs);

        return "creditors/dashboard::content";
    }
}