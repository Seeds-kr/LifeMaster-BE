package com.example.LifeMaster_BE.UserManagerTest.Register;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RegisterServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Commit
    void testFindByEmail() {
        // Arrange: 테스트 데이터를 준비
        String email = "test@example.com";
        String password = "password123";

        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setEmail(email);
        memberEntity.setPassword(password);

        memberRepository.save(memberEntity);

        // Act: findByEmail 메서드 호출
        Optional<MemberEntity> optionalMember = memberRepository.findByEmail(email);

        // Assert: 저장된 데이터가 제대로 조회되었는지 확인
        assertTrue(optionalMember.isPresent(), "MemberEntity should be found");
        MemberEntity foundMember = optionalMember.get();
        assertEquals(email, foundMember.getEmail(), "Email should match");
        assertEquals(password, foundMember.getPassword(), "Password should match");
    }
}