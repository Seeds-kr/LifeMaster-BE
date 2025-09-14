package com.example.LifeMaster_BE.UserManager.Email.Register;

import com.example.LifeMaster_BE.UserManager.Email.Register.Dto.Response.CheckNicknameResponseDto;
import com.example.LifeMaster_BE.UserManager.Email.Register.Dto.Response.RegistrationCompleteResponseDto;
import com.example.LifeMaster_BE.UserManager.Email.Register.Dto.Response.RegistrationInitResponseDto;
import com.example.LifeMaster_BE.UserManager.Email.Register.Dto.Request.RegisterDto;
import com.example.LifeMaster_BE.UserManager.Email.Register.Dto.Request.RegisterWithNicknameDto;
import com.example.LifeMaster_BE.UserManager.Peristalsis.OAuthUsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/user/register")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;
    private final OAuthUsersRepository OAuthUsersRepository;

    @PostMapping
    public ResponseEntity<RegistrationInitResponseDto> register(@RequestBody RegisterDto registerDto) {
        String email = registerDto.getEmail();

        // 연동 로그인 이메일 사용 방지
        if (OAuthUsersRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미 해당 계정은 연동 계정입니다. 연동 로그인을 이용하세요."
            );
        }

        RegistrationInitResponseDto response = registerService.registerMember(registerDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nickname")
    public ResponseEntity<CheckNicknameResponseDto> checkNicknameAvailable(
            @RequestParam("nickname") String nickname) {
        boolean isDuplicated = registerService.checkNicknameAvailable(nickname);
        if (!isDuplicated) {
            return ResponseEntity.ok().body(new CheckNicknameResponseDto(true));
        }else{
            return ResponseEntity.badRequest().body(new CheckNicknameResponseDto(false));
        }
    }

    @PostMapping(
            value = "/nickname",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<RegistrationCompleteResponseDto> registerNickname(@ModelAttribute RegisterWithNicknameDto registerWithNicknameDto) {
        String regId = registerWithNicknameDto.getRegId();
        String nickName = registerWithNicknameDto.getNickName();
        MultipartFile image = registerWithNicknameDto.getImage();

        Long memberId = registerService.registerMemberWithNickname(regId, nickName, image);
        RegistrationCompleteResponseDto response = new RegistrationCompleteResponseDto("회원가입이 완료되었습니다!", memberId);
        return ResponseEntity.ok(response);
    }
}
