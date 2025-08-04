package com.example.LifeMaster_BE.TimeManager.Sleep.WhiteNoise;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.annotation.PostConstruct;
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
        return repository.findByMemberIsNullOrMember(member);  // 공용 + 개인
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

    public WhiteNoiseEntity registerDefaultWhiteNoise(String title, String description, String thumbnailUrl, String audioUri, MusicCategory category) {
        WhiteNoiseEntity entity = new WhiteNoiseEntity();
        entity.setTitle(title);
        entity.setDescription(description);
        entity.setThumbnailUrl(thumbnailUrl);
        entity.setAudioUri(audioUri);
        entity.setCategory(category);
        entity.setMember(null);

        return repository.save(entity);
    }

    //mp3 음원 자동 삽입
    @PostConstruct
    public void initDefaultWhiteNoises() {
        if (repository.count() == 0) {
            String defaultNatureThumbnail = "http://localhost:8080/images/default_nature.jpg";
            String defaultWhiteNoiseThumbnail = "http://localhost:8080/images/default_white.jpg";
            String defaultClassicalThumbnail = "http://localhost:8080/images/default_classical.jpg";

            // 자연의 소리 5개
            registerDefaultWhiteNoise("ASMR 물소리 효과", "편안함을 주는 자연의 ASMR 물소리입니다.",
                    defaultNatureThumbnail, "http://localhost:8080/audio/nature/asmr_water.mp3", MusicCategory.NATURE_SOUNDS);

            registerDefaultWhiteNoise("자연의 숲속 새소리", "숲속을 걷는 듯한 새소리입니다.",
                    defaultNatureThumbnail, "http://localhost:8080/audio/nature/forest_birds.mp3", MusicCategory.NATURE_SOUNDS);

            registerDefaultWhiteNoise("잔잔한 물소리", "차분한 물 흐르는 소리입니다.",
                    defaultNatureThumbnail, "http://localhost:8080/audio/nature/water_sounds.mp3", MusicCategory.NATURE_SOUNDS);

            registerDefaultWhiteNoise("강물 소리", "자연스러운 강물 소리로 안정감을 줍니다.",
                    defaultNatureThumbnail, "http://localhost:8080/audio/nature/river_sound.mp3", MusicCategory.NATURE_SOUNDS);

            registerDefaultWhiteNoise("호수의 물소리", "스코틀랜드의 고요한 호숫가 물소리입니다.",
                    defaultNatureThumbnail, "http://localhost:8080/audio/nature/loch_water.mp3", MusicCategory.NATURE_SOUNDS);

            // 백색소음 4개
            registerDefaultWhiteNoise("30분 빗소리", "수면 유도를 위한 부드러운 빗소리",
                    defaultWhiteNoiseThumbnail, "http://localhost:8080/audio/whiteNoise/30min_gentle_rain.mp3", MusicCategory.WHITE_NOISE);

            registerDefaultWhiteNoise("복합 수면 소리", "Ocean, Thunder, White Noise, Nature 등이 포함된 복합 수면 사운드",
                    defaultWhiteNoiseThumbnail, "http://localhost:8080/audio/whiteNoise/royalty_free_sleep_sounds.mp3", MusicCategory.WHITE_NOISE);

            registerDefaultWhiteNoise("비 사운드 효과", "자장가처럼 들리는 비 소리 효과",
                    defaultWhiteNoiseThumbnail, "http://localhost:8080/audio/whiteNoise/rain_sound_effect.mp3", MusicCategory.WHITE_NOISE);

            registerDefaultWhiteNoise("저작권 없는 화이트노이즈", "저작권 걱정 없이 사용할 수 있는 백색소음",
                    defaultWhiteNoiseThumbnail, "http://localhost:8080/audio/whiteNoise/no_copyright_white_noise.mp3", MusicCategory.WHITE_NOISE);

            // 클래식 5개
            registerDefaultWhiteNoise("Whales Of The North", "바다를 연상시키는 오케스트라 사운드",
                    defaultClassicalThumbnail, "http://localhost:8080/audio/classical/whales_of_the_north.mp3", MusicCategory.CLASSICAL);

            registerDefaultWhiteNoise("Filaments", "Scott Buckley의 부드러운 클래식 크로스오버 곡",
                    defaultClassicalThumbnail, "http://localhost:8080/audio/classical/filaments.mp3", MusicCategory.CLASSICAL);

            registerDefaultWhiteNoise("Beethoven - Coriolan Overture", "베토벤의 명곡 코리올란 서곡",
                    defaultClassicalThumbnail, "http://localhost:8080/audio/classical/beethoven_coriolan.mp3", MusicCategory.CLASSICAL);

            registerDefaultWhiteNoise("Mozart - Magic Flute Overture", "모차르트의 마술피리 서곡",
                    defaultClassicalThumbnail, "http://localhost:8080/audio/classical/mozart_magic_flute.mp3", MusicCategory.CLASSICAL);

            registerDefaultWhiteNoise("Chopin - Nocturne Op.9 No.2", "쇼팽의 녹턴 E♭장조, Op.9 No.2",
                    defaultClassicalThumbnail, "http://localhost:8080/audio/classical/chopin_nocturne.mp3", MusicCategory.CLASSICAL);
        }
    }



    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
