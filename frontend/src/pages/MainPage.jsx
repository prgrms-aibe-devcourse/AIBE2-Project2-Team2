import React, { useState, useRef } from "react";
import { motion, AnimatePresence } from "framer-motion";

const allCategories = [
    "디자인", "IT·프로그래밍", "영상·사진·음향", "마케팅", "문서·글쓰기",
    "창업·사업", "세무·법무·노무", "전자책", "AI 서비스", "번역·통역",
    "주문제작", "취업·입시", "투잡·노하우", "직무역량 레슨", "운세",
    "심리상담", "취미 레슨", "생활서비스"
];

const categoryEmojis = [
    "🎨", "💻", "📷", "📊", "📝", "💼", "👔", "📚", "🤖", "🌐",
    "📦", "🎓", "💡", "📋", "🔮", "🧠", "🎸", "🏡"
];

const MainPage = () => {
    const [expanded, setExpanded] = useState(false);
    const categoryRef = useRef(null); // 기준 위치 ref

    const defaultCategories = allCategories.slice(0, 6);
    const extraCategories = allCategories.slice(6);
    const defaultEmojis = categoryEmojis.slice(0, 6);
    const extraEmojis = categoryEmojis.slice(6);

    const handleToggle = () => {
        if (expanded) {
            setTimeout(() => {
                categoryRef.current?.scrollIntoView({
                    behavior: "smooth",
                    block: "start",
                });
            }, 400); // 애니메이션 끝나고 스크롤
        }

        setExpanded((prev) => !prev);
    };

    return (
        <div className="min-h-screen px-4 py-12 pt-45 bg-white text-center">
            <h1 className="text-3xl font-bold mb-4 leading-relaxed">
                으악이 필요한 순간,<br />
                딱 맞는 으아악을 찾아보세요
            </h1>

            {/* 검색 바 */}
            <div className="flex justify-center mt-6">
                <input
                    type="text"
                    placeholder="어떤 전문가가 필요하세요?"
                    className="w-96 border border-gray-300 rounded-full px-6 py-3 shadow focus:outline-none"
                />
                <button className="ml-3 rounded-full bg-[#0d9488] text-white font-semibold px-6 py-2 shadow">
                    검색
                </button>
            </div>

            {/* 추천 키워드 */}
            <div className="flex flex-wrap justify-center mt-4 gap-2">
                {["홈페이지 신규 제작", "홈페이지제작", "홈페이지", "카페24", "워드프레스", "블로그"].map((keyword, i) => (
                    <button
                        key={i}
                        className="px-4 py-1 rounded-full bg-gray-100 text-sm text-gray-600 hover:bg-gray-200 transition"
                    >
                        {keyword}
                    </button>
                ))}
            </div>

            {/* 기본 카테고리 */}
            <div
                ref={categoryRef}
                className="grid grid-cols-3 sm:grid-cols-6 gap-x-4 gap-y-6 mt-10 max-w-5xl mx-auto"
            >
                {defaultCategories.map((cat, index) => (
                    <div
                        key={cat}
                        className="bg-gray-100 h-[84px] px-3 py-4 rounded shadow-sm hover:bg-gray-200 cursor-pointer flex flex-col items-center"
                    >
                        <span className="text-xl mb-1">{defaultEmojis[index]}</span>
                        <span className="text-sm leading-tight break-keep text-center">{cat}</span>
                    </div>
                ))}
            </div>

            {/* 확장 영역 */}
            <AnimatePresence>
                {expanded && (
                    <motion.div
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: "auto", opacity: 1 }}
                        exit={{ height: 0, opacity: 0 }}
                        transition={{ duration: 0.4 }}
                        className="overflow-visible mt-6"
                    >
                        <div className="grid grid-cols-3 sm:grid-cols-6 gap-x-4 gap-y-6 max-w-5xl mx-auto">
                            {extraCategories.map((cat, index) => (
                                <div
                                    key={cat}
                                    className="bg-gray-100 h-[84px] px-3 py-4 rounded shadow-sm hover:bg-gray-200 cursor-pointer flex flex-col items-center"
                                >
                                    <span className="text-xl mb-1">{extraEmojis[index]}</span>
                                    <span className="text-sm leading-tight break-keep text-center">{cat}</span>
                                </div>
                            ))}
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>

            {/* 전체보기 토글 버튼 */}
            <div className="mt-8">
                <button
                    onClick={handleToggle}
                    className="border border-gray-300 px-4 py-2 rounded-md text-sm text-black hover:bg-gray-100 transition"
                >
                    {expanded ? "닫기" : "전체보기"}
                </button>
            </div>
        </div>
    );
};

export default MainPage;
