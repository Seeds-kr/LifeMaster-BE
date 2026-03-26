package com.example.LifeMaster_BE.UserManager.Coupon;

import lombok.Getter;

public class CouponDto {

    @Getter
    public static class Register {
        private String couponCode;
    }

    @Getter
    public static class Use {
        private Long couponId;
    }

    @Getter
    public static class Create {
        private Integer couponPercent;
    }
}