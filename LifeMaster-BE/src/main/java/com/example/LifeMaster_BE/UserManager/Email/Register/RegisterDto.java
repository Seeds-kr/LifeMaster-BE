package com.example.LifeMaster_BE.UserManager.Email.Register;

import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor // 파라미터 없는 디폴트 생성자 추가
public class RegisterDto {

    private String email;
    private String password;
    private String passwordConfirm;
}