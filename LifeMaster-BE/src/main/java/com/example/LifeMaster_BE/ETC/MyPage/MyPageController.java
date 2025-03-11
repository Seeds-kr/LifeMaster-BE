package com.example.LifeMaster_BE.ETC.MyPage;

import com.example.LifeMaster_BE.ETC.MyPage.Dto.MyPageDto;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<MyPageDto> getMMember(
            @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = user.getId();
        MyPageDto memberInfo = myPageService.getMemberInfo(memberId);

        return ResponseEntity.ok(memberInfo);
    }

    @PatchMapping
    public ResponseEntity<MyPageDto> updateMember(
            @RequestBody MyPageDto dto,
            @AuthenticationPrincipal CustomUserDetails user) {
        String nickName = dto.getNickName();
        String email = dto.getEmail();
        Long memberId = user.getId();

        MyPageDto myPageDto = myPageService.updateMemberInfo(memberId, nickName, email);
        return ResponseEntity.ok(myPageDto);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMember(
            @AuthenticationPrincipal CustomUserDetails user){
        Long memberId = user.getId();
        myPageService.deleteMemberInfo(memberId);

        return ResponseEntity.noContent().build();
    }

}
