package com.example.LifeMaster_BE.UserManager.Member;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class MemberEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;

    private String nickname;
    private String imageUrl;

    // enum 타입
    @Enumerated(EnumType.STRING)
    private LoginType loginType;
    @Enumerated(EnumType.STRING)
    private LoginRole loginRole;

    private boolean loginStatus;

    public MemberEntity(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
