package org.example.backend.mypage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.Member;
import org.example.backend.exception.customException.MemberNotFoundException;
import org.example.backend.mypage.dto.request.NicknameUpdateRequestDto;
import org.example.backend.mypage.dto.response.MyPageResponseDto;
import org.example.backend.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MyPageService {
    private final MemberRepository memberRepository;

    // 내 정보 조회 메서드
    @Transactional(readOnly = true)
    public MyPageResponseDto getMyPageInfo(String email) {
        log.info("서비스 시작 - 이메일: {}", email);

        try {
            MyPageResponseDto dto = memberRepository.findMyPageInfoByEmail(email);
            log.info("리포지토리 결과: {}", dto);

            if (dto == null) {
                log.warn("회원 정보 없음 - 이메일: {}", email);
                throw new MemberNotFoundException("해당 이메일의 사용자가 존재하지 않습니다.");
            }

            return dto;
        } catch (Exception e) {
            log.error("서비스 오류 발생 - 이메일: {}, 오류: {}", email, e.getMessage(), e);
            throw e;
        }
    }

    // 닉네임 업데이트 메서드
    public void updateNickname(String email, NicknameUpdateRequestDto dto){
        log.info("닉네임 업데이트 시작 - 이메일: {}, 닉네임: {}", email, dto.getNickname());

        // 이메일로 회원 정보 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("해당 이메일의 사용자가 존재하지 않습니다."));

        // 닉네임 업데이트
        member.updateNickname(dto.getNickname());
        memberRepository.save(member);
    }
}
