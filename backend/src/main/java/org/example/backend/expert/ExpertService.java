package org.example.backend.expert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.constant.Role;
import org.example.backend.entity.*;
import org.example.backend.exception.customException.*;
import org.example.backend.expert.dto.request.ExpertRequestDto;
import org.example.backend.expert.dto.request.SkillDto;
import org.example.backend.expert.dto.request.SpecialtyDetailRequestDto;
import org.example.backend.expert.dto.response.DetailFieldDto;
import org.example.backend.expert.dto.response.ExpertProfileDto;
import org.example.backend.expert.dto.response.ExpertSignupMetaDto;
import org.example.backend.expert.dto.response.SkillCategoryDto;
import org.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExpertService {

    private final MemberRepository memberRepository;
    private final ExpertProfileRepository expertProfileRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DetailFieldRepository detailFieldRepository;
    private final ExpertProfileSpecialtyDetailRepository expertProfileSpecialtyDetailRepository;
    private final SkillRepository skillRepository;
    private final CareerRepository careerRepository;
    private final SkillCategoryRepository skillCategoryRepository;

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
        expertProfileRepository.save(profile); // 프로필 저장

        // 5. 전문 분야 및 상세 분야 저장
        saveSpecialtyDetails(profile, dto.getSpecialties());

        // 6. 기술 스킬 저장
        saveSkills(profile, dto.getSkills());

        // 7. 경력 저장
        saveCareers(profile, dto.getCareers());

        // 변경된 내용 최종 저장
        expertProfileRepository.save(profile);

        log.info("사용자 {}가 전문가로 성공적으로 전환되었습니다.", email);
    }

    private void saveSpecialtyDetails(ExpertProfile profile, List<SpecialtyDetailRequestDto> specialtyDetailDtos) {
        for (SpecialtyDetailRequestDto dto : specialtyDetailDtos) {
            Specialty specialty = specialtyRepository.findByName(dto.getSpecialty())
                    .orElseThrow(() -> new SpecialtyNotFoundException("존재하지 않는 전문 분야: " + dto.getSpecialty()));

            for (String detailName : dto.getDetailFields()) {
                DetailField detailField = detailFieldRepository.findByName(detailName)
                        .orElseThrow(() -> new DetailFieldNotFoundException("존재하지 않는 상세 분야: " + detailName));

                ExpertProfileSpecialtyDetail detail = new ExpertProfileSpecialtyDetail(profile, specialty, detailField);
                expertProfileSpecialtyDetailRepository.save(detail);

                profile.getSpecialtyDetailFields().add(detail);
            }
        }
    }

    private void saveSkills(ExpertProfile profile, List<SkillDto> skillDtos) {
        if (skillDtos == null) return;

        for (SkillDto skillDto : skillDtos) {
            SkillCategory category = skillCategoryRepository.findByName(skillDto.getCategory())
                    .orElseThrow(() -> new SkillCategoryNotFoundException("존재하지 않는 기술 카테고리: " + skillDto.getCategory()));

            Skill skill = skillRepository.findByNameAndCategory(skillDto.getName(), category)
                    .orElseThrow(() -> new SkillNotFoundException("존재하지 않는 기술 스킬: " + skillDto.getName() + " in category " + skillDto.getCategory()));

            profile.getSkills().add(skill);
        }
        expertProfileRepository.save(profile);
    }

    private void saveCareers(ExpertProfile profile, List<String> careers) {
        if (careers == null) return;

        for (String careerDesc : careers) {
            Career career = new Career(careerDesc, profile);
            careerRepository.save(career);
            profile.getCareers().add(career); // 연관관계 관리
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

    // 전문가 가입 메타 정보 조회
    public ExpertSignupMetaDto getExpertSignupMeta() {
        // 1. Specialty + DetailFields 조회 및 DTO 변환
        List<DetailFieldDto> detailFields = specialtyRepository.findAll()
                .stream()
                .map(specialty -> new DetailFieldDto(
                        specialty.getName(),
                        specialty.getDetailFields()
                                .stream()
                                .map(DetailField::getName)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        // 2. SkillCategory + Skill 조회 및 DTO 변환
        List<SkillCategoryDto> skills = skillCategoryRepository.findAll()
                .stream()
                .map(category -> new SkillCategoryDto(
                        category.getName(),
                        category.getSkills()
                                .stream()
                                .map(Skill::getName)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        // 3. 지역 리스트 (임시 하드코딩)
        List<String> regions = List.of("서울", "경기 북부", "경기 남부", "부산",
                "대구","인천", "광주", "대전", "울산", "세종", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주","해외");

        return new ExpertSignupMetaDto(detailFields, skills, regions);
    }

    // 전문가 프로필 조회
    @Transactional(readOnly = true)
    public ExpertProfileDto getExpertProfile(String email) {
        ExpertProfileDto profileDto = expertProfileRepository.findExpertProfileByEmail(email);
        if (profileDto == null) {
            throw new RuntimeException("해당 이메일의 전문가 프로필이 존재하지 않습니다.");  // 필요시 커스텀 예외로 변경 가능
        }
        return profileDto;
    }

    @Transactional
    public void updateExpertProfile(String email, ExpertRequestDto dto) {
        // 1. 이메일로 회원 조회 (전문가 권한이어야 함)
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("해당 이메일의 사용자가 존재하지 않습니다."));

        if (member.getRole() != Role.EXPERT) {
            throw new NotExpertException("전문가가 아닌 사용자는 프로필을 수정할 수 없습니다.");
        }

        // 2. 기존 전문가 프로필 조회
        ExpertProfile profile = expertProfileRepository.findByMember(member)
                .orElseThrow(() -> new ExpertProfileNotFoundException("전문가 프로필이 존재하지 않습니다."));

        // 3. 연관 데이터 초기화
        expertProfileSpecialtyDetailRepository.deleteAllByExpertProfile(profile);
        profile.getSpecialtyDetailFields().clear();

        profile.getSkills().clear();

        careerRepository.deleteAllByExpertProfile(profile);
        profile.getCareers().clear();

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

        // 5. 새롭게 연관 데이터 저장
        saveSpecialtyDetails(profile, dto.getSpecialties());
        saveSkills(profile, dto.getSkills());
        saveCareers(profile, dto.getCareers());

        expertProfileRepository.save(profile);
    }
}

