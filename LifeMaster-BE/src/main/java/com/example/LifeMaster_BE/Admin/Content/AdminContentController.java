package com.example.LifeMaster_BE.Admin.Content;

import com.example.LifeMaster_BE.Admin.Content.Dto.AdminCommentListDto;
import com.example.LifeMaster_BE.Admin.Content.Dto.AdminPostDetailDto;
import com.example.LifeMaster_BE.Admin.Content.Dto.AdminPostListDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Content", description = "관리자 콘텐츠 관리 API")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminContentController {

    private final AdminContentService adminContentService;

    @Operation(summary = "게시글 목록 조회", description = "게시글 목록을 페이징으로 조회합니다.")
    @GetMapping("/posts")
    public ResponseEntity<Page<AdminPostListDto>> getPosts(
            @Parameter(description = "검색 키워드 (제목, 내용)") @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AdminPostListDto> posts = adminContentService.getPosts(keyword, pageable);
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<AdminPostDetailDto> getPostDetail(
            @Parameter(description = "게시글 ID") @PathVariable Long postId) {
        AdminPostDetailDto post = adminContentService.getPostDetail(postId);
        return ResponseEntity.ok(post);
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "게시글 ID") @PathVariable Long postId) {
        adminContentService.deletePost(postId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 목록 조회", description = "댓글 목록을 페이징으로 조회합니다.")
    @GetMapping("/comments")
    public ResponseEntity<Page<AdminCommentListDto>> getComments(
            @Parameter(description = "검색 키워드 (댓글 내용)") @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AdminCommentListDto> comments = adminContentService.getComments(keyword, pageable);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId) {
        adminContentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
}
