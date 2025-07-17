package org.example.backend.expert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.constant.Role;
import org.example.backend.entity.*;
import org.example.backend.exception.customException.AlreadyExpertException;
import org.example.backend.exception.customException.MemberNotFoundException;
import org.example.backend.expert.dto.ExpertRequestDto;
import org.example.backend.expert.dto.SpecialtyDetailRequestDto;
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
        // 1. 이메일로 회원 조회 및 권한 확인
        Member member = findMemberByEmailAndCheckNotExpert(email);

        // 2. 전문가로 전환
        member.changeRole(Role.EXPERT);
        memberRepository.save(member);

        // 3. ExpertProfile 조회 또는 새로 생성
        ExpertProfile profile = expertProfileRepository.findByMember(member)
                .orElseGet(() -> ExpertProfile.createExpertProfile(
                        member,
                        "", "", 0, "", 0,
                        null, null, null, null
                ));

        // 4. 프로필 정보 업데이트
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

        // 5. 전문 분야 및 상세 분야 저장
        saveSpecialtyDetails(profile, dto.getSpecialties());

        // 6. 기술 스킬 저장
        saveSkills(profile, dto.getSkills());

        // 7. 경력 저장
        saveCareers(profile, dto.getCareers());

        log.info("사용자 {}가 전문가로 성공적으로 전환되었습니다.", email);
    }

    private void saveSpecialtyDetails(ExpertProfile profile, List<SpecialtyDetailRequestDto> specialtyDetailDtos) {
        for (SpecialtyDetailRequestDto dto : specialtyDetailDtos) {
            Specialty specialty = specialtyRepository.findByName(dto.getSpecialty())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전문 분야: " + dto.getSpecialty()));

            for (String detailName : dto.getDetailFields()) {
                DetailField detailField = detailFieldRepository.findByName(detailName)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상세 분야: " + detailName));

                ExpertProfileSpecialtyDetail detail = new ExpertProfileSpecialtyDetail(profile, specialty, detailField);
                expertProfileSpecialtyDetailRepository.save(detail);
            }
        }
    }

    private void saveSkills(ExpertProfile profile, List<String> skillNames) {
        if (skillNames == null) return;

        for (String skillName : skillNames) {
            Skill skill = skillRepository.findByName(skillName)
                    .orElseGet(() -> skillRepository.save(new Skill(skillName)));
            profile.getSkills().add(skill);
        }
    }

    private void saveCareers(ExpertProfile profile, List<String> careers) {
        if (careers == null) return;

        for (String careerDesc : careers) {
            Career career = new Career(careerDesc, profile);
            careerRepository.save(career);
        }
    }

    // 이메일로 회원 조회 및 권한 확인
    public Member findMemberByEmailAndCheckNotExpert(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("해당 이메일의 사용자가 존재하지 않습니다."));

        if (member.getRole() == Role.EXPERT) {
            log.info("이미 전문가로 등록된 사용자입니다: {}", email);
            throw new AlreadyExpertException("이미 전문가로 등록된 사용자입니다.");
        }

        return member;
    }

}
