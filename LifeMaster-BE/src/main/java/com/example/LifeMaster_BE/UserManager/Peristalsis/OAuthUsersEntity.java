package com.example.LifeMaster_BE.UserManager.Peristalsis;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Entity
@Table(name = "oauth_users_entity")  // 테이블 이름 설정
public class OAuthUsersEntity implements OAuth2User {

    // Getter와 Setter 추가
    @Setter
    @Getter
    @Id  // 식별자 필드 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID 자동 생성 전략
    private Long id;  // id 필드 타입을 Long으로 설정
    private String name;
    @Setter
    @Getter
    private String email;
    @Getter
    private String picture;
    private String role;  // role 필드 추가

    @Getter
    private String identifier; // 구글/네이버 개인 식별용 ID

    // 기본 생성자
    public OAuthUsersEntity() {
    }

    // 생성자에서 role을 포함시킴
    public OAuthUsersEntity(String name, String email, String picture, String role, String identifier) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = role;  // 역할 설정
        this.identifier = identifier;  // 개인 식별자 설정
    }

    @Override
    public Map<String, Object> getAttributes() {
        // 사용자 속성 반환
        return Map.of(
                "id", this.id,
                "name", this.name,
                "email", this.email,
                "picture", this.picture,  // picture 포함
                "role", this.role  // role 추가
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자 역할을 포함하여 권한을 반환
        return null;
    }

    @Override
    public String getName() {
        return this.name;  // 사용자 이름 반환
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    // update 메서드에서 role 추가
    public OAuthUsersEntity update(String name, String picture) {
        this.name = name;
        this.picture = picture;
        return this;
    }

    // toEntity 메서드에서 role을 포함하여 엔티티를 반환
    public OAuthUsersEntity toEntity() {
        return new OAuthUsersEntity(this.name, this.email, this.picture, "User", this.identifier);
    }
}
