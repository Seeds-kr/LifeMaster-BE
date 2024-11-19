package com.example.LifeMaster_BE.SelfDevelop.note.thank;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/schedule/self-reflection/thank")
@RequiredArgsConstructor
public class ThankController {

    private final ThankService thankService;

    @PostMapping
    public ResponseEntity<ThankEntity> newThank(
            @RequestBody ThankEntity thank){
        ThankEntity createdThank = thankService.createThank(thank);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdThank);
    }

    @PatchMapping("/{thank-id}")
    public ResponseEntity<ThankEntity> editThank(
            @RequestBody ThankUpdateDto thankDto,
            @PathVariable("thank-id") Long thankId){
        ThankEntity updatedThank = thankService.editDiary(thankId, thankDto);
        return ResponseEntity.ok(updatedThank);
    }

    @DeleteMapping("/{thank-id}")
    public ResponseEntity<Void> deleteThank(
            @PathVariable("thank-id") Long thankId){
        thankService.deleteDiary(thankId);
        return ResponseEntity.noContent().build();
    }
}
