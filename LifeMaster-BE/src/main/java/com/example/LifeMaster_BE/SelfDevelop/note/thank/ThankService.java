package com.example.LifeMaster_BE.SelfDevelop.note.thank;

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

    public ThankEntity createThank(ThankEntity thank){
        return thankRepository.save(thank);
    }

    public ThankEntity editDiary(Long thankId, ThankUpdateDto thankDto){
        ThankEntity thank = thankRepository.findById(thankId)
                .orElseThrow(() -> new RuntimeException("Diary not found"));
        return updateThankData(thank, thankDto);
    }

    public void deleteDiary(Long diaryId){
        thankRepository.deleteById(diaryId);
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
