package com.example.LifeMaster_BE.SelfDevelop.note.thank;

import com.example.LifeMaster_BE.SelfDevelop.note.thank.Dto.CreateThankDto;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.Dto.UpdateThankDto;
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
class ThankServiceTest {

    @Mock
    private ThankRepository thankRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ThankService thankService;

    private MemberEntity member;   // 실객체
    private ThankEntity thank;     // 실객체

    @BeforeEach
    void setUp() {
        member = new MemberEntity();
        thank = new ThankEntity();
    }

    @Test
    @DisplayName("createThank - 회원이 존재하면 저장")
    void createThank_success_setsBothSides() {

        Long memberId = 1L;
        CreateThankDto dto = mock(CreateThankDto.class);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(thankRepository.save(thank)).thenReturn(thank);
        when(dto.toEntity()).thenReturn(thank);

        ThankEntity saved = thankService.createThank(dto, memberId);

        assertSame(thank, saved);

        // 도메인 일관성 검증
        assertTrue(member.getThanks().contains(thank), "member.thanks 컬렉션에 포함되어야 함");
        assertEquals(member, thank.getMember(), "thank.member가 member로 설정되어야 함");

        verify(memberRepository).findById(memberId);
        verify(thankRepository).save(thank);
        verifyNoMoreInteractions(memberRepository, thankRepository);
    }

    @Test
    @DisplayName("updateThank - 존재하는 엔티티의 필드가 갱신")
    void updateThank_success_updatesFields() {

        Long thankId = 10L;

        UpdateThankDto dto = new UpdateThankDto();
        dto.setThankOne("1");
        dto.setThankTwo("4");
        dto.setThankThree("2");
        dto.setThankFour("3");
        dto.setThankFive("5");

        when(thankRepository.findById(thankId)).thenReturn(Optional.of(thank));

        ThankEntity updated = thankService.updateThank(thankId, dto);

        assertSame(thank, updated);
        assertEquals("1", thank.getThankOne());
        assertEquals("4", thank.getThankTwo());
        assertEquals("2", thank.getThankThree());
        assertEquals("3", thank.getThankFour());
        assertEquals("5", thank.getThankFive());

        verify(thankRepository).findById(thankId);
        verify(thankRepository, never()).save(any()); // 서비스 코드가 save() 안 부르는 설계
        verifyNoMoreInteractions(thankRepository);
        verifyNoInteractions(memberRepository);
    }

    @Test
    @DisplayName("deleteThank - 존재하는 5감사 삭제")
    void deleteDiary_delegatesToRepository() {

        Long id = 7L;

        thankService.deleteThank(id);

        verify(thankRepository).deleteById(id);
        verifyNoMoreInteractions(thankRepository);
        verifyNoInteractions(memberRepository);
    }

    @Test
    @DisplayName("createThank  - 회원이 없으면 EntityNotFoundException")
    void createThank_memberNotFound() {

        Long memberId = 404L;
        CreateThankDto dto = mock(CreateThankDto.class);

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> thankService.createThank(dto, memberId));

        verify(memberRepository).findById(memberId);
        verify(thankRepository, never()).save(any());
        verifyNoMoreInteractions(memberRepository);
        verifyNoInteractions(thankRepository);
    }

    @Test
    @DisplayName("updateThank - 엔티티가 없으면 EntityNotFoundException")
    void updateThank_notFound() {

        Long thankId = 999L;

        when(thankRepository.findById(thankId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> thankService.updateThank(thankId, new UpdateThankDto()));

        verify(thankRepository).findById(thankId);
        verify(thankRepository, never()).save(any());
        verifyNoMoreInteractions(thankRepository);
        verifyNoInteractions(memberRepository);
    }
}