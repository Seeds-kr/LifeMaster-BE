package com.example.LifeMaster_BE.UserManager;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(nullable = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(nullable = true)
    private String nickname;

    @Column(columnDefinition = "json", nullable = true)
    private String picture;

    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Enumerated(EnumType.STRING)
    private LoginRole loginRole;

    @Enumerated(EnumType.STRING)
    private LoginStatus loginStatus;

    public enum LoginType {
        DEFAULT, SOCIAL
    }

    public enum LoginRole {
        USER, ADMIN
    }

    public enum LoginStatus {
        ACTIVE, NONACTIVE
    }
}
