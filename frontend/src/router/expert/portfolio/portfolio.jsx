import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axiosInstance from "../../../lib/axios.js";
import { ChevronLeft, ChevronRight, X, Star, Eye, Edit2, Trash2 } from "lucide-react";
import { useUserInfoStore } from "../../../store/userInfo.js";
import toast from "react-hot-toast";

export default function Portfolio() {
  const { id } = useParams(); // /portfolio/:id
  const { userInfo } = useUserInfoStore();
  const [portfolio, setPortfolio] = useState(null);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  const navigate = useNavigate();

  useEffect(() => {
    const fetchPortfolio = async () => {
      try {
        const response = await axiosInstance.get(`/api/expert/portfolio/${id}`);
        setPortfolio(response.data);
      } catch (error) {
        console.error("포트폴리오 조회 실패:", error);
      }
    };
    fetchPortfolio();
  }, [id]);

  if (!portfolio) {
    return <div className="p-10 text-center">로딩 중...</div>;
  }

  const imagesWithThumbnail = (() => {
    if (!portfolio.thumbnailImage) return portfolio.images;

    // 썸네일 ID 기준으로 정렬
    const sortedImages = [...portfolio.images].sort((a, b) => {
      if (a.id === portfolio.thumbnailImage.id) return -1;
      if (b.id === portfolio.thumbnailImage.id) return 1;
      return 0;
    });

    return sortedImages;
  })();

  const isOwner = userInfo && userInfo.nickname === portfolio.expertNickname;

  const nextImage = () => {
    setCurrentImageIndex((prev) => (prev + 1) % imagesWithThumbnail.length);
  };

  const prevImage = () => {
    setCurrentImageIndex((prev) => (prev === 0 ? imagesWithThumbnail.length - 1 : prev - 1));
  };

  const handleEdit = () => {
    console.log("수정하기");
    // 수정 페이지로 이동하는 로직 필요
    navigate(`/expert/portfolio/edit/${id}`);
  };

  const handleDelete = async () => {
    if (confirm("정말로 삭제하시겠습니까?")) {
      try {
        await axiosInstance.delete(`/api/expert/portfolio/${id}`);
        toast.success("삭제되었습니다.");
        navigate("/expert/profile"); // 삭제 후 프로필 페이지로 이동
      } catch (error) {
        toast.error("삭제에 실패했습니다.");
        console.error("삭제 실패:", error);
      }
    }
  };

  return (
    <div className="bg-gray-50 flex overflow-hidden">
      {/* 좌측 - 이미지 슬라이더 */}
      <div className="flex-1 p-2 flex flex-col min-w-0">
        <div className="relative bg-white rounded-2xl overflow-hidden shadow-sm border border-gray-100 flex items-center justify-center" style={{ height: "550px", maxWidth: "100%" }}>
          <img src={imagesWithThumbnail[currentImageIndex].url} alt="포트폴리오 이미지" className="max-w-full max-h-full object-contain" />
          {imagesWithThumbnail.length > 1 && (
            <>
              <button onClick={prevImage} className="absolute left-3 top-1/2 -translate-y-1/2 w-8 h-8 bg-white/90 backdrop-blur-sm rounded-full flex items-center justify-center shadow hover:bg-white transition-all duration-200">
                <ChevronLeft className="w-4 h-4 text-gray-700" />
              </button>
              <button onClick={nextImage} className="absolute right-3 top-1/2 -translate-y-1/2 w-8 h-8 bg-white/90 backdrop-blur-sm rounded-full flex items-center justify-center shadow hover:bg-white transition-all duration-200">
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
              <button key={idx} onClick={() => setCurrentImageIndex(idx)} className={`w-2 h-2 rounded-full transition-all duration-200 ${idx === currentImageIndex ? "bg-indigo-600 w-5" : "bg-gray-300 hover:bg-gray-400"}`} aria-label={`이미지 ${idx + 1} 선택`} />
            ))}
          </div>
        )}
      </div>

      {/* 우측 - 정보 패널 */}
      <div className="w-60 bg-white border-l border-gray-200 flex flex-col">
        <div className="p-4 border-b border-gray-100 flex items-start justify-between">
          <h1 className="text-lg font-semibold text-gray-900 leading-tight">{portfolio.title}</h1>
          <button className="text-gray-400 hover:text-gray-600 transition-colors" aria-label="닫기">
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="p-0 px-4 mb-4 flex items-center space-x-3 text-xs text-gray-500">
          <div className="flex items-center space-x-1">
            <Eye className="w-3 h-3" />
            <span>조회 {portfolio.viewCount.toLocaleString()}</span>
          </div>
        </div>

        <div className="p-4 border-b border-gray-100 flex items-center space-x-2">
          <img src={portfolio.expertProfileImageUrl} alt="전문가 프로필" className="w-10 h-10 rounded-full object-cover" />
          <div className="flex-1">
            <p className="font-semibold text-gray-900 text-sm">{portfolio.expertNickname}</p>
            <div className="flex items-center space-x-1 mt-1">
              <Star className="w-3 h-3 fill-yellow-400 text-yellow-400" />
              <span className="text-xs font-medium text-gray-700">{portfolio.rating}</span>
              <span className="text-xs text-gray-500">({portfolio.reviewCount})</span>
            </div>
          </div>
        </div>

        <div className="flex-1 p-4 space-y-3 text-xs overflow-hidden">
          <div>
            <p className="font-medium text-gray-500 mb-1">서비스 종류</p>
            <p className="text-gray-900">{portfolio.category}</p>
          </div>
          <div>
            <p className="font-medium text-gray-500 mb-1">작업년도</p>
            <p className="text-gray-900">{portfolio.workingYear}</p>
          </div>
          <div>
            <p className="font-medium text-gray-500 mb-2">프로젝트 설명</p>
            <div className="text-gray-700 leading-relaxed">{portfolio.content}</div>
          </div>
        </div>

        {isOwner && (
          <div className="p-4 border-t border-gray-100">
            <div className="flex space-x-2">
              <button onClick={handleEdit} className="flex-1 flex items-center justify-center space-x-1 bg-indigo-600 text-white py-2 px-3 rounded font-medium hover:bg-indigo-700 transition-colors text-sm">
                <Edit2 className="w-4 h-4" />
                <span>수정</span>
              </button>
              <button onClick={handleDelete} className="flex items-center justify-center space-x-1 px-3 py-2 border border-red-300 text-red-600 rounded font-medium hover:bg-red-50 transition-colors text-sm">
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
