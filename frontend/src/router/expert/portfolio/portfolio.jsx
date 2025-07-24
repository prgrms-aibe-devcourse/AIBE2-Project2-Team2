import { useState } from "react";
import { ChevronLeft, ChevronRight, X, Star, Eye, Edit2, Trash2 } from "lucide-react";
import { create } from "zustand";

const useUserInfoStore = create((set) => ({
    userInfo: { userId: "user123", nickname: "expertUser" },
    setUserInfo: (info) => set({ userInfo: info }),
    clearUserInfo: () => set({ userInfo: null }),
}));

const mockPortfolio = {
    portfolioId: 123,
    title: "웹사이트 개발 프로젝트",
    content: "이 프로젝트는 React와 Node.js를 사용하여 개발한 풀스택 웹 애플리케이션입니다. 사용자 친화적인 인터페이스와 효율적인 백엔드 구조를 구현했습니다.",
    viewCount: 150,
    workingYear: 3,
    category: "웹/모바일 개발",
    images: [
        {
            id: 2,
            url: "https://firebasestorage.googleapis.com/v0/b/team2maldive.firebasestorage.app/o/portfolio%2Fuser123_portfolio_image_1_1-52af1745-9ff4-4554-8d40-afee7a9b8592.png?alt=media",
        },
        {
            id: 3,
            url: "https://firebasestorage.googleapis.com/v0/b/team2maldive.firebasestorage.app/o/portfolio%2Fuser123_portfolio_image_1_2-a5bd9e68-edc2-4980-b975-70478e258586.png?alt=media",
        },
    ],
    thumbnailImage: {
        id: 1,
        url: "https://firebasestorage.googleapis.com/v0/b/team2maldive.firebasestorage.app/o/portfolio%2Fuser123_portfolio_image_1_0-d514dfb2-9aab-4357-ba9a-e8d07d49796a.png?alt=media",
    },
    reviewCount: 25,
    rating: 4.8,
    expertNickname: "expertUser",
    expertProfileImageUrl: "https://profile.image.url",
};

export default function Portfolio() {
    const { userInfo } = useUserInfoStore();

    const imagesWithThumbnail = (() => {
        if (!mockPortfolio.thumbnailImage) return mockPortfolio.images;
        const exists = mockPortfolio.images.some(
            (img) => img.id === mockPortfolio.thumbnailImage.id
        );
        if (exists) return mockPortfolio.images;
        return [mockPortfolio.thumbnailImage, ...mockPortfolio.images];
    })();

    const [currentImageIndex, setCurrentImageIndex] = useState(0);
    const isOwner = userInfo && userInfo.nickname === mockPortfolio.expertNickname;

    const nextImage = () => {
        setCurrentImageIndex((prev) => (prev + 1) % imagesWithThumbnail.length);
    };

    const prevImage = () => {
        setCurrentImageIndex((prev) =>
            prev === 0 ? imagesWithThumbnail.length - 1 : prev - 1
        );
    };

    const handleEdit = () => {
        console.log("수정하기");
    };

    const handleDelete = () => {
        if (confirm("정말로 삭제하시겠습니까?")) {
            console.log("삭제하기");
        }
    };

    return (
        <div className="bg-gray-50 flex overflow-hidden">
            {/* Left Side - Image Slider */}
            <div className="flex-1 p-2 flex flex-col min-w-0">
                <div
                    className="relative bg-white rounded-2xl overflow-hidden shadow-sm border border-gray-100 flex items-center justify-center"
                    style={{
                        height: '550px',
                        maxWidth: '100%'
                    }}
                >
                    <img
                        src={imagesWithThumbnail[currentImageIndex].url}
                        alt="포트폴리오 이미지"
                        className="max-w-full max-h-full object-contain"
                    />

                    {imagesWithThumbnail.length > 1 && (
                        <>
                            <button
                                onClick={prevImage}
                                className="absolute left-3 top-1/2 -translate-y-1/2 w-8 h-8 bg-white/90 backdrop-blur-sm rounded-full flex items-center justify-center shadow hover:bg-white transition-all duration-200"
                                aria-label="이전 이미지"
                            >
                                <ChevronLeft className="w-4 h-4 text-gray-700" />
                            </button>
                            <button
                                onClick={nextImage}
                                className="absolute right-3 top-1/2 -translate-y-1/2 w-8 h-8 bg-white/90 backdrop-blur-sm rounded-full flex items-center justify-center shadow hover:bg-white transition-all duration-200"
                                aria-label="다음 이미지"
                            >
                                <ChevronRight className="w-4 h-4 text-gray-700" />
                            </button>
                        </>
                    )}

                    <div className="absolute bottom-3 right-3 bg-black/50 backdrop-blur-sm px-2 py-0.5 rounded-full text-white text-xs select-none">
                        {currentImageIndex + 1} / {imagesWithThumbnail.length}
                    </div>
                </div>

                {imagesWithThumbnail.length > 1 && (
                    <div className="flex justify-center pt-4 space-x-2">
                        {imagesWithThumbnail.map((_, idx) => (
                            <button
                                key={idx}
                                onClick={() => setCurrentImageIndex(idx)}
                                className={`w-2 h-2 rounded-full transition-all duration-200 ${
                                    idx === currentImageIndex
                                        ? "bg-indigo-600 w-5"
                                        : "bg-gray-300 hover:bg-gray-400"
                                }`}
                                aria-label={`이미지 ${idx + 1} 선택`}
                            />
                        ))}
                    </div>
                )}
            </div>

            {/* Right Side - Information Panel */}
            <div className="w-60 bg-white border-l border-gray-200 flex flex-col">
                {/* 헤더 */}
                <div className="p-4 border-b border-gray-100 flex items-start justify-between">
                    <h1 className="text-lg font-semibold text-gray-900 leading-tight">
                        {mockPortfolio.title}
                    </h1>
                    <button
                        className="text-gray-400 hover:text-gray-600 transition-colors"
                        aria-label="포트폴리오 닫기"
                    >
                        <X className="w-4 h-4" />
                    </button>
                </div>

                <div className="p-0 px-4 mb-4 flex items-center space-x-3 text-xs text-gray-500">
                    <div className="flex items-center space-x-1">
                        <Eye className="w-3 h-3" />
                        <span>조회 {mockPortfolio.viewCount.toLocaleString()}</span>
                    </div>
                </div>

                <div className="p-4 border-b border-gray-100 flex items-center space-x-2">
                    <img
                        src={mockPortfolio.expertProfileImageUrl}
                        alt="전문가 프로필"
                        className="w-10 h-10 rounded-full object-cover"
                    />
                    <div className="flex-1">
                        <p className="font-semibold text-gray-900 text-sm">
                            {mockPortfolio.expertNickname}
                        </p>
                        <div className="flex items-center space-x-1 mt-1">
                            <Star className="w-3 h-3 fill-yellow-400 text-yellow-400" />
                            <span className="text-xs font-medium text-gray-700">
                {mockPortfolio.rating}
              </span>
                            <span className="text-xs text-gray-500">
                ({mockPortfolio.reviewCount})
              </span>
                        </div>
                    </div>
                </div>

                <div className="flex-1 p-4 space-y-3 text-xs overflow-hidden">
                    <div>
                        <p className="font-medium text-gray-500 mb-1">서비스 종류</p>
                        <p className="text-gray-900">{mockPortfolio.category}</p>
                    </div>
                    <div>
                        <p className="font-medium text-gray-500 mb-1">작업년도</p>
                        <p className="text-gray-900">{mockPortfolio.workingYear}</p>
                    </div>
                    <div>
                        <p className="font-medium text-gray-500 mb-2">프로젝트 설명</p>
                        <div className="text-gray-700 leading-relaxed">
                            {mockPortfolio.content}
                        </div>
                    </div>
                </div>

                {isOwner && (
                    <div className="p-4 border-t border-gray-100">
                        <div className="flex space-x-2">
                            <button
                                onClick={handleEdit}
                                className="flex-1 flex items-center justify-center space-x-1 bg-indigo-600 text-white py-2 px-3 rounded font-medium hover:bg-indigo-700 transition-colors text-sm"
                            >
                                <Edit2 className="w-4 h-4" />
                                <span>수정</span>
                            </button>
                            <button
                                onClick={handleDelete}
                                className="flex items-center justify-center space-x-1 px-3 py-2 border border-red-300 text-red-600 rounded font-medium hover:bg-red-50 transition-colors text-sm"
                            >
                                <Trash2 className="w-4 h-4" />
                                <span>삭제</span>
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}