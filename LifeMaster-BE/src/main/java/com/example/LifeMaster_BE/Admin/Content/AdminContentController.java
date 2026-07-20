package com.example.LifeMaster_BE.Admin.Content;

import com.example.LifeMaster_BE.Admin.Content.Dto.AdminCommentListDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Admin - Content",
        description = "관리자 댓글 관리 API"
)
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminContentController {

    private final AdminContentService adminContentService;

    @Operation(
            summary = "댓글 목록 조회",
            description = "댓글 목록을 페이징으로 조회합니다."
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminCommentListDto>> getComments(
            @Parameter(description = "검색 키워드 (댓글 내용)")
            @RequestParam(required = false) String keyword,

            @PageableDefault(
                    size = 20,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Page<AdminCommentListDto> comments =
                adminContentService.getComments(keyword, pageable);

        return ResponseEntity.ok(comments);
    }

    @Operation(
            summary = "댓글 삭제",
            description = "관리자가 특정 댓글을 삭제합니다."
    )
    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "댓글 ID")
            @PathVariable Long commentId
    ) {
        adminContentService.deleteComment(commentId);

        return ResponseEntity.noContent().build();
    }
}