package com.example.LifeMaster_BE.SelfDevelop.note.thank;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.Dto.CreateThankDto;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.Dto.UpdateThankDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/schedule/self-reflection/thank")
@RequiredArgsConstructor
public class ThankController {

    private final ThankService thankService;

    @PostMapping
    public ResponseEntity<Void> newThank(
            @RequestBody CreateThankDto thankDto,
            @AuthenticationPrincipal CustomUserDetails user){
        Long memberId = user.getId();
        ThankEntity createdThank = thankService.createThank(thankDto, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{thank-id}")
    public ResponseEntity<ThankEntity> editThank(
            @RequestBody UpdateThankDto thankDto,
            @PathVariable("thank-id") Long thankId){
        ThankEntity updatedThank = thankService.updateThank(thankId, thankDto);
        return ResponseEntity.ok(updatedThank);
    }

    @DeleteMapping("/{thank-id}")
    public ResponseEntity<Void> deleteThank(
            @PathVariable("thank-id") Long thankId){
        thankService.deleteThank(thankId);
        return ResponseEntity.noContent().build();
    }
}
