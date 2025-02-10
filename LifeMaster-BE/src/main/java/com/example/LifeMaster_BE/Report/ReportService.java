package com.example.LifeMaster_BE.Report;

import com.example.LifeMaster_BE.Community.Post.PostEntity;
import com.example.LifeMaster_BE.Community.Post.PostRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    public void reportPost(Long memberId, Long postId, String reason){

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        if (reportRepository.existsByMemberIdAndPostId(memberId, postId)){
            throw new IllegalStateException("이미 신고한 게시물입니다.");
        }

        ReportEntity reportEntity = new ReportEntity(member, post, reason);

        member.addReport(reportEntity);
        post.addReport(reportEntity);
        reportRepository.save(reportEntity);
    }
}
