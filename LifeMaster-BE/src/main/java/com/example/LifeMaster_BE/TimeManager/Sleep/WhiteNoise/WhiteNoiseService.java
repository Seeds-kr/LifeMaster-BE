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

    public List<WhiteNoiseEntity> getAllForUser(MemberEntity member) {
        return repository.findAllByMember(member);
    }

    public Optional<WhiteNoiseEntity> findById(Long id) {
        return repository.findById(id);
    }

    public WhiteNoiseEntity create(WhiteNoiseDTO dto, Long memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        WhiteNoiseEntity entity = new WhiteNoiseEntity();
        entity.setMember(member);
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setThumbnailUrl(dto.getThumbnailUrl());
        entity.setAudioUri(dto.getAudioUri());
        entity.setCategory(dto.getCategory());

        return repository.save(entity);
    }

    public ResponseEntity<?> save(Long id, WhiteNoiseDTO dto) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setTitle(dto.getTitle());
                    existing.setDescription(dto.getDescription());
                    existing.setThumbnailUrl(dto.getThumbnailUrl());
                    existing.setAudioUri(dto.getAudioUri());
                    existing.setCategory(dto.getCategory());

                    return ResponseEntity.ok(repository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
