package com.example.LifeMaster_BE.Community.Post;

import com.example.LifeMaster_BE.Community.Post.Dto.AllPostsDto;
import com.example.LifeMaster_BE.Community.Post.Dto.PostDto;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final MemberRepository memberRepository;

    @GetMapping
    public ResponseEntity<List<AllPostsDto>> getAllPosts(@RequestParam("type") PostType type, @AuthenticationPrincipal User user) {

        String email = user.getUsername();
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(email));

        List<AllPostsDto> allPosts = postService.getAllPosts(member.getId(), type);
        return ResponseEntity.ok(allPosts);
    }

    @PostMapping
    public ResponseEntity<PostEntity> newPost(@RequestBody PostDto postDto, @AuthenticationPrincipal User user) {
        String email = user.getUsername();
        String title = postDto.getTitle();
        String content = postDto.getContent();
        String fileUrl = postDto.getFile();
        PostType type = postDto.getType();

        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(email));


        PostEntity post = postService.createPost(title, content, fileUrl, type, member);

        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostEntity> getPost(@PathVariable Long postId){
        PostEntity post = postService.getPost(postId);
        return ResponseEntity.ok(post);
    }

    @PatchMapping("/{postId}")
    public void updatePost(@PathVariable Long postId,
                           @RequestBody PostDto postDto, @AuthenticationPrincipal User user){
        String title = postDto.getTitle();
        String content = postDto.getContent();
        String fileUrl = postDto.getFile();
        String email = user.getUsername();
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(email));

        postService.updatePost(postId, title, content, fileUrl, member.getId());
    }

    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable Long postId){
        postService.deletePost(postId);
    }

    // 인기글 조회 (Redis에서 가져오기)
    @GetMapping("/popular")
    public List<PostEntity> getPopularPosts() {
        return postService.getPopularPosts();
    }
}
