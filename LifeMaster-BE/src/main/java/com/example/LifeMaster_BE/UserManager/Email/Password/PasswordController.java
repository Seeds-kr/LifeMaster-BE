package com.example.LifeMaster_BE.UserManager.Email.Password;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/password/reset")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/confirm-email")
    public ResponseEntity<PasswordResponseDto> sendEmail(@RequestBody String email){
        passwordService.sendPasswordResetEmail(email);
        return ResponseEntity.ok(new PasswordResponseDto(true, "Password reset link has been sent to your email."));
    }

    @GetMapping("/verify")
    public ResponseEntity<PasswordResponseDto> verifyToken(@RequestParam String token) {
        passwordService.verifyToken(token);
        return ResponseEntity.ok(new PasswordResponseDto(true, "Token verified."));
    }

    @PostMapping
    public ResponseEntity<PasswordResponseDto> resetPassword(@RequestBody PasswordResetDto passwordResetDto){
        String token = passwordResetDto.getToken();
        String newPassword = passwordResetDto.getNewPassword();
        String checkPassword = passwordResetDto.getCheckPassword();

        passwordService.resetPassword(token, newPassword, checkPassword);
        return ResponseEntity.ok(new PasswordResponseDto(true, "password reset successful."));
    }
}
