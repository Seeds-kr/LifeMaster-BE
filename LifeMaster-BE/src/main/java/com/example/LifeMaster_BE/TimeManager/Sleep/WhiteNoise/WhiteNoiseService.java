package com.example.LifeMaster_BE.TimeManager.Sleep.WhiteNoise;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WhiteNoiseService {

    private final WhiteNoiseRepository repository;

    public WhiteNoiseService(WhiteNoiseRepository repository) {
        this.repository = repository;
    }

    public List<WhiteNoiseEntity> findAll() {
        return repository.findAll();
    }

    public Optional<WhiteNoiseEntity> findById(Long id) {
        return repository.findById(id);
    }

    public WhiteNoiseEntity save(WhiteNoiseEntity whiteNoise) {
        return repository.save(whiteNoise);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

