package com.example.LifeMaster_BE.Admin.Coupon;

import com.example.LifeMaster_BE.UserManager.Coupon.Coupon;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminCouponStatusResponse {

    private Long couponId;
    private String couponCode;
    private Integer couponPercent;
    private String couponType;
    private String couponStatus;

    // 등록/사용된 경우 어떤 유저에게 연결되어 있는지
    private Long memberId;
    private String memberEmail;
    private String memberNickname;

    public static AdminCouponStatusResponse from(Coupon coupon) {
        return AdminCouponStatusResponse.builder()
                .couponId(coupon.getCouponId())
                .couponCode(coupon.getCouponCode())
                .couponPercent(coupon.getCouponPercent())
                .couponType(coupon.getCouponType() != null ? coupon.getCouponType().name() : null)
                .couponStatus(coupon.getCouponStatus() != null ? coupon.getCouponStatus().name() : null)
                .memberId(coupon.getUser() != null ? coupon.getUser().getId() : null)
                .memberEmail(coupon.getUser() != null ? coupon.getUser().getEmail() : null)
                .memberNickname(coupon.getUser() != null ? coupon.getUser().getNickname() : null)
                .build();
    }
}
