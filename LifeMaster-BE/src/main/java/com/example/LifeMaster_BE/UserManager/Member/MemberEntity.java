package com.example.LifeMaster_BE.UserManager.Member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "member_entity")  // 테이블 이름을 단순화하여 충돌 방지
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL 호환 전략
    private Long id;

    @Column(nullable = false, unique = true) // 이메일 중복 방지
    private String email;

    @Column(nullable = false)
    private String password; // 연동 로그인 유저는 고유 식별 ID

    private String nickname;

    @Column(name = "image_url") // 컬럼 이름 명시
    private String imageUrl;

    //@Enumerated(EnumType.STRING)
    @Column(name = "login_type") // 컬럼 이름 명시
    private LoginType loginType;

    //@Enumerated(EnumType.STRING)
    @Column(name = "login_role") // 컬럼 이름 명시
    private LoginRole loginRole;

    @Column(name = "login_status") // 컬럼 이름 명시
    private boolean loginStatus;

    public MemberEntity() {
    }

    public MemberEntity(String email, String password) {
        this.email = email;
        this.password = password;
        this.imageUrl = "url";
        this.loginStatus = true;
        this.nickname = "nick";
    }

    public String getName() {
        return this.nickname;
    }

    public String getPicture() {
        return this.imageUrl;
    }

    public MemberEntity update(String name, String picture) {
        this.nickname = name;
        this.imageUrl = picture;
        return this;
    }

    public MemberEntity login(boolean bool){
        this.loginStatus = bool;
        return this;
    }
}

