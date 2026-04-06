package com.example.LifeMaster_BE.UserManager.Coupon;

import com.example.LifeMaster_BE.ETC.BaseEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    @Column
    private String couponCode;

    @Column
    private Integer couponPercent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType couponType;

    @Enumerated(EnumType.STRING)
    @Column( nullable = true)
    private CouponStatus couponStatus = CouponStatus.UNUSE;//UNUSE,REGISTER,USE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private MemberEntity user;
}
