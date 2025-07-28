import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axiosInstance from "../../lib/axios";
import ImageModal from "../../components/modal/ImageModal";
import ShareModal from "../../components/modal/ShareModal";

import { useUserInfoStore } from "../../store/userInfo";
import toast from "react-hot-toast";
import { Star, ExternalLink, Users, GraduationCap, Calendar, Globe, Facebook, Instagram, Twitter, Plus, Share2 } from "lucide-react";

// 리뷰 응답 예시
// {
//   totalRating: 4.5,
//   totalReviewCount: 2,
//   reviews: { content: [ ... ] }
// }

const TABS = [
  { key: "desc", label: "서비스 설명" },
  { key: "price", label: "가격 정보" },
  { key: "expert", label: "전문가 정보" },
  { key: "review", label: "리뷰" },
];

function ContentDetailPage() {
  const { id } = useParams();
  const [content, setContent] = useState(null);
  const [selectedTab, setSelectedTab] = useState("desc");
  const [loading, setLoading] = useState(true);
  const [reviewLoading, setReviewLoading] = useState(false);
  const [reviews, setReviews] = useState([]);
  const [expertProfile, setExpertProfile] = useState(null);
  const [expertLoading, setExpertLoading] = useState(false);
  const [selectedOptions, setSelectedOptions] = useState({}); // 선택된 옵션들을 관리
  const [imageModalOpen, setImageModalOpen] = useState(false);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [shareModalOpen, setShareModalOpen] = useState(false);
  const navigate = useNavigate();
  const [otherContents, setOtherContents] = useState([]);

  const { userInfo } = useUserInfoStore();



  // ✅ Content!! 에서 채팅방 생성하는 함수
  const handleCreateChatRoom = async (targetEmail) => {
    try {
      const res = await axiosInstance.post("/api/chat/rooms/find-or-create", {
        targetEmail: targetEmail,
      });

      const room = res.data; // ChatRoomDto
      navigate(`/chat/${room.roomId}`); // ✅ 채팅방으로 이동
    } catch (err) {
      console.error("❌ 채팅방 생성 실패:", err);
      alert("채팅방 생성 실패");
    }
  };

  // 콘텐츠 삭제 함수
  const handleDeleteContent = async () => {
    try {
      await axiosInstance.delete(`/api/content/${id}`);
      toast.success("서비스가 삭제되었습니다.");
      navigate("/");
    } catch (err) {
      console.error("❌ 서비스 삭제 실패:", err);
      toast.error("서비스 삭제에 실패했습니다.");
    }
  };

  // 이미지 클릭 핸들러
  const handleImageClick = (index) => {
    setCurrentImageIndex(index);
    setImageModalOpen(true);
  };

  // 옵션 선택 핸들러
  const handleOptionChange = (questionId, optionId, isMultipleChoice) => {
    setSelectedOptions(prev => {
      const newSelected = { ...prev };
      
      if (isMultipleChoice) {
        // 체크박스: 복수 선택 가능
        if (!newSelected[questionId]) {
          newSelected[questionId] = [];
        }
        const currentOptions = newSelected[questionId];
        
        if (currentOptions.includes(optionId)) {
          // 이미 선택된 옵션 제거
          newSelected[questionId] = currentOptions.filter(id => id !== optionId);
        } else {
          // 새로운 옵션 추가
          newSelected[questionId] = [...currentOptions, optionId];
        }
      } else {
        // 라디오: 단일 선택
        newSelected[questionId] = [optionId];
      }
      
      console.log('선택된 옵션들:', newSelected);
      return newSelected;
    });
  };

  // 총 가격 계산 함수
  const calculateTotalPrice = () => {
    if (!content) return 0;
    
    let total = content.budget || 0;
    
    // 선택된 옵션들의 추가 비용 계산
    Object.values(selectedOptions).forEach(optionIds => {
      optionIds.forEach(optionId => {
        content.questions?.forEach(question => {
          const option = question.options.find(opt => opt.optionId === optionId);
          if (option) {
            total += option.additionalPrice || 0;
          }
        });
      });
    });
    
    console.log('총 가격 계산:', { total, selectedOptions, contentBudget: content.budget });
    return total;
  };

  // 추가 비용 계산 함수
  const calculateAdditionalPrice = () => {
    if (!content) return 0;
    
    let additionalTotal = 0;
    
    // 선택된 옵션들의 추가 비용만 계산
    Object.values(selectedOptions).forEach(optionIds => {
      optionIds.forEach(optionId => {
        content.questions?.forEach(question => {
          const option = question.options.find(opt => opt.optionId === optionId);
          if (option) {
            additionalTotal += option.additionalPrice || 0;
          }
        });
      });
    });
    
    console.log('추가 비용 계산:', { additionalTotal, selectedOptions });
    return additionalTotal;
  };

  useEffect(() => {
    if (!id) return;
    async function fetchData() {
      setLoading(true);
      try {
        const contentRes = await axiosInstance.get(`/api/content/${id}`);
        const contentData = contentRes.data;
        
        // 삭제된 콘텐츠인지 확인
        if (contentData.status === "DELETED") {
          toast.error("삭제된 서비스입니다.");
          navigate("/");
          return;
        }
        
        setContent(contentData);
      } catch (err) {
        console.error("API 요청 에러:", err);
        toast.error("서비스를 찾을 수 없습니다.");
        navigate("/");
      }
      setLoading(false);
    }
    fetchData();
  }, [id, navigate]);

  //리뷰 요청
  const handleReviewRequest = async () => {
    if (!content) return;
    setReviewLoading(true);
    try {
      const res = await axiosInstance.get(`/api/reviews/${id}`);
      const data = res.data;
      setReviews(data.reviews?.content || []);
    } catch (err) {
      setReviews([]);
      console.error("리뷰 요청 에러:", err);
    }
    setReviewLoading(false);
  };

  // 리뷰탭 클릭시 리뷰 요청, 전문가탭 클릭시 전문가 프로필 요청
  useEffect(() => {
    if (selectedTab === "review") {
      handleReviewRequest();
    } else if (selectedTab === "expert") {
      handleExpertProfileRequest();
    }
    // eslint-disable-next-line
  }, [selectedTab]);

  // 전문가 프로필 미리 불러오기 (content.expertId가 바뀔 때마다)
  useEffect(() => {
    if (!content?.expertId) return;
    setExpertLoading(true);
    axiosInstance.get(`/api/expert/profile/${content.expertId}`)
      .then(res => setExpertProfile(res.data))
      .catch(() => setExpertProfile(null))
      .finally(() => setExpertLoading(false));
  }, [content?.expertId]);

  // 전문가 정보 탭 클릭 시 별도 fetch 없이 expertProfile만 사용하도록 handleExpertProfileRequest 함수 내 내용 제거
  const handleExpertProfileRequest = async () => {};

  useEffect(() => {
    if (content?.expertId) {
      axiosInstance.get(`/api/content/expert/${content.expertId}`)
        .then(res => {
          // 현재 contentId는 제외
          setOtherContents(res.data.filter(c => c.contentId !== content.contentId));
        });
    }
  }, [content?.expertId, content?.contentId]);

  if (loading || !content) {
    return <div className="flex justify-center items-center h-96">로딩 중...</div>;
  }

  return (
    <div className="w-full flex flex-col items-center mb-8">
      {/* 상단: 카테고리, 제목, 찜/공유/스크랩, 썸네일 */}
      <div className="w-full max-w-6xl">
        <div className="text-sm text-gray-400 mb-2">
          {content.categoryName ? content.categoryName : "카테고리"}
        </div>
        <div className="flex items-center gap-2 mb-2">
          <span className="text-3xl font-bold">{content.title}</span>        
          <span
            className="ml-2 text-gray-400 cursor-pointer hover:text-gray-600 transition-colors flex items-center"
            onClick={() => setShareModalOpen(true)}
            title="공유하기"
          >
            <Share2 className="w-5 h-5" />
          </span>
        </div>
        <div className="w-full flex gap-4 mt-2">
          {/* 전문가 정보/문의 버튼 */}
          <div className="flex-1 flex items-center gap-2 bg-white rounded-lg px-4 py-2 shadow">
            <img src={content.expertProfileImageUrl || "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png"} alt="프로필" className="w-10 h-10 rounded-full" />
            <div>
              <div className="font-semibold">{content.expertNickname || "전문가"}</div>
              <div className="text-xs text-gray-400">
                {expertProfile ? (
                  <>
                    {expertProfile.introduction || "-"}
                    {" | 총 경력 : "}
                    {expertProfile.totalCareerYears ? `${expertProfile.totalCareerYears}년` : "-"}
                    {" | 기술 스택 : "}
                    {expertProfile.skills && expertProfile.skills.length > 0
                      ? expertProfile.skills.map(s => `${s.name}${s.category ? `(${s.category})` : ''}`).join(", ")
                      : "-"}
                  </>
                ) : (
                  <span className="text-gray-300">전문가 정보 불러오는 중...</span>
                )}
              </div>
            </div>
            <button 
              className="ml-4 bg-gray-100 px-4 py-2 rounded font-semibold hover:bg-gray-200 transition-colors cursor-pointer"
              onClick={() => handleCreateChatRoom(content.expertEmail)}
            >
              문의하기
            </button>
          </div>
          {/* 썸네일 */}
          <div className="w-80 h-48 bg-gray-200 rounded-lg flex items-center justify-center overflow-hidden">{content.contentUrl ? <img src={content.contentUrl} alt="대표 이미지" className="object-cover w-full h-full" /> : <span className="text-gray-400">썸네일 없음</span>}</div>
        </div>
        {/* 안내 사항 */}
        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4 mt-6 flex items-center gap-4">
          <div className="bg-black text-white text-xs px-2 py-1 rounded">안내 사항</div>
          <div className="text-sm">
            가격 및 기타 문의 사항이 있으신 경우 상단 혹은 우측 전문가에게 문의하기 버튼을 통해 문의해주세요.
            <br />
            <span className="text-blue-600 font-semibold">고객 후기가 검증된 퀄리티</span> | <span className="text-blue-600 font-semibold">경력 이상의 인사이트를 담은 서비스</span> | <span className="text-blue-600 font-semibold">다양한 업종에 맞춘 맞춤 전문성</span>
          </div>
        </div>
      </div>
      {/* 본문 좌/우 분할 */}
      <div className="w-full max-w-6xl flex gap-8 mt-8">
        {/* 좌측 메인 */}
        <div className="flex-1">
          {/* 탭 메뉴 */}
          <div className="flex border-b mb-4 gap-2">
            {TABS.map((tab) => (
              <button key={tab.key} className={`px-4 py-2 font-semibold border-b-2 transition-all ${selectedTab === tab.key ? "border-black text-black" : "border-transparent text-gray-400"}`} onClick={() => setSelectedTab(tab.key)}>
                {tab.label}
              </button>
            ))}
          </div>
          {/* 탭별 본문 */}
          <div className="mt-4">
            {selectedTab === "desc" && (
              <div>

                {/* Content 이미지들 */}
                <div className="mb-4">
                  <h3 className="text-lg font-bold mb-2">서비스 이미지</h3>
                  {content.imageUrls && content.imageUrls.length > 0 ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                      {content.imageUrls.map((imageUrl, index) => (
                        <div key={index} className="relative group cursor-pointer" onClick={() => handleImageClick(index)}>
                          <img 
                            src={imageUrl} 
                            alt={`서비스 이미지 ${index + 1}`} 
                            className="w-full h-48 object-cover rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
                          />
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-gray-400 text-center py-8 bg-gray-50 rounded-lg">
                      이미지가 없습니다.
                    </div>
                  )}
                </div>
                <h2 className="text-lg font-bold mb-2">서비스 설명</h2>          
                <div className="whitespace-pre-line text-gray-800 bg-gray-50 p-4 rounded mb-4">{content.description}</div>
                <div className="mt-8">
                  <h3 className="font-bold mb-2">이 전문가의 다른 서비스 보기</h3>
                  <div className="flex gap-4 flex-wrap">
                    {otherContents.length === 0 ? (
                      <div className="text-gray-400">다른 서비스가 없습니다.</div>
                    ) : (
                      otherContents.map(c => (
                        <div key={c.contentId} className="p-4 cursor-pointer hover:bg-gray-50 w-64"
                             onClick={() => navigate(`/content/${c.contentId}`)}>
                          {c.contentUrl && (
                            <img src={c.contentUrl} alt="썸네일" className="w-full h-32 object-cover rounded mb-2" />
                          )}
                          <div className="font-semibold truncate mb-1 text-base">{c.title}</div>
                          <div className="text-sm text-blue-600 font-bold mb-1">{c.budget?.toLocaleString()}원~</div>                        
                          <div className="text-xs text-gray-400">{c.categoryName}</div>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              </div>
            )}
            {/* 나머지 탭은 기존과 동일하게 유지 */}
            {selectedTab === "price" && (
              <div>
                <h2 className="text-lg font-bold mb-2">가격 정보</h2>
                
                {/* 기본 가격 정보 */}
                <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
                  <div className="mb-4">
                    <h3 className="text-xl font-bold text-gray-800 mb-2">기본 서비스</h3>
                  </div>
                  <div className="flex items-center justify-between mb-4">
                    <h2 className="text-lg font-semibold text-gray-700">{content.title}</h2>
                    <div className="text-2xl font-bold text-blue-600">
                      {content.budget?.toLocaleString()}원
                    </div>
                  </div>
                  <div className="text-gray-600 mb-4">
                    <p className="text-sm">VAT 포함 가격</p>
                  </div>
                </div>

                {/* 질문 및 옵션들 */}
                {content.questions && content.questions.length > 0 && (
                  <div className="space-y-6">
                    <h3 className="text-lg font-bold text-gray-800 mb-4">추가 옵션</h3>
                    {content.questions.map((question, questionIndex) => (
                      <div key={question.questionId} className="bg-white border border-gray-200 rounded-lg p-6">
                        <div className="mb-4">
                          <h4 className="text-lg font-semibold text-gray-800 mb-2">
                            {question.questionText}
                          </h4>
                          {question.isMultipleChoice && (
                            <p className="text-sm text-gray-500 mb-3">복수 선택 가능</p>
                          )}
                        </div>
                        
                                                                          <div className="space-y-3">
                           {question.options.map((option) => (
                             <div key={option.optionId} className="flex items-center justify-between p-4 border border-gray-100 rounded-lg hover:bg-gray-50 transition-colors">
                               <div className="flex items-center">
                                 <input 
                                   type={question.isMultipleChoice ? "checkbox" : "radio"} 
                                   name={`question-${questionIndex}`}
                                   id={`option-${option.optionId}`}
                                   className="mr-3"
                                   checked={selectedOptions[question.questionId]?.includes(option.optionId) || false}
                                   onChange={() => handleOptionChange(question.questionId, option.optionId, question.isMultipleChoice)}
                                 />
                                 <label htmlFor={`option-${option.optionId}`} className="text-gray-700 cursor-pointer">
                                   {option.optionText}
                                 </label>
                               </div>
                               <div className="text-right">
                                 <div className="text-lg font-semibold text-blue-600">
                                   +{option.additionalPrice.toLocaleString()}원
                                 </div>
                                 <div className="text-sm text-gray-500">추가 비용</div>
                               </div>
                             </div>
                           ))}
                         </div>
                      </div>
                    ))}
                  </div>
                )}

                                 {/* 총 가격 계산 안내 */}
                 <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
                   <div className="flex items-center justify-between">
                     <div>
                       <h4 className="font-semibold text-blue-800 mb-1">총 예상 비용</h4>
                       <p className="text-sm text-blue-600">
                         기본 가격 + 선택한 옵션들의 추가 비용
                       </p>
                     </div>
                     <div className="text-right">
                       <div className="text-2xl font-bold text-blue-800">
                         {calculateTotalPrice().toLocaleString()}원
                       </div>
                       <div className="text-sm text-blue-600">
                         {content.budget?.toLocaleString()}원 (기본) + {calculateAdditionalPrice().toLocaleString()}원 (추가)
                       </div>
                     </div>
                   </div>
                 </div>
              </div>
            )}
            {selectedTab === "faq" && (
              <div>
                <h2 className="text-lg font-bold mb-2">자주 묻는 질문</h2>
                <div className="bg-gray-50 p-4 rounded">FAQ가 여기에 표시됩니다.</div>
              </div>
            )}
            {selectedTab === "expert" && (
              <div>
                <h2 className="text-lg font-bold mb-4">전문가 정보</h2>
                {expertLoading ? (
                  <div className="text-center py-4 text-gray-400">로딩 중...</div>
                ) : expertProfile ? (
                  <div className="bg-white rounded-lg shadow-sm border border-gray-100 p-6">
                    {/* 기본 프로필 정보 */}
                    <div className="flex items-center gap-4 mb-6">
                      <div className="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center overflow-hidden border border-gray-200">
                        <img src={expertProfile.profileImageUrl || "/api/placeholder/64/64"} alt="프로필 이미지" className="w-full h-full object-cover object-center" />
                      </div>
                      <div>
                        <h3 className="text-lg font-semibold text-gray-800 mb-1">{expertProfile.nickname}</h3>
                        {expertProfile.reviewCount > 0 && (
                          <div className="flex items-center gap-2 mb-1">
                            <div className="flex items-center gap-1">
                              {/* 별점 렌더링 함수가 필요하면 추가 */}
                              <span className="text-yellow-400">★</span>
                            </div>
                            <span className="text-sm text-gray-600">
                              {expertProfile.averageScore} ({expertProfile.reviewCount}개 리뷰)
                            </span>
                          </div>
                        )}
                        {expertProfile.region && <div className="text-sm text-gray-600">{expertProfile.region}</div>}
                      </div>
                    </div>

                    <div className="space-y-4">
                      {/* 자기소개 */}
                      {expertProfile.introduction && (
                        <div>
                          <label className="block font-medium mb-1 text-gray-700 text-sm">자기소개</label>
                          <div className="rounded border border-gray-200 bg-gray-50 px-3 py-2 text-gray-700 text-sm">{expertProfile.introduction}</div>
                        </div>
                      )}

                      {/* 경력 및 교육 */}
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {expertProfile.totalCareerYears > 0 && (
                          <div>
                            <label className="block font-medium mb-1 text-gray-700 text-sm">총 경력</label>
                            <div className="rounded border border-gray-200 bg-gray-50 px-3 py-2 text-gray-700 text-sm">{expertProfile.totalCareerYears}년</div>
                          </div>
                        )}

                        {expertProfile.education && (
                          <div>
                            <label className="block font-medium mb-1 text-gray-700 text-sm">학력</label>
                            <div className="rounded border border-gray-200 bg-gray-50 px-3 py-2 text-gray-700 text-sm">{expertProfile.education}</div>
                          </div>
                        )}
                      </div>

                      {/* 직원 수 */}
                      {expertProfile.employeeCount > 0 && (
                        <div>
                          <label className="block font-medium mb-1 text-gray-700 text-sm">직원 수</label>
                          <div className="rounded border border-gray-200 bg-gray-50 px-3 py-2 text-gray-700 text-sm">{expertProfile.employeeCount}명</div>
                        </div>
                      )}

                      {/* 전문 분야 */}
                      {expertProfile.specialties && expertProfile.specialties.length > 0 && (
                        <div>
                          <label className="block font-medium mb-1 text-gray-700 text-sm">전문 분야</label>
                          <div className="space-y-2">
                            {expertProfile.specialties.map((specialty, index) => (
                              <div key={index} className="rounded border border-gray-200 bg-gray-50 px-3 py-2">
                                <div className="font-medium text-gray-700 mb-1 text-sm">{specialty.specialty}</div>
                                <div className="flex flex-wrap gap-1">
                                  {specialty.detailFields.map((field, fieldIndex) => (
                                    <span key={fieldIndex} className="inline-block bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded">
                                      {field}
                                    </span>
                                  ))}
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>
                      )}

                      {/* 기술 스택 */}
                      {expertProfile.skills && expertProfile.skills.length > 0 && (
                        <div className="bg-gray-50 rounded border border-gray-200 p-3">
                          <label className="block font-medium mb-1 text-gray-700 text-sm">기술 스택</label>
                          <div className="flex flex-wrap gap-1">
                            {expertProfile.skills.map((skill, index) => (
                              <span key={index} className="inline-block bg-green-100 text-green-800 text-xs px-2 py-1 rounded">
                                {skill.name} ({skill.category})
                              </span>
                            ))}
                          </div>
                        </div>
                      )}

                      {/* 경력 사항 */}
                      {expertProfile.careers && expertProfile.careers.length > 0 && (
                        <div>
                          <label className="block font-medium mb-1 text-gray-700 text-sm">경력 사항</label>
                          <div className="space-y-1">
                            {expertProfile.careers.map((career, index) => (
                              <div key={index} className="rounded border border-gray-200 bg-gray-50 px-3 py-2 text-gray-700 text-sm">
                                {career}
                              </div>
                            ))}
                          </div>
                        </div>
                      )}

                      {/* 소셜 링크 */}
                      {(expertProfile.websiteUrl || expertProfile.facebookUrl || expertProfile.instagramUrl || expertProfile.xUrl) && (
                        <div>
                          <label className="block font-medium mb-1 text-gray-700 text-sm">소셜 링크</label>
                          <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                            {expertProfile.websiteUrl && (
                              <a href={expertProfile.websiteUrl} target="_blank" rel="noopener noreferrer" className="flex items-center gap-2 rounded border border-gray-200 bg-gray-50 px-3 py-2 text-blue-600 hover:text-blue-800 transition text-sm">
                                <Globe size={16} />
                                <span className="truncate">웹사이트</span>
                                <ExternalLink size={14} />
                              </a>
                            )}
                            {expertProfile.facebookUrl && (
                              <a href={expertProfile.facebookUrl} target="_blank" rel="noopener noreferrer" className="flex items-center gap-2 rounded border border-gray-200 bg-gray-50 px-3 py-2 text-blue-600 hover:text-blue-800 transition text-sm">
                                <Facebook size={16} />
                                <span className="truncate">Facebook</span>
                                <ExternalLink size={14} />
                              </a>
                            )}
                            {expertProfile.instagramUrl && (
                              <a href={expertProfile.instagramUrl} target="_blank" rel="noopener noreferrer" className="flex items-center gap-2 rounded border border-gray-200 bg-gray-50 px-3 py-2 text-blue-600 hover:text-blue-800 transition text-sm">
                                <Instagram size={16} />
                                <span className="truncate">Instagram</span>
                                <ExternalLink size={14} />
                              </a>
                            )}
                            {expertProfile.xUrl && (
                              <a href={expertProfile.xUrl} target="_blank" rel="noopener noreferrer" className="flex items-center gap-2 rounded border border-gray-200 bg-gray-50 px-3 py-2 text-blue-600 hover:text-blue-800 transition text-sm">
                                <Twitter size={16} />
                                <span className="truncate">X (Twitter)</span>
                                <ExternalLink size={14} />
                              </a>
                            )}
                          </div>
                        </div>
                      )}

                      {/* 콘텐츠 및 포트폴리오 */}
                      <div className="grid grid-cols-1 gap-4">
                        {/* 콘텐츠 */}
                        <div>
                          <label className="block font-medium mb-2 text-gray-700 text-sm">콘텐츠 ({expertProfile.contents ? expertProfile.contents.length : 0})</label>
                          {expertProfile.contents && expertProfile.contents.length > 0 ? (
                            <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
                              {expertProfile.contents.map((content) => (
                                <div key={content.contentId} className="relative rounded overflow-hidden group aspect-[4/3] bg-black">
                                  <img src={content.thumbnailUrl} alt={content.title} className="w-full h-full object-cover transition-transform duration-200 group-hover:scale-105" />
                                  <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/40 to-transparent" />
                                  <div className="absolute left-0 bottom-0 w-full p-2 flex flex-col">
                                    <span className="text-sm font-semibold text-white mb-1 truncate drop-shadow-lg">{content.title}</span>
                                    <span className="text-xs text-blue-200 font-medium drop-shadow-lg truncate">{content.category}</span>
                                  </div>
                                </div>
                              ))}
                            </div>
                          ) : (
                            <a href="/content/create" className="h-40 bg-white rounded-lg border-2 p-6 flex flex-col items-center justify-center border-dashed border-gray-200 text-gray-400 hover:border-blue-300 hover:text-blue-500 transition-all duration-200 cursor-pointer">
                              <div className="text-sm">등록된 항목이 없습니다</div>
                            </a>
                          )}
                        </div>

                        {/* 포트폴리오 */}
                        <div>
                          <label className="block font-medium mb-2 text-gray-700 text-sm">포트폴리오 ({expertProfile.portfolios ? expertProfile.portfolios.length : 0})</label>
                          {expertProfile.portfolios && expertProfile.portfolios.length > 0 ? (
                            <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
                              {expertProfile.portfolios.map((portfolio) => (
                                <div key={portfolio.portfolioId} className="relative rounded overflow-hidden group aspect-[4/3] bg-black">
                                  <img src={portfolio.thumbnailUrl} alt={portfolio.title} className="w-full h-full object-cover transition-transform duration-200 group-hover:scale-105" />
                                  <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/40 to-transparent" />
                                  <div className="absolute left-0 bottom-0 w-full p-2 flex flex-col">
                                    <span className="text-sm font-semibold text-white mb-1 truncate drop-shadow-lg">{portfolio.title}</span>
                                    <span className="text-xs text-blue-200 font-medium drop-shadow-lg truncate">{portfolio.category}</span>
                                  </div>
                                </div>
                              ))}
                            </div>
                          ) : (
                            <a href="/expert/portfolio/register" className="h-40 bg-white rounded-lg border-2 p-6 flex flex-col items-center justify-center border-dashed border-gray-200 text-gray-400 hover:border-blue-300 hover:text-blue-500 transition-all duration-200 cursor-pointer">
                              <div className="text-sm">등록된 항목이 없습니다</div>
                            </a>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : (
                  <div className="text-gray-400 text-center py-4">전문가 정보가 없습니다.</div>
                )}
              </div>
            )}
            {selectedTab === "review" && (
              <div>
                <h2 className="text-lg font-bold mb-2">리뷰</h2>
                {reviewLoading ? (
                  <div>리뷰 불러오는 중...</div>
                ) : reviews && reviews.length > 0 ? (
                  <div className="flex gap-4 flex-wrap">
                    {reviews.map((r) => (
                      <div key={r.reviewId} className="bg-gray-100 rounded p-4 w-72">
                        <div className="flex items-center gap-2 mb-2">
                          <img src={r.reviewerProfileImageUrl} alt="프로필" className="w-8 h-8 rounded-full border" />
                          <span className="font-semibold">{r.reviewerNickname}</span>
                          <span className="text-yellow-400 font-bold">★ {r.rating}</span>
                        </div>
                        <div className="text-gray-700 text-sm">{r.comment}</div>
                        <div className="text-xs text-gray-400 mt-1">{r.createdAt}</div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-gray-400">아직 리뷰가 없습니다.</div>
                )}
              </div>
            )}
          </div>
        </div>
        {/* 우측 사이드바 */}
        <aside className="w-full max-w-xs flex-shrink-0 bg-white rounded-lg shadow p-6 mt-2">
          {/* 가격 정보 */}
          <div className="mb-4">
            <div className="flex gap-2 border-b mb-2">        
              <span className="font-semibold text-black">가격 정보</span>  
            </div>
            <div className="text-2xl font-bold mb-1">
              {content.budget?.toLocaleString()}원 <span className="text-xs text-gray-400">(VAT 포함)</span>
            </div>
            <div className="text-gray-500 mb-2">{content.title}</div>
            <ul className="text-sm text-gray-700 mb-2 list-disc ml-4">
              {content.questions && content.questions.length > 0 ? (
                content.questions.map((question, index) => (
                  <li key={index}>{question.questionText}</li>
                ))
              ) : (
                <li>서비스 상세 정보가 없습니다.</li>
              )}
            </ul>
            <div className="text-xs text-gray-400 mb-2">관련파일 제공, 고해상도 파일 제공, 응용 디자인, 사이즈 이외 가능</div>
          </div>
          {userInfo && userInfo.email === content.expertEmail ? (
            // 본인이 작성한 콘텐츠인 경우 - 수정/삭제 버튼
            <>
              <button
                className="w-full bg-blue-500 py-2 rounded font-semibold mb-2 hover:bg-blue-600 transition-colors cursor-pointer text-white"
                onClick={() => {
                  navigate(`/content/edit/${id}`);
                }}
              >
                수정하기
              </button>
              <button
                className="w-full bg-red-500 py-2 rounded font-semibold mb-2 hover:bg-red-600 transition-colors cursor-pointer text-white"
                onClick={() => {
                  toast.custom(
                    (t) => (
                      <div className="bg-white border border-gray-200 rounded-lg shadow-lg p-4 max-w-sm">
                        <div className="flex items-center mb-3">
                          <div className="w-8 h-8 bg-red-100 rounded-full flex items-center justify-center mr-3">
                            <span className="text-red-600 text-lg">⚠️</span>
                          </div>
                          <div>
                            <h3 className="font-semibold text-gray-900">서비스 삭제</h3>
                            <p className="text-sm text-gray-600">정말로 이 서비스를 삭제하시겠습니까?</p>
                          </div>
                        </div>
                        <div className="flex gap-2">
                          <button
                            className="flex-1 bg-red-500 text-white py-2 px-4 rounded-md text-sm font-medium hover:bg-red-600 transition-colors"
                            onClick={() => {
                              toast.dismiss(t.id);
                              handleDeleteContent();
                            }}
                          >
                            삭제
                          </button>
                          <button
                            className="flex-1 bg-gray-300 text-gray-700 py-2 px-4 rounded-md text-sm font-medium hover:bg-gray-400 transition-colors"
                            onClick={() => toast.dismiss(t.id)}
                          >
                            취소
                          </button>
                        </div>
                      </div>
                    ),
                    {
                      duration: Infinity,
                      position: "top-center",
                    }
                  );
                }}
              >
                삭제하기
              </button>
            </>
          ) : (
            // 다른 사용자가 작성한 콘텐츠인 경우 - 문의/결제 버튼
            <>
              <button
                className="w-full bg-gray-100 py-2 rounded font-semibold mb-2 hover:bg-gray-200 transition-colors cursor-pointer"
                onClick={() => {
                  if (!userInfo) {
                    toast.error("로그인이 필요합니다.");
                    navigate("/auth/login");
                    return;
                  }
                  
                  // 본인이 작성한 콘텐츠인지 확인
                  if (userInfo.email === content.expertEmail) {
                    toast.error("본인이 작성한 서비스입니다.");
                    return;
                  }
                  
                  handleCreateChatRoom(content.expertEmail);            
                }}
              >
                전문가에게 문의하기
              </button>
              <button
                className="w-full bg-yellow-400 py-2 rounded font-bold cursor-pointer hover:bg-yellow-500 transition-colors"
                onClick={() => {
                  if (!userInfo) {
                    toast.error("로그인이 필요합니다.");
                    navigate("/auth/login");
                    return;
                  }
                  
                  // 본인이 작성한 콘텐츠인지 확인
                  if (userInfo.email === content.expertEmail) {
                    toast.error("본인이 작성한 서비스입니다.");
                    return;
                  }
                  
                  navigate(`/content/${id}/payment`);
                }}>
                결제하기
              </button>
            </>
          )}
          <div className="text-xs text-gray-400 mt-4">
            * 서비스 제공 완료 후 전문가에게 전달되니 안전하게 거래하세요.
            <br />
            * 결제 시 수수료 4.5%(VAT별도)가 추가됩니다.
            <br />* 이 전문가님은 세금계산서를 발행할 수 없어요.
          </div>
        </aside>
      </div>

      {/* 이미지 모달 */}
      <ImageModal
        isOpen={imageModalOpen}
        onClose={() => setImageModalOpen(false)}
        images={content?.imageUrls || []}
        currentIndex={currentImageIndex}
        onImageChange={setCurrentImageIndex}
      />

      {/* 공유 모달 */}
      <ShareModal
        isOpen={shareModalOpen}
        onClose={() => setShareModalOpen(false)}
        content={content}
      />
    </div>
  );
}

export default ContentDetailPage;
