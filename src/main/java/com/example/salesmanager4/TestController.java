package com.example.salesmanager4;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
public class TestController {

    public String toastTest() {
        return "test/toast-test";
    }

}
