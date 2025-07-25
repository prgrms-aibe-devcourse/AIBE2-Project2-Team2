import React, { useState, useRef } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { useNavigate } from "react-router-dom"; // ✅ 추가

const categories = [
  { label: "디자인", emoji: "🎨", id: 1 },
  { label: "IT·프로그래밍", emoji: "💻", id: 75 },
  { label: "영상·사진·음향", emoji: "📷", id: 142 },
  { label: "마케팅", emoji: "📈", id: 195 },
  { label: "문서·글쓰기", emoji: "📝", id: 253 },
  { label: "창업·사업", emoji: "💼", id: 282 },
  { label: "세무·법무·노무", emoji: "👔", id: 319 },
  { label: "전자책", emoji: "📚", id: 337 },
  { label: "AI", emoji: "🤖", id: 105 },
  { label: "번역·통역", emoji: "🌐", id: 345 },
  { label: "주문제작", emoji: "📦", id: 360 },
  { label: "취업·입시", emoji: "🎓", id: 371 },
  { label: "투잡·노하우", emoji: "💡", id: 389 },
  { label: "직무역량 레슨", emoji: "📋", id: 398 },
  { label: "운세", emoji: "🔮", id: 418 },
  { label: "심리상담", emoji: "🧐", id: 434 },
  { label: "생활서비스", emoji: "🏡", id: 446 },
];

const MainPage = () => {
  const [expanded, setExpanded] = useState(false);
  const categoryRef = useRef(null);
  const navigate = useNavigate(); // ✅ 추가

  const defaultCategories = categories.slice(0, 6);
  const extraCategories = categories.slice(6);

  const handleToggle = () => {
    if (expanded) {
      setTimeout(() => {
        categoryRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
      }, 400);
    }
    setExpanded((prev) => !prev);
  };

  const handleCategoryClick = (id) => {
    navigate(`/category/${id}`);
  };

  return (
    <div className="min-h-screen px-4 py-40 bg-white text-center">
      <h1 className="text-3xl sm:text-4xl font-bold mb-6 leading-relaxed">
        으악이 필요한 순간,
        <br className="hidden sm:block" />딱 맞는 으아악을 찾아보세요
      </h1>

      <div className="flex justify-center mt-6 gap-3">
        <input type="text" placeholder="어떤 전문가가 필요하세요?" className="w-full max-w-md border border-gray-300 rounded-full px-6 py-3 shadow-sm focus:outline-none" />
        <button className="rounded-full bg-teal-600 text-white font-semibold px-6 py-2 shadow hover:bg-teal-700 transition">검색</button>
      </div>

      <div className="flex flex-wrap justify-center mt-4 gap-2 text-sm">
        {["홈페이지 신규 제작", "홈페이지제작", "홈페이지", "카페24", "워드프레스", "블로그"].map((keyword, i) => (
          <button key={i} className="px-4 py-1 rounded-full bg-gray-100 text-gray-600 hover:bg-gray-200 transition">
            {keyword}
          </button>
        ))}
      </div>

      {/* 기본 카테고리 */}
      <div ref={categoryRef} className="grid grid-cols-3 sm:grid-cols-6 gap-x-4 gap-y-6 mt-10 max-w-5xl mx-auto">
        {defaultCategories.map(({ label, emoji, id }) => (
          <div
            key={label}
            onClick={() => handleCategoryClick(id)} // ✅ 클릭 이벤트 연결
            className="bg-gray-100 h-[84px] px-3 py-4 rounded shadow-sm hover:bg-gray-200 cursor-pointer flex flex-col items-center justify-center">
            <span className="text-xl mb-1">{emoji}</span>
            <span className="text-sm text-center leading-tight break-keep">{label}</span>
          </div>
        ))}
      </div>

      {/* 추가 카테고리 */}
      <AnimatePresence>
        {expanded && (
          <motion.div initial={{ height: 0, opacity: 0 }} animate={{ height: "auto", opacity: 1 }} exit={{ height: 0, opacity: 0 }} transition={{ duration: 0.4 }} className="overflow-visible mt-6">
            <div className="grid grid-cols-3 sm:grid-cols-6 gap-x-4 gap-y-6 max-w-5xl mx-auto">
              {extraCategories.map(({ label, emoji, id }) => (
                <div
                  key={label}
                  onClick={() => handleCategoryClick(id)} // ✅ 클릭 이벤트 연결
                  className="bg-gray-100 h-[84px] px-3 py-4 rounded shadow-sm hover:bg-gray-200 cursor-pointer flex flex-col items-center justify-center">
                  <span className="text-xl mb-1">{emoji}</span>
                  <span className="text-sm text-center leading-tight break-keep">{label}</span>
                </div>
              ))}
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      <div className="mt-8">
        <button onClick={handleToggle} className="border border-gray-300 px-4 py-2 rounded-md text-sm text-black hover:bg-gray-100 transition">
          {expanded ? "닫기" : "전체보기"}
        </button>
      </div>
    </div>
  );
};

export default MainPage;
