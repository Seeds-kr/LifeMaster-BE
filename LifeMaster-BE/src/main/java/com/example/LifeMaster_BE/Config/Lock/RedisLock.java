package com.example.LifeMaster_BE.Config.Lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 분산 락 구현체 (Redisson 사용)
 * 프로덕션 환경에서 멀티 서버 동시성 제어
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "lock.strategy", havingValue = "redis", matchIfMissing = true)
public class RedisLock implements LockStrategy {

    private final RedissonClient redissonClient;
    private static final String LOCK_PREFIX = "lock:";

    @Override
    public boolean lock(String key, long waitTime, long leaseTime) throws InterruptedException {
        String lockKey = LOCK_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);

        if (acquired) {
            log.debug("락 획득 성공: {}", lockKey);
        } else {
            log.warn("락 획득 실패: {} (대기 시간 {}초 초과)", lockKey, waitTime);
        }

        return acquired;
    }

    @Override
    public void unlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);

        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("락 해제: {}", lockKey);
        }
    }
}
