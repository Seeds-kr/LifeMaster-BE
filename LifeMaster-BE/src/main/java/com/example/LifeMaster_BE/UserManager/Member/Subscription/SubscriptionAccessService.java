package com.example.LifeMaster_BE.UserManager.Member.Subscription;

import com.example.LifeMaster_BE.Exception.CustomException.ForbiddenActionException;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionAccessService {

    public void validateFeatureAccess(MemberEntity member, FeatureType featureType) {
        if (member == null) {
            throw new ForbiddenActionException("회원 정보를 확인할 수 없습니다.");
        }

        switch (featureType) {
            case GROUP,
                    MULTI_CHALLENGE,
                    FULL_RECORD_VIEW,
                    ADVANCED_STATISTICS,
                    SLEEP_PATTERN,
                    CUSTOM_SCREEN,
                    Detox-> validatePremiumAccess(member);

            default -> {
                // FREE도 허용되는 기능이면 통과
            }
        }
    }

    public void validatePremiumAccess(MemberEntity member) {
        if (!member.hasActivePremiumAccess()) {
            throw new ForbiddenActionException("프리미엄 전용 기능입니다.");
        }
    }
}