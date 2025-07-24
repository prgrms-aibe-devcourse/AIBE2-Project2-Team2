import { useParams, useNavigate, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";

// 대분류와 소분류로 구성된 카테고리 트리
const categoryTree = {
    "디자인": ["로고 디자인", "상세페이지", "배너·포스터", "PPT·인쇄물", "캐릭터·일러스트"],
    "IT·프로그래밍": ["웹 개발", "앱 개발", "크롤링·자동화", "챗봇·AI", "웹사이트 유지보수"],
    "영상·사진·음향": ["영상 편집", "촬영", "유튜브 썸네일", "자막 작업", "음향 편집"],
    "마케팅": ["SNS 광고", "검색광고", "블로그 마케팅", "인스타그램 운영", "리뷰·바이럴"],
    "문서·글쓰기": ["보고서 작성", "이력서·자소서", "기획서·제안서", "카피라이팅", "논문·레포트 대필"],
    "창업·사업": ["사업계획서", "IR 자료", "정부지원사업 신청", "창업 컨설팅", "시장 조사"],
    "세무·법무·노무": ["세무 상담", "계산서·세금신고", "법률 자문", "노무 자문", "계약서 검토"],
    "전자책": ["전자책 제작", "표지 디자인", "출판 컨설팅", "PDF 변환", "eBook 배포"],
    "AI 서비스": ["챗봇 구축", "AI 이미지 생성", "음성 AI", "RPA 자동화", "추천 시스템"],
    "번역·통역": ["영어 번역", "일본어 번역", "중국어 번역", "영상 자막 번역", "현장 통역"],
    "주문제작": ["제품 제작", "커스텀 굿즈", "인쇄물 제작", "패키지 디자인", "소량 제작"],
    "취업·입시": ["자기소개서 첨삭", "면접 코칭", "포트폴리오 첨삭", "입시 컨설팅", "학습 컨설팅"],
    "투잡·노하우": ["스마트스토어", "블로그 수익화", "유튜브 운영법", "마켓 운영", "자동화 수익화"],
    "직무역량 레슨": ["엑셀·파워포인트", "SQL·데이터", "마케팅 전략", "UX/UI 교육", "업무자동화"],
    "운세": ["사주·타로", "운세 상담", "신년운세", "커플 궁합", "작명·개명"],
    "심리상담": ["불안·우울", "연애 상담", "진로 상담", "MBTI 기반", "직장 스트레스"],
    "취미 레슨": ["보컬·노래", "피아노·기타", "드로잉·캘리그라피", "댄스·스트레칭", "공예·향수 만들기"],
    "생활서비스": ["반려동물 상담", "정리 수납", "인테리어 컨설팅", "이사 체크리스트", "일상 꿀팁"]
};

const dummyServices = [
    {
        id: 1,
        title: "1:1 PPT 맞춤 제작",
        description: "깔끔함을 바탕으로 메시지가 돋보이는 디자인",
        price: 11000,
        imageUrl: "https://via.placeholder.com/300x180?text=PPT+디자인",
    },
    {
        id: 2,
        title: "상세페이지 디자인",
        description: "고퀄리티 상세페이지를 빠르게 제작해드립니다.",
        price: 33000,
        imageUrl: "https://via.placeholder.com/300x180?text=상세페이지",
    },
    {
        id: 3,
        title: "배너 제작",
        description: "이벤트/홍보용 배너를 빠르게 만들어드립니다.",
        price: 15000,
        imageUrl: "https://via.placeholder.com/300x180?text=배너",
    },
    {
        id: 4,
        title: "브랜드 가이드 디자인",
        description: "일관된 브랜딩을 위한 디자인 가이드 제작",
        price: 22000,
        imageUrl: "https://via.placeholder.com/300x180?text=브랜드가이드",
    },
];

const CategoryPage = () => {
    const { categoryName } = useParams();
    const { search } = useLocation(); // ✅ 쿼리 파라미터 가져오기
    const navigate = useNavigate();
    const [services, setServices] = useState([]);

    const subCategory = new URLSearchParams(search).get("sub"); // ✅ 현재 선택된 하위 카테고리

    useEffect(() => {
        setServices(dummyServices);
    }, [categoryName, subCategory]); // 💡 서브카테고리 바뀔 때도 리렌더링 가능

    return (
        <div className="flex max-w-7xl mx-auto px-6 pt-24 min-h-screen gap-10 items-start">
            {/* Sidebar */}
            <aside className="w-[240px] hidden lg:block">
                <h3 className="text-lg font-semibold mb-4">카테고리</h3>
                <ul className="space-y-4">
                    {Object.entries(categoryTree).map(([main, subList]) => (
                        <li key={main}>
                            <div
                                onClick={() => navigate(`/category/${main}`)}
                                className={`cursor-pointer px-2 py-1 font-semibold hover:bg-gray-100 rounded-md ${
                                    main === categoryName ? "bg-gray-200 text-teal-700" : ""
                                }`}
                            >
                                {main}
                            </div>
                            <ul className="ml-3 mt-1 space-y-1 text-sm">
                                {subList.map((sub) => (
                                    <li
                                        key={sub}
                                        onClick={() => navigate(`/category/${main}?sub=${sub}`)}
                                        className={`cursor-pointer px-2 py-1 rounded-md hover:text-teal-600 transition ${
                                            sub === subCategory ? "text-black font-medium" : "text-gray-500"
                                        }`}
                                    >
                                        {sub}
                                    </li>
                                ))}
                            </ul>
                        </li>
                    ))}
                </ul>
            </aside>

            {/* Main content */}
            <main className="flex-1">
                <h2 className="text-2xl sm:text2xl font-bold mb-8">
                    {categoryName}
                    {subCategory ? ` > ${subCategory}` : ""}
                </h2>
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-6">
                    {services.map((service) => (
                        <div
                            key={service.id}
                            className="group bg-white border border-gray-200 rounded-xl shadow-sm hover:shadow-lg hover:border-teal-500 transition-all duration-300 p-4 cursor-pointer"
                        >
                            <div className="overflow-hidden rounded-md">
                                <img
                                    src={service.imageUrl}
                                    alt={service.title}
                                    className="w-full h-40 object-cover transform group-hover:scale-105 group-hover:brightness-110 transition-transform duration-300"
                                />
                            </div>
                            <h3 className="text-lg font-semibold mt-4 group-hover:text-teal-700 transition-colors duration-200">
                                {service.title}
                            </h3>
                            <p className="text-sm text-gray-600 my-2">{service.description}</p>
                            <p className="font-bold text-teal-600">
                                {service.price.toLocaleString()}원~
                            </p>
                        </div>
                    ))}
                </div>
            </main>
        </div>
    );
};

export default CategoryPage;
