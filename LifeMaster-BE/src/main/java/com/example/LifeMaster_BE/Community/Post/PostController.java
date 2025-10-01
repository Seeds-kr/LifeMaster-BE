package com.example.LifeMaster_BE.Community.Post;

import com.example.LifeMaster_BE.Community.Post.Dto.AllPostsDto;
import com.example.LifeMaster_BE.Community.Post.Dto.PostCreateRequest;
import com.example.LifeMaster_BE.Community.Post.Dto.PostGetResponse;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 전체 조회", description = "특정 유형(type)의 게시글 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<AllPostsDto>> getAllPosts(@RequestParam("type") PostType type,
                                                         @AuthenticationPrincipal CustomUserDetails user) {

        List<AllPostsDto> allPosts = postService.getAllPosts(user.getId(), type);
        return ResponseEntity.ok(allPosts);
    }

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    @PostMapping
    public ResponseEntity<Map<String, Long>> newPost(@RequestBody PostCreateRequest postDto,
                                              @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = user.getId();
        String title = postDto.getTitle();
        String content = postDto.getContent();
        String fileUrl = postDto.getFile();
        PostType type = postDto.getType();

        PostEntity post = postService.createPost(title, content, fileUrl, type, memberId);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("postId", post.getId()));
    }

    @Operation(summary = "게시글 조회", description = "게시글 ID를 통해 단일 게시글을 조회합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<PostGetResponse> getPost(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        PostEntity post = postService.getPost(postId);
        PostGetResponse response = new PostGetResponse(
                post.getTitle(),
                post.getContent(),
                post.getFile(),
                post.getType(),
                post.getMember().getId(),
                post.getMember().getNickname(),
                user.getId().equals(post.getMember().getId()),
                post.getCreatedAt()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 수정", description = "게시글 ID를 통해 특정 게시글을 수정합니다.")
    @PatchMapping("/{postId}")
    public void updatePost(@PathVariable("postId") Long postId,
                           @RequestBody PostCreateRequest postDto,
                           @AuthenticationPrincipal CustomUserDetails user){
        String title = postDto.getTitle();
        String content = postDto.getContent();
        String fileUrl = postDto.getFile();

        postService.updatePost(postId, title, content, fileUrl, user.getId());
    }

    @Operation(summary = "게시글 삭제", description = "게시글 ID를 통해 특정 게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable("postId") Long postId,
                           @AuthenticationPrincipal CustomUserDetails user){
        postService.deletePost(postId, user.getId());
    }

    // 인기글 조회 (Redis에서 가져오기)
    @Operation(summary = "인기 게시글 조회", description = "Redis에서 인기 게시글 목록을 가져옵니다.")
    @GetMapping("/popular")
    public ResponseEntity<List<AllPostsDto>> getPopularPosts(@AuthenticationPrincipal CustomUserDetails user) {
        List<AllPostsDto> popularPosts = postService.getPopularPosts(user.getId());
        return ResponseEntity.ok(popularPosts);
    }
}
