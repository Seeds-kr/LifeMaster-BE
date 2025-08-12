package com.example.LifeMaster_BE.SelfDevelop.note.diary;

import com.example.LifeMaster_BE.SelfDevelop.note.diary.Dto.CreateDiaryDto;
import com.example.LifeMaster_BE.SelfDevelop.note.diary.Dto.UpdateDiaryDto;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
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
class DiaryServiceTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private DiaryService diaryService;

    private MemberEntity member;
    private DiaryEntity diary;

    @BeforeEach
    void setUp(){
        member = new MemberEntity();
        diary = new DiaryEntity();
        diary.setDiaryContent("old Content");
    }

    @Test
    @DisplayName("createDiary - 회원이 존재하면 일기 저장 성공")
    void createDiary_success(){

        Long memberId = 1L;
        CreateDiaryDto mock = mock(CreateDiaryDto.class);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(diaryRepository.save(diary)).thenReturn(diary);
        when(mock.toEntity()).thenReturn(diary);

        DiaryEntity saved = diaryService.createDiary(mock, memberId);

        assertSame(diary, saved);
        verify(memberRepository).findById(memberId);
        verify(diaryRepository).save(diary);
        verifyNoMoreInteractions(diaryRepository, memberRepository);
    }

    @Test
    @DisplayName("editDiary - 존재하는 일기의 내용을 수정")
    void editDiary_success(){

        Long diaryId = 1L;
        UpdateDiaryDto dto = new UpdateDiaryDto();
        dto.setDiaryContent("new Content");

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(diary));

        DiaryEntity edited = diaryService.updateDiary(diaryId, dto);

        assertSame(diary, edited);
        assertEquals("new Content", diary.getDiaryContent());
        verify(diaryRepository).findById(diaryId);

        // 서비스 코드 상 save를 호출하지 않으므로 아래 검증
        verify(diaryRepository, never()).save(any());
        verifyNoMoreInteractions(diaryRepository);
        verifyNoInteractions(memberRepository);
    }

    @Test
    @DisplayName("deleteDiary - 존재하는 일기 삭제")
    void deleteDiary_callsRepository() {

        Long diaryId = 11L;

        diaryService.deleteDiary(diaryId);

        verify(diaryRepository).deleteById(diaryId);
        verifyNoInteractions(memberRepository);
    }

    @Test
    @DisplayName("createDiary - 회원이 없으면 EntityNotFoundException")
    void createDiary_memberNotFound() {

        Long memberId = 99L;
        CreateDiaryDto mock = mock(CreateDiaryDto.class);

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> diaryService.createDiary(mock, memberId));

        verify(memberRepository).findById(memberId);
        verify(diaryRepository, never()).save(any());
    }

    @Test
    @DisplayName("editDiary - 일기가 없으면 EntityNotFoundException")
    void editDiary_notFound() {
        // given
        Long diaryId = 404L;
        when(diaryRepository.findById(diaryId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class,
                () -> diaryService.updateDiary(diaryId, new UpdateDiaryDto()));

        verify(diaryRepository).findById(diaryId);
        verify(diaryRepository, never()).save(any());
    }
}