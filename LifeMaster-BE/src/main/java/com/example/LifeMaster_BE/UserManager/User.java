package com.example.LifeMaster_BE.UserManager;

import com.example.LifeMaster_BE.UserManager.Member.LoginRole;
import com.example.LifeMaster_BE.UserManager.Member.LoginType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column
    private String email;

    @Column
    private String password;

    @Column
    private String nickName;

    @Column
    private String profileUrl;

    @Column
    private LoginType loginType;

    @Column
    private LoginRole loginRole;

    @Column
    private String loginStatus;

    @Column
    private String phoneNumber;
}
