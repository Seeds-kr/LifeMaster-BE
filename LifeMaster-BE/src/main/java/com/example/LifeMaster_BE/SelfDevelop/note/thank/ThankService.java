package com.example.LifeMaster_BE.SelfDevelop.note.thank;

import com.example.LifeMaster_BE.SelfDevelop.note.thank.Dto.CreateThankDto;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.Dto.ThankResponse;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.Dto.UpdateThankDto;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ThankService {

    private final ThankRepository thankRepository;
    private final MemberRepository memberRepository;

    public ThankEntity createThank(CreateThankDto thankDto, Long memberId){

        ThankEntity newThank1 = thankDto.toEntity();
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        ThankEntity newThank = newThank1.toBuilder()
                .member(member)
                .build();
        return thankRepository.save(newThank);
    }

    public ThankResponse getThank(Long thankId, Long memberId){
        ThankEntity thank = thankRepository.findByIdAndMember_Id(thankId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("Thank not found or no permission"));

        return ThankResponse.builder()
                .thankOne(thank.getThankOne())
                .thankTwo(thank.getThankTwo())
                .thankThree(thank.getThankThree())
                .thankFour(thank.getThankFour())
                .thankFive(thank.getThankFive())
                .thankDate(thank.getThankDate())
                .build();
    }
    public ThankEntity updateThank(Long thankId, UpdateThankDto thankDto, Long memberId){
        ThankEntity thank = thankRepository.findByIdAndMember_Id(thankId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("Thank not found or no permission"));
        return updateThankData(thank, thankDto);
    }

    public void deleteThank(Long thankId){
        thankRepository.deleteById(thankId);
    }

    private ThankEntity updateThankData(ThankEntity thank, UpdateThankDto thankDto){
        thank.setThankOne(thankDto.getThankOne());
        thank.setThankTwo(thankDto.getThankTwo());
        thank.setThankThree(thankDto.getThankThree());
        thank.setThankFour(thankDto.getThankFour());
        thank.setThankFive(thankDto.getThankFive());

        return thank;
    }

    public ThankEntity getThankByIdAndMemberId(Long thankId, Long memberId) {
        return thankRepository.findByIdAndMember_Id(thankId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("Thank not found"));
    }

    public void deleteThank(Long thankId, Long memberId) {
        ThankEntity thank = getThankByIdAndMemberId(thankId, memberId);
        thankRepository.delete(thank);
    }

    public long countThankByMemberIdAndDate(Long memberId, LocalDate thankDate) {
        return thankRepository.countByMember_IdAndThankDate(memberId, thankDate);
    }
}
