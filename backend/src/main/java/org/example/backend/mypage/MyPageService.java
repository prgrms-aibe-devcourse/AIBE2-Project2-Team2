package org.example.backend.mypage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.constant.PaymentStatus;
import org.example.backend.entity.Content;
import org.example.backend.entity.EstimateRecord;
import org.example.backend.entity.Member;
import org.example.backend.entity.Payment;
import org.example.backend.exception.customException.EstimateRecordNotFoundException;
import org.example.backend.exception.customException.MemberNotFoundException;
import org.example.backend.exception.customException.PaymentNotFoundException;
import org.example.backend.firebase.FirebaseImageService;
import org.example.backend.mypage.dto.request.NicknameUpdateRequestDto;
import org.example.backend.mypage.dto.response.MyPageResponseDto;
import org.example.backend.mypage.dto.response.PaymentResponseDto;
import org.example.backend.repository.EstimateRecordRepository;
import org.example.backend.repository.MemberRepository;
import org.example.backend.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MyPageService {
    private final MemberRepository memberRepository;
    private final FirebaseImageService firebaseImageService;
    private final PaymentRepository paymentRepository;
    private final EstimateRecordRepository estimateRecordRepository;
    private static final String DEFAULT_PROFILE_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/team2maldive.firebasestorage.app/o/default-profile.png?alt=media";

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

    public void updateProfileImage(String email, MultipartFile file) {
        log.info("프로필 이미지 업데이트 시작 - 이메일: {}", email);

        // 파일 유효성 검사
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }

        // 이메일로 회원 정보 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("해당 이메일의 사용자가 존재하지 않습니다."));

        // 이전 프로필이 디폴트 이미지인지 체크
        String currentProfileUrl = member.getProfileImageUrl();
        if (currentProfileUrl != null && !currentProfileUrl.equals(DEFAULT_PROFILE_IMAGE_URL)) {
            // 디폴트 아미지가 아닌 다른 이미지가 있다면 파이어베이스에서 이미지 삭제
            firebaseImageService.deleteImage(currentProfileUrl);
            log.info("기존 프로필 이미지 삭제 - URL: {}", currentProfileUrl);
        }

        // 프로필 이미지 업로드
        String fileName = "profile/" + member.getNickname() + "_profile_image";
        String imageUrl = firebaseImageService.uploadImage(file, fileName);

        // 프로필 이미지 URL 업데이트
        log.info("프로필 이미지 URL 업데이트 - 이메일: {}, URL: {}", email, imageUrl);
        member.updateProfileImageUrl(imageUrl);
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getUserPayments(String email, PaymentStatus filterStatus) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("해당 유저가 존재하지 않습니다."));

        List<Payment> payments = paymentRepository.findAllByMatching_Member(member);

        return payments.stream()
                .filter(p -> filterStatus == null || p.getStatus() == filterStatus)
                .sorted(Comparator.comparing(Payment::getUpdateTime).reversed())
                .map(p -> {
                    Content content = p.getMatching().getContent();
                    Member expert = content.getMember();

                    return new PaymentResponseDto(
                            p.getPaymentId(),
                            p.getCost(),
                            p.getRegTime(),
                            p.getStatus().name(),
                            content.getContentId(),
                            content.getTitle(),
                            expert.getNickname(),
                            expert.getProfileImageUrl()
                    );
                })
                .collect(Collectors.toList());
    }
}
