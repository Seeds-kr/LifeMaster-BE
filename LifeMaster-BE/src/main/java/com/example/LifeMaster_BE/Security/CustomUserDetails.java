package com.example.LifeMaster_BE.Security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {

    private final Long id;
    private final String nickname;

    public CustomUserDetails(Long id, String email, String password, String nickname,
                             Collection<? extends GrantedAuthority> authorities){
        super(email, password, authorities);
        this.id = id;
        this.nickname = nickname;
    }
}
