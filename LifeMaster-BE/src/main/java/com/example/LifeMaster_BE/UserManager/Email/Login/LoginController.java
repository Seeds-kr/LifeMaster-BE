package com.example.LifeMaster_BE.UserManager.Email.Login;

import com.example.LifeMaster_BE.Security.Utils.JwtUtil;
import com.example.LifeMaster_BE.UserManager.Peristalsis.OAuthUsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user/login")
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final OAuthUsersRepository OAuthUsersRepository;

    @PostMapping
    public ResponseEntity<String> login(@RequestBody LoginDto loginDto){
        String email = loginDto.getEmail();
        String password = loginDto.getPassword();

        //연동 로그인 이메일 사용 방지
        if (OAuthUsersRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("이미 해당 계정은 연동 계정입니다. 연동 로그인을 이용하세요.");
        }

        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        String token = jwtUtil.generateToken(authenticate.getName());
        return ResponseEntity.ok(token);
    }

}
