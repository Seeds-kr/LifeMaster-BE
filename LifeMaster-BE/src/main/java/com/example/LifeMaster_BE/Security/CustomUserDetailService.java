package com.example.LifeMaster_BE.Security;

import com.example.LifeMaster_BE.Exception.CustomException.MemberSuspendedException;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() ->  new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        // 정지 상태 체크
        if (member.isSuspended()) {
            throw new MemberSuspendedException(member.getSuspendedUntil());
        }

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + member.getLoginRole().name())
        );

        return new CustomUserDetails(
                member.getId(),
                member.getEmail(),
                member.getPassword(),
                member.getNickname(),
                authorities
        );
    }
}
