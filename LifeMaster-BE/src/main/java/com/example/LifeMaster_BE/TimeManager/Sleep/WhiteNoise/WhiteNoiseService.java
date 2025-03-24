package com.example.LifeMaster_BE.TimeManager.Sleep.WhiteNoise;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WhiteNoiseService {

    private final WhiteNoiseRepository repository;

    private final MemberRepository memberRepository;

    public WhiteNoiseService(WhiteNoiseRepository repository, MemberRepository memberRepository) {
        this.repository = repository;
        this.memberRepository = memberRepository;
    }

    public List<WhiteNoiseEntity> findAll() {
        return repository.findAll();
    }

    public Optional<WhiteNoiseEntity> findById(Long id) {
        return repository.findById(id);
    }

    public WhiteNoiseEntity create(WhiteNoiseEntity whiteNoise,Long memberId) {
        // 멤버 확인
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        whiteNoise.setMember(member);

        return repository.save(whiteNoise);
    }

    public WhiteNoiseEntity save(WhiteNoiseEntity whiteNoise) {
        return repository.save(whiteNoise);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

