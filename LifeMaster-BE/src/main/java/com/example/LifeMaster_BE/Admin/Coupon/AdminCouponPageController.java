package com.example.LifeMaster_BE.Admin.Coupon;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminCouponPageController {

    @GetMapping("/admin/coupons-page")
    public String couponPage() {
        return "admin/coupons";
    }
}