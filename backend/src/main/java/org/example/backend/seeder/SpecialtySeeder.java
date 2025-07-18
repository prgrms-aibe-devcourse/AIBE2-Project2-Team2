package org.example.backend.seeder;

import org.example.backend.entity.DetailField;
import org.example.backend.entity.Specialty;
import org.example.backend.repository.DetailFieldRepository;
import org.example.backend.repository.SpecialtyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SpecialtySeeder implements CommandLineRunner {

    private final SpecialtyRepository specialtyRepository;
    private final DetailFieldRepository detailFieldRepository;

    public SpecialtySeeder(SpecialtyRepository specialtyRepository,
                           DetailFieldRepository detailFieldRepository) {
        this.specialtyRepository = specialtyRepository;
        this.detailFieldRepository = detailFieldRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (specialtyRepository.count() > 0) {
            return; // 이미 데이터 있으면 시딩 안 함
        }

        // 1. 디자인
        Specialty design = new Specialty("디자인");
        List<String> designDetails = Arrays.asList(
                "로고디자인",
                "브랜드 디자인/가이드",
                "인쇄/홍보물",
                "패키지 디자인",
                "웹/모바일 디자인",
                "마케팅 디자인",
                "캐릭터/일러스트",
                "ppt/인포그래픽",
                "산업/제품 디자인",
                "포토샵 편집",
                "책 표지/내지",
                "캘리그라피/폰트/사인",
                "공간/건축 디자인",
                "VR/AR/게임",
                "앨범 커버",
                "AI 디자인",
                "메타버스",
                "NFT아트",
                "패션·텍스타일"
        );
        saveDetailFields(design, designDetails);

        // 2. 마케팅
        Specialty marketing = new Specialty("마케팅");
        List<String> marketingDetails = Arrays.asList(
                "채널 활성화",
                "인스타그램 관리",
                "기타 SNS 채널 관리",
                "바이럴/체험단",
                "PR/행사",
                "업종별 마케팅",
                "최적화 노출",
                "앱마케팅",
                "해외 마케팅",
                "광고(퍼포먼스)",
                "마케팅 분석·전략",
                "옥외·인쇄·방송 광고"
        );
        saveDetailFields(marketing, marketingDetails);

        // 3. 번역/통역
        Specialty translation = new Specialty("번역/통역");
        List<String> translationDetails = Arrays.asList(
                "번역",
                "통역",
                "번역공증",
                "감수"
        );
        saveDetailFields(translation, translationDetails);

        // 4. 문서/글쓰기
        Specialty writing = new Specialty("문서/글쓰기");
        List<String> writingDetails = Arrays.asList(
                "카피라이팅",
                "마케팅 글쓰기",
                "콘텐츠 글쓰기",
                "스토리텔링",
                "산업별 전문 글작성",
                "네이밍/브랜딩",
                "타이핑",
                "문서 교정",
                "문서서식/폼"
        );
        saveDetailFields(writing, writingDetails);

        // 5. 영상/사진/음향
        Specialty videoPhotoSound = new Specialty("영상/사진/음향");
        List<String> videoPhotoSoundDetails = Arrays.asList(
                "영상제작",
                "영상편집",
                "사진촬영",
                "사진보정",
                "CG/애니메이션",
                "음악/사운드",
                "더빙/녹음",
                "엔터테이너"
        );
        saveDetailFields(videoPhotoSound, videoPhotoSoundDetails);

        // 6. 창업/사업
        Specialty startup = new Specialty("창업/사업");
        List<String> startupDetails = Arrays.asList(
                "창업 자문",
                "사업계획/리포트",
                "해외 사업",
                "재무 자문",
                "업무지원·운영지원",
                "브랜딩",
                "HR/기업문화",
                "리서치/서베이",
                "물류/생산",
                "쇼핑몰/구매대행",
                "크라우드펀딩",
                "기업 인증"
        );
        saveDetailFields(startup, startupDetails);

        // 7. 운세
        Specialty fortune = new Specialty("운세");
        List<String> fortuneDetails = Arrays.asList(
                "사주",
                "필수",
                "작명/개명/이름풀이",
                "신점",
                "타로",
                "관상/손금",
                "풍수지리"
        );
        saveDetailFields(fortune, fortuneDetails);

        // 8. 직무역량 레슨
        Specialty jobSkills = new Specialty("직무역량 레슨");
        List<String> jobSkillsDetails = Arrays.asList(
                "외국어",
                "컴퓨터활용/프로그래밍",
                "그래픽디자인",
                "데이터사이언스",
                "마케팅",
                "커뮤니케이션",
                "영상촬영/편집",
                "사진촬영/편집"
        );
        saveDetailFields(jobSkills, jobSkillsDetails);

        // 9. IT/프로그래밍
        Specialty it = new Specialty("IT/프로그래밍");
        List<String> itDetails = Arrays.asList(
                "UX 기획",
                "웹사이트 신규 제작",
                "웹사이트 개선/버그수정",
                "모바일앱 신규 제작",
                "모바일앱 개선/버그수정",
                "프로그램 개발",
                "게임/언리얼",
                "임베디드 시스템",
                "데이터 사이언스",
                "보안",
                "QA/테스트"
        );
        saveDetailFields(it, itDetails);

        // 10. 취업/입시
        Specialty jobAdmission = new Specialty("취업/입시");
        List<String> jobAdmissionDetails = Arrays.asList(
                "자소서/이력서 첨삭/코칭",
                "면접/커리어 컨설팅",
                "인적성/NCS필기",
                "자격증 상담",
                "대학 입시 상담",
                "편입/검정고시 상담",
                "대학원/유학 상담"
        );
        saveDetailFields(jobAdmission, jobAdmissionDetails);

        // 11. 투잡/노하우
        Specialty sideJob = new Specialty("투잡/노하우");
        List<String> sideJobDetails = Arrays.asList(
                "투잡/재테크 컨설팅"
        );
        saveDetailFields(sideJob, sideJobDetails);

        // 12. 세무/법무/노무
        Specialty taxLawLabor = new Specialty("세무/법무/노무");
        List<String> taxLawLaborDetails = Arrays.asList(
                "법무",
                "세무",
                "회계",
                "노무",
                "지식재산권",
                "관세"
        );
        saveDetailFields(taxLawLabor, taxLawLaborDetails);

        // 13. 취미 레슨
        Specialty hobbyLesson = new Specialty("취미 레슨");
        List<String> hobbyLessonDetails = Arrays.asList(
                "뷰티/패션 레슨",
                "헬스/PT 레슨",
                "요가/필라테스 레슨",
                "스포츠/액티비티 레슨",
                "골프 레슨",
                "게임 레슨",
                "댄스 레슨",
                "음악/악기 레슨",
                "미술/드로잉 레슨",
                "글쓰기 레슨",
                "공예/DIY 레슨",
                "타로/사주 레슨",
                "연기 레슨"
        );
        saveDetailFields(hobbyLesson, hobbyLessonDetails);

        // 14. 심리상담
        Specialty psychConsult = new Specialty("심리상담");
        List<String> psychConsultDetails = Arrays.asList(
                "심리 상담",
                "심리 검사",
                "심리치료",
                "기업상담(EAP)",
                "강연/워크샵",
                "코칭"
        );
        saveDetailFields(psychConsult, psychConsultDetails);

        // 15. 생활서비스
        Specialty lifeService = new Specialty("생활서비스");
        List<String> lifeServiceDetails = Arrays.asList(
                "인테리어",
                "청소",
                "설치/수리",
                "해충방역",
                "웨딩",
                "사회자/MC",
                "공연/축가/엔터테이너",
                "반려동물 케어",
                "공간 컨설팅/대여"
        );
        saveDetailFields(lifeService, lifeServiceDetails);

        // 저장
        specialtyRepository.saveAll(Arrays.asList(
                design,
                marketing,
                translation,
                writing,
                videoPhotoSound,
                startup,
                fortune,
                jobSkills,
                it,
                jobAdmission,
                sideJob,
                taxLawLabor,
                hobbyLesson,
                psychConsult,
                lifeService
        ));
    }

    private void saveDetailFields(Specialty specialty, List<String> detailNames) {
        for (String name : detailNames) {
            DetailField detail = new DetailField(name, specialty);
            specialty.getDetailFields().add(detail);
        }
    }
}
