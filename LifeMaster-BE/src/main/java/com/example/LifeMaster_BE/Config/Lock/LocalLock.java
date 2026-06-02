package com.example.LifeMaster_BE.Config.Lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 로컬 메모리 기반 락 구현체 (ReentrantLock 사용)
 * 개발/테스트 환경용 (단일 서버)
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "lock.strategy", havingValue = "local")
public class LocalLock implements LockStrategy {

    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Override
    public boolean lock(String key, long waitTime, long leaseTime) throws InterruptedException {
        ReentrantLock lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock());

        boolean acquired = lock.tryLock(waitTime, TimeUnit.SECONDS);

        if (acquired) {
            log.debug("로컬 락 획득 성공: {}", key);
        } else {
            log.warn("로컬 락 획득 실패: {} (대기 시간 {}초 초과)", key, waitTime);
        }

        return acquired;
    }

    @Override
    public void unlock(String key) {
        ReentrantLock lock = lockMap.get(key);

        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("로컬 락 해제: {}", key);

            // 대기 중인 스레드가 없으면 Map에서 제거 (메모리 절약)
            if (!lock.hasQueuedThreads()) {
                lockMap.remove(key);
            }
        }
    }
}
