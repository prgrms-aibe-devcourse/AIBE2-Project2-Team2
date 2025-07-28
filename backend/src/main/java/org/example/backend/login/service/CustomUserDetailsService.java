package org.example.backend.login.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Member;
import org.example.backend.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // DB에서 email로 관리자 조회, 없으면 예외 발생
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + email));

        // UserDetails 객체 생성 및 반환
        return new org.springframework.security.core.userdetails.User(
                member.getEmail(),            // 이메일
                member.getPassword(),        // 암호화된 비밀번호
                true,                      // 계정 활성화 여부 (true: 활성화)
                true,                      // 계정 만료 여부 (true: 만료 안됨)
                true,                      // 자격증명(비밀번호) 만료 여부 (true: 만료 안됨)
                true,                      // 계정 잠김 여부 (true: 잠기지 않음)
                member.getRole().getAuthorities()  // 권한 리스트
        );
    }

}
