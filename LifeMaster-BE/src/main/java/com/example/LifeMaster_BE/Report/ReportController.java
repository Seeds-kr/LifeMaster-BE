package com.example.LifeMaster_BE.Report;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<String> reportPost(
            @RequestBody ReportDto reportDto,
            @AuthenticationPrincipal CustomUserDetails user) {

        reportService.reportPost(user.getId(), reportDto.getPostId(), reportDto.getReason());
        return ResponseEntity.ok("신고가 정상적으로 접수되었습니다.");
    }
}
