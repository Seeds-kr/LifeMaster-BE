package com.example.LifeMaster_BE.Report;

import com.example.LifeMaster_BE.Community.Post.PostEntity;
import com.example.LifeMaster_BE.Community.Post.PostRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private ReportService reportService;

    private final MemberEntity member = mock(MemberEntity.class);
    private final PostEntity post = mock(PostEntity.class);

    @Test
    @DisplayName("정상적으로 신고 접수")
    void reportPost_success(){
        Long memberId = 1L;
        Long postId = 100L;
        String reason = "부적절한 게시글";

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(reportRepository.existsByMemberIdAndPostId(memberId, postId)).thenReturn(false);

        reportService.reportPost(memberId, postId, reason);

        verify(reportRepository).save(any(ReportEntity.class));
        verify(member).addReport(any(ReportEntity.class));
        verify(post).addReport(any(ReportEntity.class));
    }

    @Test
    @DisplayName("이미 신고한 게시물일 경우 예외 발생")
    void reportPost_alreadyReported_throwsException(){
        Long memberId = 1L;
        Long postId = 100L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(reportRepository.existsByMemberIdAndPostId(memberId, postId)).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> reportService.reportPost(memberId, postId, "중복 신고"));

        assertEquals("이미 신고한 게시물입니다.", exception.getMessage());
        verify(reportRepository, never()).save(any(ReportEntity.class));
    }
}