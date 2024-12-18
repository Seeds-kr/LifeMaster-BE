package com.example.LifeMaster_BE.UserManager.Member;

import jakarta.persistence.*;

@Entity
public class MemberEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String email;
    private String password;
    private String nickname;
    private String picture;

    // enum 타입
    @Enumerated(EnumType.STRING)
    private LoginType loginType;
    @Enumerated(EnumType.STRING)
    private LoginRole loginRole;

    private boolean loginStatus;
}
