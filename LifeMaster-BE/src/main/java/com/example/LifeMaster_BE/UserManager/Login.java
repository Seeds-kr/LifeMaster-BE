package com.example.LifeMaster_BE.UserManager;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

@Service
public class Login {
    /** 로그인 체크 공통 로직 */
    public ResponseEntity<?> checkLogin(@AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }
        return null;
    }
}
