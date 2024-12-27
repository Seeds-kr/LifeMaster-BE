package com.example.LifeMaster_BE.Challenge.Detox;

import com.example.LifeMaster_BE.UserManager.User;
import com.example.LifeMaster_BE.UserManager.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DetoxService {

    private final DetoxRepository detoxRepository;
    private final UserRepository userRepository;

    public Detox addRepeatedLock(Long userId, DetoxDto.Request request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Detox detox = Detox.builder()
                .detoxLock(request.getDetoxLock())
                .detoxUse(request.getDetoxUse())
                .detoxBreack(request.getDetoxBreak())
                .user(user)
                .build();

        return detoxRepository.save(detox);
    }

    public List<Detox> getRepeatedLocksByUser(Long userId) {
        return detoxRepository.findByUserId(userId);
    }

    public Detox updateRepeatedLock(Long lockId, DetoxDto.Request request) {
        Detox detox = detoxRepository.findById(lockId)
                .orElseThrow(() -> new RuntimeException("Lock not found"));

        detox.setDetoxLock(request.getDetoxLock());
        detox.setDetoxUse(request.getDetoxUse());
        detox.setDetoxBreack(request.getDetoxBreak());

        return detoxRepository.save(detox);
    }

    public void deleteRepeatedLock(Long lockId) {
        detoxRepository.deleteById(lockId);
    }
}
