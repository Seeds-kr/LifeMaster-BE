package com.example.LifeMaster_BE.UserManager.Email.Login;

import com.example.LifeMaster_BE.Security.Utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user/login")
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping
    public String login(@RequestBody LoginDto loginDto){
        String email = loginDto.getEmail();
        String password = loginDto.getPassword();

        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        return jwtUtil.generateToken(authenticate.getName());
    }

}
