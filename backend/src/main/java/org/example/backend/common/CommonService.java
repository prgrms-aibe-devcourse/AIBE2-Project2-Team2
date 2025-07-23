package org.example.backend.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.constant.Status;
import org.example.backend.entity.Member;
import org.example.backend.exception.customException.MemberNotFoundException;
import org.example.backend.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommonService {

    private final MemberRepository memberRepository;


    public CommonResponseDto checkUserInfo(String email) {
        // 이메일로 유저 정보 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("해당 이메일의 사용자가 존재하지 않습니다."));

        // 유저 상태가 ACTIVE인지 확인
        if (member.getStatus() != Status.ACTIVE) {
            throw new MemberNotFoundException("해당 이메일의 사용자가 활성화 상태가 아닙니다.");
        }

        return CommonResponseDto.builder()
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImageUrl() != null ? member.getProfileImageUrl() : "")
                .role(member.getRole().name())
                .build();
    }
}
