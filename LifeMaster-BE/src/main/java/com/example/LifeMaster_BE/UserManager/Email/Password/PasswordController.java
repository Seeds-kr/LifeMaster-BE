package com.example.LifeMaster_BE.UserManager.Email.Password;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/send-email")
    public ResponseEntity<PasswordResponseDto> sendEmail(@RequestBody String email){
        passwordService.sendPasswordResetEmail(email);
        return ResponseEntity.ok(new PasswordResponseDto(true, "Password reset link has been sent to your email."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResponseDto> resetPassword(@RequestBody PasswordResetDto passwordResetDto){
        String token = passwordResetDto.getToken();
        String newPassword = passwordResetDto.getNewPassword();
        String checkPassword = passwordResetDto.getCheckPassword();

        passwordService.resetPassword(token, newPassword, checkPassword);
        return ResponseEntity.ok(new PasswordResponseDto(true, "password reset successful."));
    }
}
