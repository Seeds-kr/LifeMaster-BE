package com.example.LifeMaster_BE.ETC.MyPage;

import com.example.LifeMaster_BE.ETC.MyPage.Dto.MyPageDto;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MyPageService myPageService;

    @Test
    @DisplayName("회원정보 가져오기 성공")
    void getMemberInfo_success(){

        Long memberId = 1L;
        MemberEntity member = new MemberEntity();
        member.setNickname("testNick");
        member.setEmail("test@example.com");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        MyPageDto result = myPageService.getMemberInfo(memberId);

        assertEquals("testNick", result.getNickName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("회원정보 가져오기 실패(존재하지 않는 회원)")
    void getMemberInfo_notFound(){

        Long memberId = 99L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> myPageService.getMemberInfo(memberId));
    }

    @Test
    @DisplayName("회원정보 수정 성공")
    void updateMemberInfo_success(){

        Long memberId = 1L;

        MemberEntity member = new MemberEntity();
        member.setNickname("oldNick");
        member.setEmail("old@example.com");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberRepository.save(member)).thenReturn(member);

        MyPageDto result = myPageService.updateMemberInfo(memberId, "newNick", "new@example.com");

        assertEquals("newNick", result.getNickName());
        assertEquals("new@example.com", result.getEmail());
    }

    @Test
    @DisplayName("회원정보 수정 실패(존재하지 않는 회원)")
    void updateMemberInfo_notFound(){

        Long memberId = 99L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> myPageService.updateMemberInfo(memberId, "testNick", "test@example.com"));
    }

    @Test
    @DisplayName("회원정보 삭제 성공")
    void deleteMemberInfo_success(){

        Long memberId = 1L;
        MemberEntity member = new MemberEntity();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        myPageService.deleteMemberInfo(memberId);

        // Soft delete이므로 save() 호출됨
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("회원정보 삭제 성공")
    void deleteMemberInfo_notFound(){
        Long memberId = 99L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> myPageService.deleteMemberInfo(memberId));
    }
}