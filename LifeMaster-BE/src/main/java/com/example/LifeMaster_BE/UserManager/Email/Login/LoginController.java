package com.example.LifeMaster_BE.UserManager.Email.Login;

import com.example.LifeMaster_BE.Security.Utils.JwtUtil;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;

@Slf4j
@RestController
@RequestMapping("/user/login")
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @PostMapping
    public ResponseEntity<String> login(@RequestBody LoginDto loginDto){
        String email = loginDto.getEmail();
        String password = loginDto.getPassword();

        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not fount" + email));
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        String token = jwtUtil.generateToken(authenticate.getName());
        return ResponseEntity.ok(token);
    }

}
