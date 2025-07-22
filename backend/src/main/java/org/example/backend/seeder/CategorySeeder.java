package org.example.backend.seeder;

import org.example.backend.entity.Category;
import org.example.backend.entity.SubCategory;
import org.example.backend.entity.SubSubCategory;
import org.example.backend.repository.CategoryRepository;
import org.example.backend.repository.SubCategoryRepository;
import org.example.backend.repository.SubSubCategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import java.util.List;


@Component
public class CategorySeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() > 0) {
            return; // 이미 데이터가 있으면 시드하지 않음
        }

        // 1차 카테고리: 디자인
        Category design = categoryRepository.save(new Category("디자인", null));

        // 2차, 3차 카테고리
        saveSubCategoryWithItems(design, "로고·브랜딩", Arrays.asList("로고 디자인", "브랜드 디자인·가이드"));
        saveSubCategoryWithItems(design, "인쇄·홍보물", Arrays.asList("명함", "전단지·포스터·인쇄물", "현수막·X 배너", "메뉴판", "홍보물 인쇄·출력", "스티커·봉투·초대장"));
        saveSubCategoryWithItems(design, "패키지·커버", Arrays.asList("패키지", "책표지·내지", "전자책 표지·내지", "앨범커버"));
        saveSubCategoryWithItems(design, "웹·모바일 디자인", Arrays.asList("웹 UI·UX", "앱·모바일 UI·UX", "템플릿형 홈페이지", "아이콘·버튼"));
        saveSubCategoryWithItems(design, "마케팅 디자인", Arrays.asList("상세페이지", "SNS·광고소재·썸네일", "채널아트 디자인", "방송용 아바타", "배너·배달어플", "블로그·카페 디자인"));
        saveSubCategoryWithItems(design, "그래픽 디자인", Arrays.asList("PPT·인포그래픽", "포토샵·파일변환"));
        saveSubCategoryWithItems(design, "캐릭터·일러스트", Arrays.asList("일러스트", "캐리커쳐", "웹툰·콘티", "2D 캐릭터", "이모티콘"));
        saveSubCategoryWithItems(design, "3D 디자인", Arrays.asList("3D 캐릭터·피규어", "3D 제품모델링·렌더링", "3D 공간 모델링", "3D 의류·쥬얼리", "3D 게임용 소스", "3D 그래픽", "시제품·3D프린팅"));
        saveSubCategoryWithItems(design, "산업·제품 디자인", Arrays.asList("제품·기구 설계", "제품 도면·스케치"));
        saveSubCategoryWithItems(design, "공간·건축", Arrays.asList("도면 제작·수정", "인테리어 컨설팅", "전시·무대 디자인", "간판·시공"));
        saveSubCategoryWithItems(design, "AI 디자인", Arrays.asList("AI 디자인"));
        saveSubCategoryWithItems(design, "게임·웹3.0", Arrays.asList("VR·AR·게임", "메타버스", "NFT아트"));
        saveSubCategoryWithItems(design, "캘리그라피·폰트", Arrays.asList("캘리그라피", "타이포그래피", "폰트", "사인·직인"));
        saveSubCategoryWithItems(design, "패션·텍스타일", Arrays.asList("의류·쥬얼리 디자인", "작업지시서·도식화", "패턴·샘플제작"));
        saveSubCategoryWithItems(design, "기타", Arrays.asList("디자이너 구독제", "디자인 템플릿", "AI 프롬프트", "바코드·QR코드", "기타 디자인"));

        // 1차 카테고리: IT 프로그래밍
        Category itProgramming = categoryRepository.save(new Category("IT 프로그래밍", null));

        saveSubCategoryWithItems(itProgramming, "웹빌더", Arrays.asList("워드프레스", "카페24", "아임웹", "노션"));
        saveSubCategoryWithItems(itProgramming, "웹 제작", Arrays.asList("홈페이지 신규 제작", "쇼핑몰 신규 제작", "랜딩페이지"));
        saveSubCategoryWithItems(itProgramming, "웹 유지보수", Arrays.asList("홈페이지 수정·유지보수", "쇼핑몰 수정·유지보수", "퍼블리싱", "검색최적화·SEO", "애널리틱스"));
        saveSubCategoryWithItems(itProgramming, "프로그램", Arrays.asList("완성형 프로그램 스토어", "수익 자동화", "업무 자동화", "크롤링·스크래핑", "일반 프로그램", "프로그램 수정·유지보수", "서버·클라우드", "엑셀·스프레드시트", "봇·챗봇"));
        saveSubCategoryWithItems(itProgramming, "모바일", Arrays.asList("앱", "앱 패키징", "앱 수정·유지보수"));
        saveSubCategoryWithItems(itProgramming, "AI", Arrays.asList("AI 시스템·서비스", "맞춤형 챗봇·GPT", "AI 자동화 프로그램", "프롬프트 설계(엔지니어링)", "AI 모델링·최적화", "이미지·음성 인식", "AI 기능 개발·연동", "AI 에이전트", "AI 데이터 분석", "AI 도입 컨설팅", "자연어 처리"));
        saveSubCategoryWithItems(itProgramming, "데이터", Arrays.asList("데이터 구매·구축", "데이터 라벨링", "데이터 전처리·분석·시각화", "데이터베이스"));
        saveSubCategoryWithItems(itProgramming, "보안·품질관리", Arrays.asList("정보 보안", "QA·테스트"));
        saveSubCategoryWithItems(itProgramming, "트렌드", Arrays.asList("게임·AR·VR", "메타버스", "블록체인·NFT"));
        saveSubCategoryWithItems(itProgramming, "직무직군", Arrays.asList("UI·UX 기획", "프론트엔드", "백엔드", "풀스택", "데이터·ML·DL", "데브옵스·인프라"));
        saveSubCategoryWithItems(itProgramming, "기타", Arrays.asList("서비스·MVP 개발", "컴퓨터 기술지원", "하드웨어·임베디드", "파일변환", "기타 프로그래밍"));

        // 1차 카테고리: 영상 사진 음향
        Category media = categoryRepository.save(new Category("영상 사진 음향", null));

        saveSubCategoryWithItems(media, "영상", Arrays.asList("광고·홍보 영상", "숏폼 영상", "업종별 영상", "제품 영상", "교육 영상", "행사 영상", "유튜브 영상", "온라인 중계", "드론 촬영", "영상 후반작업", "현장 스탭", "영상 기타"));
        saveSubCategoryWithItems(media, "컴퓨터 그래픽(CG)", Arrays.asList("모션그래픽", "인포그래픽", "미디어 아트", "인트로·로고", "타이포그래피", "3D 모델링", "AR·VR·XR"));
        saveSubCategoryWithItems(media, "애니메이션", Arrays.asList("2D 애니메이션", "3D 애니메이션", "화이트보드 애니메이션", "로티·web 애니메이션"));
        saveSubCategoryWithItems(media, "AI 콘텐츠", Arrays.asList("AI 영상", "AI 이미지", "AI 음향"));
        saveSubCategoryWithItems(media, "사진", Arrays.asList("제품·홍보 사진", "개인·프로필 사진", "이벤트 스냅", "사진 보정"));
        saveSubCategoryWithItems(media, "음향", Arrays.asList("성우", "음악·음원", "오디오 콘텐츠", "오디오 엔지니어링", "기타 음향·음악"));
        saveSubCategoryWithItems(media, "엔터테이너", Arrays.asList("모델", "배우", "쇼호스트", "MC", "공연"));
        saveSubCategoryWithItems(media, "기타", Arrays.asList("콘티·스토리보드", "헤어메이크업", "스튜디오 렌탈", "기타 영상·사진·음향"));

        // 1차 카테고리: 마케팅
        Category marketing = categoryRepository.save(new Category("마케팅", null));

        saveSubCategoryWithItems(marketing, "채널 활성화", Arrays.asList("블로그 관리", "카페 관리", "인스타그램 관리", "유튜브 관리", "릴스·쇼츠·틱톡 관리", "기타 채널 관리"));
        saveSubCategoryWithItems(marketing, "바이럴·협찬", Arrays.asList("인플루언서 마케팅", "체험단 모집", "바이럴·포스팅"));
        saveSubCategoryWithItems(marketing, "지도 마케팅", Arrays.asList("지도 세팅", "지도 활성화", "지도 최적화노출", "클립 마케팅"));
        saveSubCategoryWithItems(marketing, "업종·목적별", Arrays.asList("스토어 마케팅", "언론홍보", "앱마케팅", "포털질문·답변", "라이브커머스", "업종별 마케팅 패키지", "종합광고대행", "DB 마케팅", "메시지 마케팅", "스레드 마케팅", "링크드인 마케팅"));
        saveSubCategoryWithItems(marketing, "SEO 최적화 노출", Arrays.asList("테크니컬 SEO", "콘텐츠 SEO", "키워드·경쟁사 분석", "백링크·트래픽", "포털 최적화노출", "인기게시물 관리"));
        saveSubCategoryWithItems(marketing, "해외 마케팅", Arrays.asList("해외 언론홍보", "해외 검색·쇼핑몰", "해외 SNS·바이럴", "기타 해외 마케팅"));
        saveSubCategoryWithItems(marketing, "광고(퍼포먼스)", Arrays.asList("SNS 광고", "키워드·검색 광고", "디스플레이·영상·배너"));
        saveSubCategoryWithItems(marketing, "분석·전략", Arrays.asList("마케팅 컨설팅", "브랜드 컨설팅", "데이터 성과 분석"));
        saveSubCategoryWithItems(marketing, "AI 마케팅", Arrays.asList("AI 마케팅"));
        saveSubCategoryWithItems(marketing, "기타 마케팅", Arrays.asList("옥외·인쇄·방송 광고", "커뮤니티·사이트 배너", "영상 광고", "마케팅 자료·키워드", "행사·이벤트", "기타 마케팅"));

        // 1차 카테고리: 문서 글쓰기
        Category writing = categoryRepository.save(new Category("문서 글쓰기", null));

        saveSubCategoryWithItems(writing, "콘텐츠 글쓰기", Arrays.asList("블로그·카페 원고", "대본 작성", "보도자료·기사·칼럼", "책·전자책 출판", "산업별 전문 글작성"));
        saveSubCategoryWithItems(writing, "비즈니스 카피", Arrays.asList("네이밍·브랜딩", "제품 카피라이팅", "광고 카피라이팅", "기타 카피라이팅"));
        saveSubCategoryWithItems(writing, "논문·자료 조사", Arrays.asList("논문 컨설팅", "논문 교정·편집", "논문 통계분석", "자료 조사"));
        saveSubCategoryWithItems(writing, "타이핑·편집", Arrays.asList("타이핑(문서)", "타이핑(영상)", "문서 편집"));
        saveSubCategoryWithItems(writing, "교정·첨삭", Arrays.asList("교정·교열 첨삭"));
        saveSubCategoryWithItems(writing, "AI 글쓰기", Arrays.asList("AI 콘텐츠 생산", "AI 콘텐츠 검수·편집"));
        saveSubCategoryWithItems(writing, "기타", Arrays.asList("기타 글쓰기", "문서 자료"));

        // 1차 카테고리: 창업 사업
        Category startup = categoryRepository.save(new Category("창업 사업", null));

        saveSubCategoryWithItems(startup, "사업계획", Arrays.asList("사업계획서·투자제안서", "리서치"));
        saveSubCategoryWithItems(startup, "스타트업 자문", Arrays.asList("비전·미션·초기 브랜딩", "개인·조직 목표 관리", "스타트업 인사 자문", "스타트업 투자 유치"));
        saveSubCategoryWithItems(startup, "기업 자문", Arrays.asList("일반 경영 자문", "브랜딩", "영업 전략·관리", "물류·생산", "HR·기업문화", "해외 사업·해외 진출", "IT 컨설팅", "재무 자문", "운영 지원"));
        saveSubCategoryWithItems(startup, "업종별 창업", Arrays.asList("온라인 쇼핑몰 창업", "카페·요식업", "병원·약국", "프랜차이즈", "무인점포·공간대여", "반려동물", "기타 창업", "패션·미용·뷰티케어", "건강기능식품", "화장품"));
        saveSubCategoryWithItems(startup, "AI 자문", Arrays.asList("AI 창업 자문", "AI 경영·운영 자문"));
        saveSubCategoryWithItems(startup, "자료∙콘텐츠", Arrays.asList("비즈니스 문서"));
        saveSubCategoryWithItems(startup, "기타", Arrays.asList("기타 자문·지원"));

        // 1차 카테고리: 세무 법무 노무
        Category lawTaxLabor = categoryRepository.save(new Category("세무 법무 노무", null));

        saveSubCategoryWithItems(lawTaxLabor, "법무", Arrays.asList("사업자 법률 자문", "개인 법률 자문", "법무·행정"));
        saveSubCategoryWithItems(lawTaxLabor, "세무·회계", Arrays.asList("사업자 세무·회계", "개인 세무·회계"));
        saveSubCategoryWithItems(lawTaxLabor, "지식재산권 보호", Arrays.asList("국내 특허·상표", "기타 지식재산권"));
        saveSubCategoryWithItems(lawTaxLabor, "노무", Arrays.asList("고용인 노무 상담", "근로자 노무 상담", "근로계약서 상담", "고용지원금 상담"));
        saveSubCategoryWithItems(lawTaxLabor, "기타", Arrays.asList("기타 자문(관세사 등)"));

        // 1차 카테고리: 전자책
        Category ebook = categoryRepository.save(new Category("전자책", null));

        saveSubCategoryWithItems(ebook, "투자·재테크 전자책", Arrays.asList());
        saveSubCategoryWithItems(ebook, "부업·수익화 전자책", Arrays.asList());
        saveSubCategoryWithItems(ebook, "창업 전자책", Arrays.asList());
        saveSubCategoryWithItems(ebook, "취업·시험·자격증 전자책", Arrays.asList());
        saveSubCategoryWithItems(ebook, "직무역량 전자책", Arrays.asList());
        saveSubCategoryWithItems(ebook, "학습·교육 전자책", Arrays.asList());
        saveSubCategoryWithItems(ebook, "라이프 전자책", Arrays.asList());

        // 1차 카테고리: 번역 통역
        Category translation = categoryRepository.save(new Category("번역 통역", null));

        saveSubCategoryWithItems(translation, "번역", Arrays.asList("영어 번역", "중국어 번역", "일본어 번역", "기타 언어 번역", "감수", "번역공증대행", "AI 번역 검수·편집"));
        saveSubCategoryWithItems(translation, "통역", Arrays.asList("영어 통역", "중국어 통역", "일본어 통역", "기타 언어 통역", "AI 통역"));

        // 1차 카테고리: 주문제작
        Category customOrder = categoryRepository.save(new Category("주문제작", null));

        saveSubCategoryWithItems(customOrder, "인쇄·판촉물", Arrays.asList("인쇄", "3D프린팅", "패키지 제작", "가게용품 제작", "기념품 제작", "모형 제작", "제품 제작"));
        saveSubCategoryWithItems(customOrder, "기타", Arrays.asList("기타 주문제작"));

        // 1차 카테고리: 취업 입시
        Category employment = categoryRepository.save(new Category("취업 입시", null));

        saveSubCategoryWithItems(employment, "취업·이직", Arrays.asList("국내 자소서·이력서", "외국계 자소서·이력서", "인적성·NCS필기", "면접·커리어 컨설팅"));
        saveSubCategoryWithItems(employment, "전자책·자료", Arrays.asList("취업·시험·자격증 전자책", "취업·입시 자료", "학습·교육 전자책", "교육 자료"));
        saveSubCategoryWithItems(employment, "입시·자격증", Arrays.asList("자격증", "대학 입시", "편입·검정고시", "대학원·유학"));
        saveSubCategoryWithItems(employment, "AI 컨설팅", Arrays.asList("AI 취업·입시 컨설팅"));

        // 1차 카테고리: 투잡 노하우
        Category sideJob = categoryRepository.save(new Category("투잡 노하우", null));

        saveSubCategoryWithItems(sideJob, "전자책·자료", Arrays.asList("투자·재테크 전자책", "부업·수익화 전자책", "창업 전자책", "투잡·재테크 자료"));
        saveSubCategoryWithItems(sideJob, "교육·강의", Arrays.asList("N잡 스쿨", "투잡·재테크 교육"));

        // 1차 카테고리: 직무역량 레슨
        Category jobLesson = categoryRepository.save(new Category("직무역량 레슨", null));

        saveSubCategoryWithItems(jobLesson, "전자책", Arrays.asList("직무역량 전자책"));
        saveSubCategoryWithItems(jobLesson, "데이터·개발", Arrays.asList("프로그래밍 레슨", "데이터분석 레슨"));
        saveSubCategoryWithItems(jobLesson, "실무·자기개발", Arrays.asList("마케팅 레슨", "PPT·프레젠테이션 레슨"));
        saveSubCategoryWithItems(jobLesson, "외국어", Arrays.asList("영어 레슨", "기타 외국어 레슨"));
        saveSubCategoryWithItems(jobLesson, "디자인·영상", Arrays.asList("그래픽디자인 레슨", "사진 레슨", "영상·유튜브 레슨"));
        saveSubCategoryWithItems(jobLesson, "AI 레슨", Arrays.asList("AI 레슨"));
        saveSubCategoryWithItems(jobLesson, "기타", Arrays.asList("기타 직무역량 레슨"));

        // 1차 카테고리: 운세
        Category fortune = categoryRepository.save(new Category("운세", null));

        saveSubCategoryWithItems(fortune, "분야별 운세", Arrays.asList("신점", "사주", "타로"));
        saveSubCategoryWithItems(fortune, "주제별 운세", Arrays.asList("작명", "연애·애정·궁합", "오늘의 운세", "재물운", "길일", "진로·적성", "풍수지리", "관상·손금"));
        saveSubCategoryWithItems(fortune, "기타", Arrays.asList("기타"));

        // 1차 카테고리: 심리상담
        Category counseling = categoryRepository.save(new Category("심리상담", null));

        saveSubCategoryWithItems(counseling, "심리 검사·상담", Arrays.asList("심리 검사", "심리 상담"));
        saveSubCategoryWithItems(counseling, "연애·고민 상담", Arrays.asList("연애 상담", "고민 상담"));
        saveSubCategoryWithItems(counseling, "코칭", Arrays.asList("코칭", "명상·성공마인드"));
        saveSubCategoryWithItems(counseling, "심리 치료", Arrays.asList("심리 치료"));

        // 1차 카테고리: 생활서비스
        Category lifeService = categoryRepository.save(new Category("생활서비스", null));

        saveSubCategoryWithItems(lifeService, "공간", Arrays.asList("청소·수리·설치", "인테리어", "공간대여"));
        saveSubCategoryWithItems(lifeService, "웨딩", Arrays.asList("웨딩 디렉터", "스튜디오·스냅·보정", "웨딩 사진·영상", "사회", "축가·댄스·반주·음향", "청첩장·식권·현수막", "꽃 장식·부케", "스드메 패키지", "한복·예복·웨딩밴드", "혼인서약서·사회대본", "웨딩 기타"));
        saveSubCategoryWithItems(lifeService, "기타", Arrays.asList("반려동물", "기타"));
    }

    private void saveSubCategoryWithItems(Category parent, String subCategoryName, java.util.List<String> subSubCategoryNames) {
        Category subCategory = categoryRepository.save(new Category(subCategoryName, parent));
        for (String name : subSubCategoryNames) {
            categoryRepository.save(new Category(name, subCategory));
        }
    }
}