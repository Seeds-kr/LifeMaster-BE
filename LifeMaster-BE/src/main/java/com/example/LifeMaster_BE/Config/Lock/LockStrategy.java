package com.example.LifeMaster_BE.Config.Lock;

/**
 * 락 전략 인터페이스
 * 환경에 따라 구현체를 교체할 수 있습니다.
 * - LocalLock: 개발/테스트 환경 (ReentrantLock)
 * - RedisLock: 프로덕션 환경 (Redisson)
 */
public interface LockStrategy {

    /**
     * 락 획득
     *
     * @param key       락 키
     * @param waitTime  락 획득 대기 시간 (초)
     * @param leaseTime 락 자동 해제 시간 (초), -1이면 명시적 해제까지 유지
     * @return 락 획득 성공 여부
     * @throws InterruptedException 락 대기 중 인터럽트 발생
     */
    boolean lock(String key, long waitTime, long leaseTime) throws InterruptedException;

    /**
     * 락 해제
     *
     * @param key 락 키
     */
    void unlock(String key);
}
