package com.example.LifeMaster_BE.TimeManager.Sleep.WhiteNoise;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
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

    public WhiteNoiseEntity create(WhiteNoiseDTO whiteNoise,Long memberId) {
        WhiteNoiseEntity whiteNoiseEntity = new WhiteNoiseEntity();
        // 멤버 확인
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        whiteNoiseEntity.setMember(member);
        whiteNoiseEntity.setUrl(whiteNoise.getUrl());
        whiteNoiseEntity.setTitle(whiteNoise.getTitle());
        whiteNoiseEntity.setLength(whiteNoise.getLength());

        return repository.save(whiteNoiseEntity);
    }

    public ResponseEntity<?> save(Long id, WhiteNoiseDTO whiteNoiseDTO) {
        return repository.findById(id)
                .map(existingNoise -> {
                    existingNoise.setTitle(whiteNoiseDTO.getTitle());
                    existingNoise.setUrl(whiteNoiseDTO.getUrl());
                    existingNoise.setLength(whiteNoiseDTO.getLength());

                    WhiteNoiseEntity updatedNoise = repository.save(existingNoise);
                    return ResponseEntity.ok(updatedNoise);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

