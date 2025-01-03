package com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Entity
@Table(name = "google_users_entity")  // 테이블 이름 설정
public class GoogleUsersEntity implements OAuth2User {

    @Id  // 식별자 필드 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID 자동 생성 전략
    private Long id;  // id 필드 타입을 Long으로 설정
    private String name;
    private String email;
    private String picture;
    private String role;  // role 필드 추가

    private String identifier; // 구글/네이버 개인 식별용 ID

    // 기본 생성자
    public GoogleUsersEntity() {
    }

    // 생성자에서 role을 포함시킴
    public GoogleUsersEntity(String name, String email, String picture, String role, String identifier) {
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

    // Getter와 Setter 추가
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPicture() {
        return picture;
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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    // update 메서드에서 role 추가
    public GoogleUsersEntity update(String name, String picture) {
        this.name = name;
        this.picture = picture;
        return this;
    }

    // toEntity 메서드에서 role을 포함하여 엔티티를 반환
    public GoogleUsersEntity toEntity() {
        return new GoogleUsersEntity(this.name, this.email, this.picture, "User", this.identifier);
    }
}
