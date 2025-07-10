package com.example.LifeMaster_BE.ETC.MyPage;

import com.example.LifeMaster_BE.ETC.MyPage.Dto.MyPageDto;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MyPageService {

    private final MemberRepository memberRepository;

    public MyPageDto getMemberInfo(Long memberId){
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member Not Found"));

        return new MyPageDto(member.getNickname(), member.getEmail());
    }

    public MyPageDto updateMemberInfo(Long memberId, String nickName, String email){
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member Not Found"));

        member.updateByMyPage(nickName, email);
        memberRepository.save(member);

        return new MyPageDto(member.getNickname(), member.getEmail());
    }

    public void deleteMemberInfo(Long memberId){
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member Not Found"));

        memberRepository.delete(member);
    }
}
