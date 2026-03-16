package com.example.salesmanager4.retailers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/retailers")
public class RetailerController {

    List<RetailerDto> retailers = List.of(
            new RetailerDto("R001", "Retailer A"),
            new RetailerDto("R002", "Retailer B"),
            new RetailerDto("R003", "Retailer C")
        );

    @GetMapping("/list")
    public String dropdownList(@RequestParam(name="q") String q, Model model) {
        
        model.addAttribute("retailers", retailers.stream().filter(r -> r.name().toUpperCase().contains(q.toUpperCase())).toList());
        return "fragments/retailer_dropdown_list";
    }

}

record RetailerDto(
    String id,
    String name) {
}
