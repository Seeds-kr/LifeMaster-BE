//package com.example.LifeMaster_BE.UserManager.Email.Register;
//
//import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
//import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
//import com.example.LifeMaster_BE.UserManager.S3Service;
//import jakarta.persistence.EntityNotFoundException;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class RegisterServiceTest {
//
//    @Mock
//    private MemberRepository memberRepository;
//
//    @Mock
//    private S3Service s3Service;
//
//    @InjectMocks
//    private RegisterService registerService;
//
//
//    @DisplayName("이메일 중복이 없고 비밀번호 일치하면 회원가입 성공")
//    @Test
//    void registerMember_success(){
//
//        String email = "test@example.com";
//        String password = "password";
//
//        when(memberRepository.existsByEmail(email)).thenReturn(false);
//        when(memberRepository.save(any())).thenAnswer(invocation-> invocation.getArgument(0));
//
//        MemberEntity saved = registerService.registerMember(email, password, password);
//
//        assertEquals(email, saved.getEmail());
//        assertTrue(new BCryptPasswordEncoder().matches(password, saved.getPassword()));
//    }
//
//    @DisplayName("이메일이 이미 존재하면 회원가입 시 예외가 발생한다")
//    @Test
//    void registerMember_duplicateEmail_throwsException(){
//
//        String email = "test@example.com";
//        when(memberRepository.existsByEmail(email)).thenReturn(true);
//
//        assertThrows(IllegalArgumentException.class, () -> {
//            registerService.registerMember(email, "pass", "pass");
//        });
//    }
//
//    @DisplayName("비밀번호와 확인 비밀번호 값이 다르면 회원가입 시 예외가 발생한다")
//    @Test
//    void registerMember_passwordMismatch_throwsException(){
//        assertThrows(IllegalArgumentException.class, () -> {
//            registerService.registerMember("test@example.com", "pass1", "pass2");
//        });
//    }
//
//    @DisplayName("닉네임 중복이 없고 이미지 업로드가 성공하면 닉네임과 이미지가 등록된다")
//    @Test
//    void registerMemberWithNickname_success() throws IOException{
//        Long id = 1L;
//        String nickname = "nickname";
//        MultipartFile mockFile = mock(MultipartFile.class);
//
//        MemberEntity member = new MemberEntity("test@mail.com", "pw");
//
//        when(memberRepository.existsByNickname(nickname)).thenReturn(false);
//        when(memberRepository.findById(id)).thenReturn(Optional.of(member));
//        when(mockFile.isEmpty()).thenReturn(false);
//        when(s3Service.uploadFile(mockFile)).thenReturn("https://url.com/img");
//
//        registerService.registerMemberWithNickname(id, nickname, mockFile);
//
//        assertEquals(nickname, member.getNickname());
//        assertEquals("https://url.com/img", member.getImageUrl());
//    }
//
//    @DisplayName("닉네임이 중복되면 예외가 발생한다")
//    @Test
//    void registerMemberWithNickname_duplicateNickname_throwsException(){
//
//        when(memberRepository.existsByNickname("dupe")).thenReturn(true);
//
//        assertThrows(IllegalArgumentException.class, () -> {
//            registerService.registerMemberWithNickname(1L, "dupe", mock(MultipartFile.class));
//        });
//    }
//
//    @DisplayName("존재하지 않는 사용자 ID로 닉네임을 등록하면 예외가 발생한다")
//    @Test
//    void registerMemberWithNickname_userNotFound_throwsException(){
//        when(memberRepository.findById(100L)).thenReturn(Optional.empty());
//
//        assertThrows(EntityNotFoundException.class, () -> {
//            registerService.registerMemberWithNickname(100L, "nick", mock(MultipartFile.class));
//        });
//    }
//
//    @DisplayName("이미지 업로드 중 IOException이 발생하면 예외가 발생한다")
//    @Test
//    void registerMemberWithNickname_imageUploadFails_throwsRuntimeException() throws IOException {
//        Long id = 1L;
//        MultipartFile file = mock(MultipartFile.class);
//        MemberEntity member = new MemberEntity("email", "pw");
//
//        when(memberRepository.existsByNickname("nick")).thenReturn(false);
//        when(memberRepository.findById(id)).thenReturn(Optional.of(member));
//        when(file.isEmpty()).thenReturn(false);
//        when(s3Service.uploadFile(file)).thenThrow(new IOException("upload fail"));
//
//        assertThrows(RuntimeException.class, () -> {
//            registerService.registerMemberWithNickname(id, "nick", file);
//        });
//    }
//}