package com.example.LifeMaster_BE.Challenge.Detox.PermanentDetox;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PermanentDetoxService {

    private final PermanentDetoxRepository permanentDetoxRepository;
    private final MemberRepository memberRepository;

    public PermanentDetoxResponseDTO createPermanentDetox(Long loginMemberId, PermanentDetoxRequestDTO requestDTO) {
        if (permanentDetoxRepository.existsByMember_Id(loginMemberId)) {
            throw new IllegalArgumentException("이미 영구 잠금 디톡스가 존재합니다.");
        }

        MemberEntity member = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        PermanentDetoxEntity entity = new PermanentDetoxEntity();
        entity.setMember(member);
        entity.setLockedApps(requestDTO.getLockedApps());

        PermanentDetoxEntity saved = permanentDetoxRepository.save(entity);
        return new PermanentDetoxResponseDTO(saved.getLockedApps());
    }

    @Transactional(readOnly = true)
    public PermanentDetoxResponseDTO getPermanentDetox(Long loginMemberId) {
        PermanentDetoxEntity entity = permanentDetoxRepository.findByMember_Id(loginMemberId)
                .orElseThrow(() -> new IllegalArgumentException("영구 잠금 디톡스가 존재하지 않습니다."));

        return new PermanentDetoxResponseDTO(entity.getLockedApps());
    }

    public PermanentDetoxResponseDTO updatePermanentDetox(Long loginMemberId, PermanentDetoxRequestDTO requestDTO) {
        PermanentDetoxEntity entity = permanentDetoxRepository.findByMember_Id(loginMemberId)
                .orElseThrow(() -> new IllegalArgumentException("영구 잠금 디톡스가 존재하지 않습니다."));

        entity.setLockedApps(requestDTO.getLockedApps());

        PermanentDetoxEntity updated = permanentDetoxRepository.save(entity);
        return new PermanentDetoxResponseDTO(updated.getLockedApps());
    }

    public void deletePermanentDetox(Long loginMemberId) {
        PermanentDetoxEntity entity = permanentDetoxRepository.findByMember_Id(loginMemberId)
                .orElseThrow(() -> new IllegalArgumentException("영구 잠금 디톡스가 존재하지 않습니다."));

        permanentDetoxRepository.delete(entity);
    }
}
