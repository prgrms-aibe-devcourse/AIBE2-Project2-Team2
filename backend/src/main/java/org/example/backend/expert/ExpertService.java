package org.example.backend.expert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.constant.Role;
import org.example.backend.entity.*;
import org.example.backend.expert.dto.ExpertRequestDto;
import org.example.backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ExpertService {

    private final MemberRepository memberRepository;
    private final ExpertProfileRepository expertProfileRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DetailFieldRepository detailFieldRepository;
    private final ExpertProfileSpecialtyDetailRepository expertProfileSpecialtyDetailRepository;
    private final SkillRepository skillRepository;
    private final CareerRepository careerRepository;

    // 전문가로 전환하는 메소드 - 포트폴리오는 제외하고 나머지 정보들 등록
    public void upgradeToExpert(String email, ExpertRequestDto dto) {
        // 1. 이메일로 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자가 존재하지 않습니다."));

        // 2. 전문가 권한이 이미 있는지 확인
        if (member.getRole().name().equals("EXPERT")) {
            log.info("이미 전문가로 등록된 사용자입니다: {}", email);
            throw new IllegalArgumentException("이미 전문가로 등록된 사용자입니다.");
        }

        // 3. 전문가로 전환
        member.changeRole(Role.EXPERT);
        memberRepository.save(member);

        // 4. ExpertProfile 조회 또는 새로 생성
        ExpertProfile profile = expertProfileRepository.findByMember(member)
                .orElseGet(() -> ExpertProfile.createExpertProfile(
                        member,
                        "", "", 0, "", 0,
                        null, null, null, null
                ));

        // 5. 프로필 정보 업데이트
        profile.updateProfileInfo(
                dto.getIntroduction(),
                dto.getRegion(),
                dto.getTotalCareerYears(),
                dto.getEducation(),
                dto.getEmployeeCount(),
                dto.getWebsiteUrl(),
                dto.getFacebookUrl(),
                dto.getXUrl(),
                dto.getInstagramUrl()
        );
        expertProfileRepository.save(profile);

        // 6. 전문 분야 및 상세 분야 저장
        saveSpecialtyDetails(profile, dto.getSpecialties(), dto.getDetailFields());

        // 7. 기술 스킬 저장
        saveSkills(profile, dto.getSkills());

        // 8. 경력 저장
        saveCareers(profile, dto.getCareers());

        log.info("사용자 {}가 전문가로 성공적으로 전환되었습니다.", email);
    }

    private void saveSpecialtyDetails(ExpertProfile profile, List<String> specialties, List<String> detailFields) {
        if (specialties.size() != detailFields.size()) {
            throw new IllegalArgumentException("전문 분야와 상세 분야 목록 크기가 일치하지 않습니다.");
        }
        for (int i = 0; i < specialties.size(); i++) {
            final int index = i;  // final 혹은 effectively final 변수로 복사
            Specialty specialty = specialtyRepository.findByName(specialties.get(index))
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전문 분야: " + specialties.get(index)));

            DetailField detailField = detailFieldRepository.findByName(detailFields.get(index))
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상세 분야: " + detailFields.get(index)));

            ExpertProfileSpecialtyDetail detail = new ExpertProfileSpecialtyDetail(profile, specialty, detailField);
            expertProfileSpecialtyDetailRepository.save(detail);
        }
    }

    private void saveSkills(ExpertProfile profile, List<String> skillNames) {
        if (skillNames == null) return;

        for (String skillName : skillNames) {
            Skill skill = skillRepository.findByName(skillName)
                    .orElseGet(() -> skillRepository.save(new Skill(skillName)));
            profile.getSkills().add(skill);
        }

        expertProfileRepository.save(profile);
    }

    private void saveCareers(ExpertProfile profile, List<String> careers) {
        if (careers == null) return;

        for (String careerDesc : careers) {
            Career career = new Career(careerDesc, profile);
            careerRepository.save(career);
        }
    }
}
