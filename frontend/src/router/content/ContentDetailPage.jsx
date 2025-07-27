import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "../../lib/axios";
import axiosInstance from "../../lib/axios";

import { Star, ExternalLink, Users, GraduationCap, Calendar, Globe, Facebook, Instagram, Twitter, Plus } from "lucide-react";

// 리뷰 응답 예시
// {
//   totalRating: 4.5,
//   totalReviewCount: 2,
//   reviews: { content: [ ... ] }
// }

const TABS = [
  { key: "desc", label: "서비스 설명" },
  { key: "price", label: "가격 정보" },
  { key: "faq", label: "자주 묻는 질문" },
  { key: "expert", label: "전문가 정보" },
  { key: "review", label: "리뷰" },
];

function ContentDetailPage() {
  const { id } = useParams();
  const [content, setContent] = useState(null);
  const [selectedTab, setSelectedTab] = useState("portfolio");
  const [loading, setLoading] = useState(true);
  const [reviewLoading, setReviewLoading] = useState(false);
  const [reviews, setReviews] = useState([]);
  const [totalRating, setTotalRating] = useState(null);
  const [totalReviewCount, setTotalReviewCount] = useState(null);
  const [expertProfile, setExpertProfile] = useState(null);
  const [expertLoading, setExpertLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (!id) return;
    async function fetchData() {
      setLoading(true);
      try {
        const contentRes = await axiosInstance.get(`/api/content/${id}`);
        setContent(contentRes.data);
      } catch (err) {
        console.error("API 요청 에러:", err);
      }
      setLoading(false);
    }
    fetchData();
  }, [id]);

  //리뷰 요청
  const handleReviewRequest = async () => {
    if (!content) return;
    setReviewLoading(true);
    try {
      const res = await axiosInstance.get(`/api/reviews/${id}`);
      const data = res.data;
      setReviews(data.reviews?.content || []);
      setTotalRating(data.totalRating);
      setTotalReviewCount(data.totalReviewCount);
    } catch (err) {
      setReviews([]);
      setTotalRating(null);
      setTotalReviewCount(null);
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

  // 전문가 프로필 요청
  const handleExpertProfileRequest = async () => {
    if (!content || !content.expertId) return;
    setExpertLoading(true);
    try {
      const res = await axiosInstance.get(`/api/expert/profile/${content.expertId}`);
      setExpertProfile(res.data);
    } catch (err) {
      setExpertProfile(null);
      console.error("전문가 프로필 요청 에러:", err);
    }
    setExpertLoading(false);
  };

  if (loading || !content) {
    return <div className="flex justify-center items-center h-96">로딩 중...</div>;
  }

  return (
    <div className="w-full flex flex-col items-center mb-8">
      {/* 상단: 카테고리, 제목, 찜/공유/스크랩, 썸네일 */}
      <div className="w-full max-w-6xl">
        <div className="text-sm text-gray-400 mb-2">디자인 &gt; 로고 디자인</div>
        <div className="flex items-center gap-2 mb-2">
          <span className="bg-black text-white text-xs px-2 py-1 rounded">prime</span>
          <span className="text-3xl font-bold">{content.title}</span>
          <span className="ml-2 text-yellow-500 font-bold">★ 4.9</span>
          <span className="text-gray-400">(1446)</span>
          <span className="ml-2 text-gray-400">♡ 2,062</span>
          <span className="ml-2 text-gray-400 cursor-pointer">공유</span>
        </div>
        <div className="flex gap-4 mt-2">
          {/* 전문가 정보/문의 버튼 */}
          <div className="flex items-center gap-2 bg-white rounded-lg px-4 py-2 shadow">
            <img src="https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png" alt="프로필" className="w-10 h-10 rounded-full border" />
            <div>
              <div className="font-semibold">브랜딩보울</div>
              <div className="text-xs text-gray-400">연락 가능 시간: 10시~23시 | 평균 응답 시간: 10분 이내 | 세금계산서 발행 가능</div>
            </div>
            <button className="ml-4 bg-gray-100 px-4 py-2 rounded font-semibold">문의하기</button>
          </div>
          {/* 썸네일 */}
          <div className="w-80 h-48 bg-gray-200 rounded-lg flex items-center justify-center overflow-hidden">{content.contentUrl ? <img src={content.contentUrl} alt="대표 이미지" className="object-cover w-full h-full" /> : <span className="text-gray-400">썸네일 없음</span>}</div>
        </div>
        {/* 프라임/혜택 안내 */}
        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4 mt-6 flex items-center gap-4">
          <div className="bg-black text-white text-xs px-2 py-1 rounded">prime</div>
          <div className="text-sm">
            이 서비스는 크몽이 엄선한 상위 2% 전문가가 제공합니다.
            <br />
            <span className="text-blue-600 font-semibold">프리미엄의 고객 후기가 검증된 퀄리티</span> | <span className="text-blue-600 font-semibold">경력 이상의 인사이트를 담은 서비스</span> | <span className="text-blue-600 font-semibold">다양한 업종에 맞춘 맞춤 전문성</span>
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
                <h2 className="text-lg font-bold mb-2">서비스 설명</h2>
                <div className="whitespace-pre-line text-gray-800 bg-gray-50 p-4 rounded mb-4">{content.description}</div>
                <div className="mt-4 text-center text-lg font-bold">
                  <span className="text-blue-700">브랜딩보울</span>은 어떻게 <span className="text-blue-700">고객만족도 100%</span>를 유지할까요?
                  <br />
                  맘에 들지 않으면 <span className="text-blue-700">전액 환불</span> 해드리기 때문입니다.
                  <br />
                  미친 자신감을 가진 이유, <span className="text-blue-700">3분</span>이면 확인 가능합니다.
                </div>
                <div className="mt-4 text-center font-bold">
                  네이밍부터 로고까지 한번에!!!
                  <br />
                  상담만 하셔도,
                  <br />
                  "로고 기획 함께 고민 해드립니다"
                </div>
                <div className="mt-4 text-center font-bold">
                  2025.07.15 ~ 2025.07.31
                  <br />
                  30% 할인 이벤트 진행중
                </div>
                <div className="mt-4 text-center">
                  <span className="line-through text-gray-400 mr-2">디럭스 220,000원</span>
                  <span className="font-bold text-blue-700">179,000원</span>
                  <br />
                  <span className="line-through text-gray-400 mr-2">프리미엄 320,000원</span>
                  <span className="font-bold text-blue-700">260,000원</span>
                </div>
                <div className="mt-4 text-center text-xs text-blue-600 underline cursor-pointer">
                  상담하기
                  <br />
                  http://kmong.com/inbox/브랜딩보울
                </div>
              </div>
            )}
            {/* 나머지 탭은 기존과 동일하게 유지 */}
            {selectedTab === "price" && (
              <div>
                <h2 className="text-lg font-bold mb-2">가격 정보</h2>
                <div className="bg-gray-50 p-4 rounded">상세 가격 정보가 여기에 표시됩니다.</div>
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
              <span className="font-semibold text-gray-400">STANDARD</span>
              <span className="font-semibold text-black border-b-2 border-black">DELUXE</span>
              <span className="font-semibold text-gray-400">PREMIUM</span>
            </div>
            <div className="text-2xl font-bold mb-1">
              {content.budget?.toLocaleString()}원 <span className="text-xs text-gray-400">(VAT 포함)</span>
            </div>
            <div className="text-gray-500 mb-2">로고, 시그니처 + 명함 디자인, 슬로건</div>
            <ul className="text-sm text-gray-700 mb-2 list-disc ml-4">
              <li>시그니처(국/영문, 무제한수정)</li>
              <li>명함디자인</li>
              <li>+슬로건</li>
              <li>+추가옵션</li>
              <li>원본(모두제공), 저작/재산권 이전</li>
            </ul>
            <div className="text-xs text-gray-400 mb-2">관련파일 제공, 고해상도 파일 제공, 응용 디자인, 사이즈 이외 가능</div>
          </div>
          <button className="w-full bg-gray-100 py-2 rounded font-semibold mb-2">전문가에게 문의하기</button>
          <button className="w-full bg-yellow-400 py-2 rounded font-bold" onClick={() => navigate(`/content/${id}/payment`)}>
            결제하기
          </button>
          <div className="text-xs text-gray-400 mt-4">
            * 서비스 제공 완료 후 전문가에게 전달되니 안전하게 거래하세요.
            <br />
            * 결제 시 수수료 4.5%(VAT별도)가 추가됩니다.
            <br />* 이 전문가님은 세금계산서를 발행할 수 없어요.
          </div>
        </aside>
      </div>
    </div>
  );
}

export default ContentDetailPage;
