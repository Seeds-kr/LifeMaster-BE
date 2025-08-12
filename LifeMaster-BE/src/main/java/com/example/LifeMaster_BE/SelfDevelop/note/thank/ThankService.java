package com.example.LifeMaster_BE.SelfDevelop.note.thank;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ThankService {

    private final ThankRepository thankRepository;
    private final MemberRepository memberRepository;

    public ThankEntity createThank(ThankEntity thank, Long memberId){
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        member.addThank(thank);
        return thankRepository.save(thank);
    }

    public ThankEntity updateThank(Long thankId, ThankUpdateDto thankDto){
        ThankEntity thank = thankRepository.findById(thankId)
                .orElseThrow(() -> new EntityNotFoundException("Thank not found"));
        return updateThankData(thank, thankDto);
    }

    public void deleteThank(Long thankId){
        thankRepository.deleteById(thankId);
    }

    private ThankEntity updateThankData(ThankEntity thank, ThankUpdateDto thankDto){
        thank.setThankOne(thankDto.getThankOne());
        thank.setThankTwo(thankDto.getThankTwo());
        thank.setThankThree(thankDto.getThankThree());
        thank.setThankFour(thankDto.getThankFour());
        thank.setThankFive(thankDto.getThankFive());

        return thank;
    }
}
