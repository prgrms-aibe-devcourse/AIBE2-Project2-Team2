package org.example.backend.expert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.constant.Role;
import org.example.backend.entity.*;
import org.example.backend.exception.customException.*;
import org.example.backend.expert.dto.request.ExpertRequestDto;
import org.example.backend.expert.dto.request.SkillDto;
import org.example.backend.expert.dto.request.SpecialtyDetailRequestDto;
import org.example.backend.expert.dto.response.*;
import org.example.backend.firebase.FirebaseImageService;
import org.example.backend.repository.*;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
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
    private final PortfolioRepository portfolioRepository;
    private final PortfolioImageRepository portfolioImageRepository;
    private final FirebaseImageService firebaseImageService;

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

    // 포트폴리오 상세 조회
    @Transactional(readOnly = true)
    public PortfolioDetailResponseDto getPortfolioDetail(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("해당 포트폴리오가 존재하지 않습니다."));

        ExpertProfile expertProfile = portfolio.getExpertProfile();
        Member expertMember = expertProfile.getMember();

        // 전체 이미지 리스트 DTO 변환
        List<PortfolioDetailResponseDto.PortfolioImageDto> imageDtos = portfolio.getImages().stream()
                .map(img -> new PortfolioDetailResponseDto.PortfolioImageDto(
                        img.getPortfolioImageId(),
                        img.getImageUrl()))
                .collect(Collectors.toList());

        // 썸네일 이미지 찾기
        PortfolioImage thumbnailImage = portfolio.getImages().stream()
                .filter(PortfolioImage::isThumbnailCheck)  // boolean getter 메서드 이름이 isThumbnailCheck여야 함
                .findFirst()
                .orElse(null);

        PortfolioDetailResponseDto.PortfolioImageDto thumbnailDto = null;
        if (thumbnailImage != null) {
            thumbnailDto = new PortfolioDetailResponseDto.PortfolioImageDto(
                    thumbnailImage.getPortfolioImageId(),
                    thumbnailImage.getImageUrl()
            );
        }

        return PortfolioDetailResponseDto.builder()
                .portfolioId(portfolio.getPortfolioId())
                .title(portfolio.getTitle())
                .content(portfolio.getContent())
                .viewCount(portfolio.getViewCount())
                .workingYear(portfolio.getWorkingYear())
                .category(portfolio.getCategory())
                .images(imageDtos)
                .thumbnailImage(thumbnailDto)   // 썸네일 추가
                .reviewCount(expertProfile.getReviewCount())
                .rating(expertProfile.getRating())
                .expertNickname(expertMember.getNickname())
                .expertProfileImageUrl(expertMember.getProfileImageUrl())
                .build();
    }


    @Transactional
    public void createPortfolio(String email, String title, String content, String category,
                                Integer workingYear, List<MultipartFile> images, MultipartFile thumbnailImage) {

        // 0. 이미지 수 검사 (썸네일 제외 일반 이미지 수 검사)
        int totalImages = images == null ? 0 : images.size();
        if (thumbnailImage == null) {
            throw new InvalidPortfolioImageException("썸네일 이미지를 반드시 전송해야 합니다.");
        }
        if (totalImages + 1 > 5) {
            throw new InvalidPortfolioImageException("포트폴리오 이미지는 최대 5개까지 업로드할 수 있습니다.");
        }

        // 1. 전문가 프로필 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("해당 이메일의 사용자가 존재하지 않습니다."));
        if (member.getRole() != Role.EXPERT) {
            throw new NotExpertException("전문가가 아닌 사용자는 포트폴리오를 생성할 수 없습니다.");
        }
        ExpertProfile expertProfile = expertProfileRepository.findByMember(member)
                .orElseThrow(() -> new ExpertProfileNotFoundException("전문가 프로필이 존재하지 않습니다."));

        // 2. Portfolio 엔티티 생성 및 저장
        Portfolio portfolio = new Portfolio(expertProfile, title, content, workingYear, category);
        portfolioRepository.save(portfolio);

        // 3. 썸네일 이미지 업로드 및 저장
        String thumbnailFileName = "portfolio/" + member.getNickname() + "_portfolio_thumbnail_" + portfolio.getPortfolioId();
        String thumbnailUrl = firebaseImageService.uploadImage(thumbnailImage, thumbnailFileName);
        PortfolioImage thumbnailPortfolioImage = new PortfolioImage(portfolio, thumbnailUrl, true);
        portfolio.getImages().add(thumbnailPortfolioImage);

        // 4. 일반 이미지 업로드 및 저장 (썸네일 제외)
        if (images != null) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);
                String fileName = "portfolio/" + member.getNickname() + "_portfolio_image_" + portfolio.getPortfolioId() + "_" + i;
                String imageUrl = firebaseImageService.uploadImage(image, fileName);
                PortfolioImage portfolioImage = new PortfolioImage(portfolio, imageUrl, false);
                portfolio.getImages().add(portfolioImage);
            }
        }
    }

    @Transactional
    public void updatePortfolio(
            String email,
            Long portfolioId,
            String title,
            String content,
            String category,
            Integer workingYear,
            List<Long> remainingImageIds,
            List<MultipartFile> newImages,
            MultipartFile thumbnailImage,
            Long thumbnailRemainImageId
    ) {
        // 1. 포트폴리오 조회 및 권한 체크
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("해당 포트폴리오가 존재하지 않습니다."));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("해당 이메일의 사용자가 존재하지 않습니다."));

        if (!portfolio.getExpertProfile().getMember().equals(member)) {
            throw new MemberNotFoundException("해당 포트폴리오의 작성자가 아닙니다.");
        }

        if (member.getRole() != Role.EXPERT) {
            throw new NotExpertException("전문가 권한이 없습니다.");
        }

        // 2. 포트폴리오 기본 정보 수정
        portfolio.setTitle(title);
        portfolio.setContent(content);
        portfolio.setCategory(category);
        portfolio.setWorkingYear(workingYear);

        // 3. 기존 이미지 중 남길 이미지 필터링
        List<PortfolioImage> existingImages = portfolioImageRepository.findByPortfolio(portfolio);
        List<PortfolioImage> imagesToKeep = new ArrayList<>();

        for (PortfolioImage img : existingImages) {
            if (remainingImageIds.contains(img.getPortfolioImageId())) {
                imagesToKeep.add(img);
            } else {
                // 기존 이미지 삭제 처리 (DB 및 스토리지)
                firebaseImageService.deleteImage(img.getImageUrl());
                portfolioImageRepository.delete(img);
            }
        }

        // 4. 새 이미지 업로드 및 추가
        if (newImages != null) {
            for (MultipartFile newImage : newImages) {
                String fileName = "portfolio/" + member.getNickname() + "_portfolio_image_" + portfolio.getPortfolioId() + "_" + System.currentTimeMillis();
                String imageUrl = firebaseImageService.uploadImage(newImage, fileName);

                PortfolioImage newPortfolioImage = new PortfolioImage(portfolio, imageUrl, false);
                portfolio.getImages().add(newPortfolioImage);
                imagesToKeep.add(newPortfolioImage);
            }
        }

        // 5. 썸네일 처리
        // 5-1) 새 썸네일 이미지가 있다면 새로 추가 + 기존 썸네일 false 처리
        if (thumbnailImage != null) {
            imagesToKeep.forEach(img -> img.setThumbnailCheck(false));

            String thumbnailFileName = "portfolio/" + member.getNickname() + "_portfolio_thumbnail_" + portfolio.getPortfolioId() + "_" + System.currentTimeMillis();
            String thumbnailUrl = firebaseImageService.uploadImage(thumbnailImage, thumbnailFileName);

            PortfolioImage newThumbnailImage = new PortfolioImage(portfolio, thumbnailUrl, true);
            portfolio.getImages().add(newThumbnailImage);

        } else if (thumbnailRemainImageId != null) {
            // 5-2) 기존 이미지 중 명시한 이미지 썸네일 지정
            boolean found = false;
            for (PortfolioImage img : imagesToKeep) {
                if (img.getPortfolioImageId().equals(thumbnailRemainImageId)) {
                    imagesToKeep.forEach(i -> i.setThumbnailCheck(false)); // 초기화
                    img.setThumbnailCheck(true);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new InvalidThumbnailIndexException("썸네일로 지정한 기존 이미지가 존재하지 않습니다.");
            }

        } else {
            // 3) 기존 썸네일 이미지가 남아있으면 유지, 없으면 첫 번째 이미지 썸네일 지정
            boolean hasThumbnail = imagesToKeep.stream()
                    .anyMatch(PortfolioImage::isThumbnailCheck);

            if (!hasThumbnail && !imagesToKeep.isEmpty()) {
                imagesToKeep.get(0).setThumbnailCheck(true);
            }
        }

        // 6. 변경 내용 저장
        portfolioRepository.save(portfolio);
    }
}

