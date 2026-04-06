package com.example.LifeMaster_BE.UserManager.Coupon;

import lombok.Getter;
import lombok.Setter;

public class CouponDto {

    @Getter
    public static class Register {
        private String couponCode;
    }

    @Getter
    @Setter
    public static class Use {
        private String couponCode;
    }

    @Getter
    @Setter
    public static class Create {
        private Integer couponPercent;
        private CouponType couponType;
    }
}