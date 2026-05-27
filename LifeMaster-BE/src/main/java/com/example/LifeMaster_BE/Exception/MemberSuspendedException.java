package com.example.LifeMaster_BE.Exception;

import java.time.LocalDateTime;

/**
 * 회원 정지 상태 예외
 */
public class MemberSuspendedException extends RuntimeException {

    private final LocalDateTime suspendedUntil;

    public MemberSuspendedException(LocalDateTime suspendedUntil) {
        super("계정이 " + suspendedUntil + "까지 정지되었습니다.");
        this.suspendedUntil = suspendedUntil;
    }

    public LocalDateTime getSuspendedUntil() {
        return suspendedUntil;
    }
}
