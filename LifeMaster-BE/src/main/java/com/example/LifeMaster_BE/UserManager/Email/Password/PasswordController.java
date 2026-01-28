package com.example.LifeMaster_BE.UserManager.Email.Password;

import com.example.LifeMaster_BE.UserManager.Email.Password.Dto.ConfirmEmailRequest;
import com.example.LifeMaster_BE.UserManager.Email.Password.Dto.PasswordResetDto;
import com.example.LifeMaster_BE.UserManager.Email.Password.Dto.PasswordResponseDto;
import com.example.LifeMaster_BE.UserManager.Email.Password.Dto.TokenVerifyResponse;
import com.example.LifeMaster_BE.UserManager.Email.Password.Dto.VerifyCodeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/password/reset")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/confirm-email")
    public ResponseEntity<PasswordResponseDto> sendEmail(@RequestBody ConfirmEmailRequest request) {
        passwordService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok(new PasswordResponseDto(true, "인증 코드가 이메일로 발송되었습니다."));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<TokenVerifyResponse> verifyCode(@RequestBody VerifyCodeRequest request) {
        String token = passwordService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(new TokenVerifyResponse(true, "인증이 완료되었습니다.", token));
    }

    @PostMapping
    public ResponseEntity<PasswordResponseDto> resetPassword(@RequestBody PasswordResetDto passwordResetDto) {
        String token = passwordResetDto.getToken();
        String newPassword = passwordResetDto.getNewPassword();
        String checkPassword = passwordResetDto.getCheckPassword();

        passwordService.resetPassword(token, newPassword, checkPassword);
        return ResponseEntity.ok(new PasswordResponseDto(true, "비밀번호가 성공적으로 재설정되었습니다."));
    }
}
