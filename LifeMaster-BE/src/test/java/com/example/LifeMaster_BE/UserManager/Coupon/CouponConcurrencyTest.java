package com.example.LifeMaster_BE.UserManager.Coupon;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.Payment.PaymentStatus;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionPlan;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CouponConcurrencyTest {

    private static final Logger log = LoggerFactory.getLogger(CouponConcurrencyTest.class);

    @Autowired
    private CouponFacade couponFacade;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DataSource dataSource;

    private List<MemberEntity> testUsers = new ArrayList<>();
    private Coupon testCoupon;

    private HikariPoolMXBean getPoolMXBean() {
        return ((HikariDataSource) dataSource).getHikariPoolMXBean();
    }

    @BeforeEach
    void setUp() {
        // 테스트 유저 100명 생성
        for (int i = 0; i < 100; i++) {
            MemberEntity member = MemberEntity.builder()
                    .email("testuser" + i + "@test.com")
                    .password("password")
                    .nickname("testuser" + i)
                    .subscriptionPlan(SubscriptionPlan.FREE)
                    .paymentStatus(PaymentStatus.UNPAID)
                    .build();
            testUsers.add(memberRepository.save(member));
        }

        // 쿠폰 1개 생성 (UNUSE 상태)
        testCoupon = Coupon.builder()
                .couponCode("TEST-COUPON-001")
                .couponPercent(100)
                .couponStatus(CouponStatus.UNUSE)
                .build();
        testCoupon = couponRepository.save(testCoupon);
    }

    @AfterEach
    void tearDown() {
        couponRepository.deleteAll();
        memberRepository.deleteAll();
        testUsers.clear();
    }

    @Test
    @Disabled("Stage 1: Pessimistic Lock 적용 전 정합성 문제 증명용 (락 적용 후 더 이상 재현 불가)")
    @DisplayName("[락 없음] 동일 쿠폰에 100명이 동시 등록 시 1명만 성공해야 하지만, 락이 없으면 여러 명이 등록될 수 있다")
    void 락_없이_동일_쿠폰_동시_등록() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> successUsers = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    couponFacade.registerCoupon(testUsers.get(index).getId(), "TEST-COUPON-001");
                    successCount.incrementAndGet();
                    successUsers.add("testuser" + index);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        executorService.shutdown();

        log.warn("=== [락 없음] 동시성 테스트 결과 ===");
        log.warn("성공 횟수: " + successCount.get());
        log.warn("실패 횟수: " + failCount.get());
        log.warn("성공 유저: " + successUsers);

        Coupon result = couponRepository.findByCouponCode("TEST-COUPON-001").orElseThrow();
        log.warn("쿠폰 상태: " + result.getCouponStatus());
        log.warn("쿠폰 소유자 ID: " + (result.getUser() != null ? result.getUser().getId() : "null"));

        log.warn("[검증] 정합성 깨짐 여부: " + (successCount.get() > 1 ? "YES - " + successCount.get() + "명이 성공" : "NO"));

        assertThat(successCount.get())
                .as("락이 없으면 동시 등록 시 1명 이상이 성공 응답을 받는다 (정합성 문제 증명)")
                .isGreaterThan(1);
    }

    @Test
    @Disabled("Stage 2: Pessimistic Lock 테스트 (Redisson 분산 락으로 대체됨)")
    @DisplayName("[Pessimistic Lock] 동일 쿠폰에 100명이 동시 등록 시 정확히 1명만 성공해야 한다")
    void Pessimistic_Lock_동일_쿠폰_동시_등록() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger connectionTimeoutCount = new AtomicInteger(0);
        List<String> successUsers = Collections.synchronizedList(new ArrayList<>());

        HikariPoolMXBean poolMXBean = getPoolMXBean();
        AtomicInteger peakActiveConnections = new AtomicInteger(0);
        AtomicInteger peakPendingThreads = new AtomicInteger(0);

        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
        monitor.scheduleAtFixedRate(() -> {
            int active = poolMXBean.getActiveConnections();
            int pending = poolMXBean.getThreadsAwaitingConnection();
            peakActiveConnections.updateAndGet(current -> Math.max(current, active));
            peakPendingThreads.updateAndGet(current -> Math.max(current, pending));
        }, 0, 10, TimeUnit.MILLISECONDS);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    couponFacade.registerCoupon(testUsers.get(index).getId(), "TEST-COUPON-001");
                    successCount.incrementAndGet();
                    successUsers.add("testuser" + index);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    if (e.getMessage() != null && e.getMessage().contains("Connection is not available")) {
                        connectionTimeoutCount.incrementAndGet();
                    }
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        long startTime = System.nanoTime();
        startLatch.countDown();
        doneLatch.await();
        long totalTimeMs = (System.nanoTime() - startTime) / 1_000_000;

        monitor.shutdown();
        executorService.shutdown();

        log.warn("=== [Pessimistic Lock] 동시성 테스트 결과 ===");
        log.warn("성공 횟수: {}", successCount.get());
        log.warn("실패 횟수: {}", failCount.get());
        log.warn("성공 유저: {}", successUsers);
        log.warn("=== 성능 지표 ===");
        log.warn("전체 소요 시간: {}ms", totalTimeMs);
        log.warn("최대 동시 Active 커넥션: {}", peakActiveConnections.get());
        log.warn("최대 커넥션 대기 스레드: {}", peakPendingThreads.get());
        log.warn("커넥션 타임아웃 발생: {}건", connectionTimeoutCount.get());
        log.warn("HikariCP 풀 크기: {}", ((HikariDataSource) dataSource).getMaximumPoolSize());

        Coupon result = couponRepository.findByCouponCode("TEST-COUPON-001").orElseThrow();
        log.warn("쿠폰 상태: {}", result.getCouponStatus());
        log.warn("쿠폰 소유자 ID: {}", result.getUser() != null ? result.getUser().getId() : "null");

        assertThat(successCount.get())
                .as("Pessimistic Lock 적용 시 동시 등록에서 정확히 1명만 성공해야 한다")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("[Redisson 분산 락] 동일 쿠폰에 100명이 동시 등록 시 정확히 1명만 성공해야 한다")
    void Redisson_분산_락_동일_쿠폰_동시_등록() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger lockFailCount = new AtomicInteger(0);
        AtomicInteger connectionTimeoutCount = new AtomicInteger(0);
        List<String> successUsers = Collections.synchronizedList(new ArrayList<>());

        // HikariCP 커넥션 풀 모니터링 (10ms 간격으로 샘플링)
        HikariPoolMXBean poolMXBean = getPoolMXBean();
        AtomicInteger peakActiveConnections = new AtomicInteger(0);
        AtomicInteger peakPendingThreads = new AtomicInteger(0);

        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
        monitor.scheduleAtFixedRate(() -> {
            int active = poolMXBean.getActiveConnections();
            int pending = poolMXBean.getThreadsAwaitingConnection();
            peakActiveConnections.updateAndGet(current -> Math.max(current, active));
            peakPendingThreads.updateAndGet(current -> Math.max(current, pending));
        }, 0, 10, TimeUnit.MILLISECONDS);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    couponFacade.registerCoupon(testUsers.get(index).getId(), "TEST-COUPON-001");
                    successCount.incrementAndGet();
                    successUsers.add("testuser" + index);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    if (e.getMessage() != null && e.getMessage().contains("락 획득 실패")) {
                        lockFailCount.incrementAndGet();
                    }
                    if (e.getMessage() != null && e.getMessage().contains("Connection is not available")) {
                        connectionTimeoutCount.incrementAndGet();
                    }
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        long startTime = System.nanoTime();
        startLatch.countDown();
        doneLatch.await();
        long totalTimeMs = (System.nanoTime() - startTime) / 1_000_000;

        monitor.shutdown();
        executorService.shutdown();

        // 결과 출력
        log.warn("=== [Redisson 분산 락] 동시성 테스트 결과 ===");
        log.warn("성공 횟수: {}", successCount.get());
        log.warn("실패 횟수: {}", failCount.get());
        log.warn("락 획득 실패: {}건", lockFailCount.get());
        log.warn("성공 유저: {}", successUsers);
        log.warn("=== 성능 지표 ===");
        log.warn("전체 소요 시간: {}ms", totalTimeMs);
        log.warn("최대 동시 Active 커넥션: {}", peakActiveConnections.get());
        log.warn("최대 커넥션 대기 스레드: {}", peakPendingThreads.get());
        log.warn("커넥션 타임아웃 발생: {}건", connectionTimeoutCount.get());
        log.warn("HikariCP 풀 크기: {}", ((HikariDataSource) dataSource).getMaximumPoolSize());

        // 실제 DB 상태 확인
        Coupon result = couponRepository.findByCouponCode("TEST-COUPON-001").orElseThrow();
        log.warn("쿠폰 상태: {}", result.getCouponStatus());
        log.warn("쿠폰 소유자 ID: {}", result.getUser() != null ? result.getUser().getId() : "null");

        // Redisson 분산 락 적용 후 정확히 1명만 성공해야 한다
        assertThat(successCount.get())
                .as("Redisson 분산 락 적용 시 동시 등록에서 정확히 1명만 성공해야 한다")
                .isEqualTo(1);
    }
}
