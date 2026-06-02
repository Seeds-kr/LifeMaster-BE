package com.example.LifeMaster_BE.Config.Lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 분산 락 애노테이션
 * AOP를 통해 메서드 실행 전후로 락을 획득/해제합니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 락 키 (SpEL 표현식 지원)
     * 예: "#couponCode", "'user:' + #userId", "#lightningId"
     */
    String key();

    /**
     * 락 획득 대기 시간 (초 단위)
     * 기본값: 5초
     */
    long waitTime() default 5L;

    /**
     * 락 자동 해제 시간 (초 단위)
     * -1이면 명시적으로 unlock할 때까지 유지
     * 기본값: 3초
     */
    long leaseTime() default 3L;
}
