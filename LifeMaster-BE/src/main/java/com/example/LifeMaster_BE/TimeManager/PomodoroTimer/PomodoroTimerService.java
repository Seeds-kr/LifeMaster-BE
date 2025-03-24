package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PomodoroTimerService {
    @Autowired
    private PomodoroTimerRepository repository;

    private final MemberRepository memberRepository;

    public PomodoroTimerService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<PomodoroTimerEntity> findAll() {
        return repository.findAll();
    }

    public Optional<PomodoroTimerEntity> findById(Long id) {
        return repository.findById(id);
    }

    public List<PomodoroTimerEntity> findByDate(String date) {
        return repository.findByDate(date);
    }

    public PomodoroTimerEntity create(PomodoroTimerEntity pomodoroTimer,Long memberId) {
        // 멤버 확인
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        pomodoroTimer.setMember(member);
        return repository.save(pomodoroTimer);
    }

    public PomodoroTimerEntity save(PomodoroTimerEntity pomodoroTimer) {
        return repository.save(pomodoroTimer);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public void deleteAllByDate(String date) {
        repository.deleteAllByDate(date);  // 날짜 기준 모든 타이머 삭제
    }
}
