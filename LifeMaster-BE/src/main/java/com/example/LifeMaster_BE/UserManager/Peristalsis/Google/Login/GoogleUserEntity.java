package com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login;
/*
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class GoogleUserEntity implements OAuth2User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 데이터베이스에서 사용하는 기본 키

    private String googleId;   // 구글 인증 ID
    private String email;      // 이메일
    private String name;       // 이름
    private String pictureUrl; // 프로필 사진 URL

    public GoogleUserEntity(String googleId, String email, String name, String pictureUrl) {
        this.googleId = googleId;
        this.email = email;
        this.name = name;
        this.pictureUrl = pictureUrl;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of(
                "googleId", googleId,
                "email", email,
                "name", name,
                "picture", pictureUrl
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }
}

 */
