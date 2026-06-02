package com.example.LifeMaster_BE.Config.Lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 분산 락 AOP
 * @DistributedLock 애노테이션이 붙은 메서드에 대해 락 획득/해제를 자동 처리합니다.
 * SpEL을 사용하여 동적으로 락 키를 생성합니다.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final LockStrategy lockStrategy;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(distributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        // 1. SpEL로 락 키 생성
        String lockKey = generateLockKey(joinPoint, distributedLock);

        // 2. 락 획득 시도
        boolean acquired = false;
        try {
            acquired = lockStrategy.lock(
                    lockKey,
                    distributedLock.waitTime(),
                    distributedLock.leaseTime()
            );

            if (!acquired) {
                throw new IllegalStateException("락 획득 실패: " + lockKey);
            }

            // 3. 비즈니스 로직 실행
            log.debug("비즈니스 로직 실행 시작: {}", lockKey);
            return joinPoint.proceed();

        } finally {
            // 4. 락 해제
            if (acquired) {
                lockStrategy.unlock(lockKey);
                log.debug("비즈니스 로직 실행 완료: {}", lockKey);
            }
        }
    }

    /**
     * SpEL을 사용하여 동적으로 락 키를 생성합니다.
     *
     * @param joinPoint        AOP 조인 포인트
     * @param distributedLock  분산 락 애노테이션
     * @return 생성된 락 키
     */
    private String generateLockKey(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 1. 메서드 파라미터 이름 추출
        String[] parameterNames = signature.getParameterNames();

        // 2. 메서드 파라미터 값 추출
        Object[] args = joinPoint.getArgs();

        // 3. SpEL Context 생성 및 변수 등록
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        // 4. SpEL 표현식 평가
        String keyExpression = distributedLock.key();
        Object value = parser.parseExpression(keyExpression).getValue(context);

        // 5. 최종 락 키 생성: 메서드명:평가된값
        String lockKey = method.getName() + ":" + value;

        log.debug("락 키 생성 - 표현식: {}, 결과: {}", keyExpression, lockKey);

        return lockKey;
    }
}
