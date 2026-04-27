package com.example.LifeMaster_BE.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//어드민용 프론트 페이지 컨트롤러
@Controller
public class AdminPageController {

    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "admin/login";
    }

    @GetMapping("/admin/coupons-page")
    public String couponPage() {
        return "adminPage";
    }
}
