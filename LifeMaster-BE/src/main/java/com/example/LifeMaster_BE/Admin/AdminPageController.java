package com.example.LifeMaster_BE.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "admin/login";
    }

    @GetMapping("/admin/admin-page")
    public String couponPage() {
        return "admin/adminPage"; // ← 이렇게 수정
    }
}