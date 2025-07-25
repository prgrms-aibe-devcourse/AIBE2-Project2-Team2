import { useEffect, useState } from "react";
import { Star, ExternalLink, Users, GraduationCap, Calendar, Globe, Facebook, Instagram, Twitter, Plus } from "lucide-react";
import axiosInstance from "../../../lib/axios";
import { Link } from "react-router-dom";

export default function Profile() {
  const [userInfo, setUserInfo] = useState({
    profileImageUrl: "",
    nickname: "",
    introduction: "",
    region: "",
    totalCareerYears: 0,
    education: "",
    employeeCount: 0,
    websiteUrl: "",
    facebookUrl: "",
    instagramUrl: "",
    xUrl: "",
    reviewCount: 0,
    averageScore: 0,
    specialties: [],
    skills: [],
    careers: [],
    contents: [],
    portfolios: [],
  });

  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    axiosInstance
        .get("/api/expert/profile")
        .then((res) => {
          setUserInfo(res.data);
        })
        .catch((err) => {
          console.error("추가 정보 불러오기 실패:", err);
        })
        .finally(() => setIsLoading(false));
  }, []);

  const renderStars = (score) => {
    const stars = [];
    const fullStars = Math.floor(score);
    const hasHalfStar = score % 1 !== 0;

    for (let i = 0; i < fullStars; i++) {
      stars.push(<Star key={i} size={16} className="text-yellow-400 fill-current" />);
    }

    if (hasHalfStar) {
      stars.push(<Star key="half" size={16} className="text-yellow-400 fill-current opacity-50" />);
    }

    const remainingStars = 5 - Math.ceil(score);
    for (let i = 0; i < remainingStars; i++) {
      stars.push(<Star key={`empty-${i}`} size={16} className="text-gray-300" />);
    }

    return stars;
  };

  if (isLoading) {
    return <div className="text-center py-10 text-gray-400">로딩 중...</div>;
  }

  return (
      <div className="min-h-screen bg-white py-16 px-6">
        <div className="max-w-4xl mx-auto bg-white rounded-lg shadow-md border border-gray-100 p-10">
          <div className="mb-8 flex items-center justify-between">
            <h1 className="text-2xl font-semibold  text-gray-800">전문가 프로필 관리</h1>
            <Link to={`/expert/profile/edit`} className="text-sm text-blue-600 hover:underline">
              프로필 수정
            </Link>
          </div>

          <div className="flex gap-14">
            {/* 좌측: 프로필 이미지 및 기본 정보 */}
            {/* <div className="flex flex-col items-center space-y-5">
            <div className="relative w-36 h-36 rounded-full bg-gray-100 flex items-center justify-center overflow-hidden border border-gray-200">
              <img src={userInfo.profileImageUrl || "/api/placeholder/150/150"} alt="프로필 이미지" className="w-full h-full object-cover object-center" />
            </div>
            <div className="text-center">
              <h2 className="text-lg font-semibold text-gray-800 mb-1">{userInfo.nickname}</h2>
              {userInfo.reviewCount > 0 && (
                <div className="flex items-center justify-center gap-2 mb-2">
                  <div className="flex items-center gap-1">{renderStars(userInfo.averageScore)}</div>
                  <span className="text-sm text-gray-600">
                    {userInfo.averageScore} ({userInfo.reviewCount}개 리뷰)
                  </span>
                </div>
              )}
              {userInfo.region && (
                <div className="flex items-center justify-center gap-1 text-sm text-gray-600">
                  <MapPin size={14} />
                  <span>{userInfo.region}</span>
                </div>
              )}
            </div>
          </div> */}

            {/* 우측: 상세 정보 */}
            <div className="flex-1 space-y-8">
              {/* 자기소개 */}
              {userInfo.introduction && (
                  <div>
                    <label className="block font-semibold mb-2 text-gray-700">자기소개</label>
                    <div className="w-full rounded-md border border-gray-200 bg-gray-50 px-4 py-3 text-gray-700 min-h-[80px]">{userInfo.introduction}</div>
                  </div>
              )}

              {/* 경력 및 교육 */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {userInfo.totalCareerYears > 0 && (
                    <div>
                      <label className="block font-semibold mb-2 text-gray-700">
                        <Calendar size={16} className="inline mr-1" />총 경력
                      </label>
                      <input type="text" value={`${userInfo.totalCareerYears}년`} disabled className="w-full rounded-md border border-gray-200 bg-gray-50 px-4 py-3 cursor-not-allowed text-gray-500" />
                    </div>
                )}

                {userInfo.education && (
                    <div>
                      <label className="block font-semibold mb-2 text-gray-700">
                        <GraduationCap size={16} className="inline mr-1" />
                        학력
                      </label>
                      <input type="text" value={userInfo.education} disabled className="w-full rounded-md border border-gray-200 bg-gray-50 px-4 py-3 cursor-not-allowed text-gray-500" />
                    </div>
                )}
              </div>

              {/* 직원 수 */}
              {userInfo.employeeCount > 0 && (
                  <div>
                    <label className="block font-semibold mb-2 text-gray-700">
                      <Users size={16} className="inline mr-1" />
                      직원 수
                    </label>
                    <input type="text" value={`${userInfo.employeeCount}명`} disabled className="w-full rounded-md border border-gray-200 bg-gray-50 px-4 py-3 cursor-not-allowed text-gray-500" />
                  </div>
              )}

              {/* 전문 분야 */}
              {userInfo.specialties.length > 0 && (
                  <div>
                    <label className="block font-semibold mb-2 text-gray-700">전문 분야</label>
                    <div className="space-y-3">
                      {userInfo.specialties.map((specialty, index) => (
                          <div key={index} className="rounded-md border border-gray-200 bg-gray-50 px-4 py-3">
                            <div className="font-medium text-gray-700 mb-2">{specialty.specialty}</div>
                            <div className="flex flex-wrap gap-2">
                              {specialty.detailFields.map((field, fieldIndex) => (
                                  <span key={fieldIndex} className="inline-block bg-blue-100 text-blue-800 text-sm px-2 py-1 rounded-md">
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
              {userInfo.skills.length > 0 && (
                  <div>
                    <label className="block font-semibold mb-2 text-gray-700">기술 스택</label>
                    <div className="flex flex-wrap gap-2">
                      {userInfo.skills.map((skill, index) => (
                          <span key={index} className="inline-block bg-green-100 text-green-800 text-sm px-3 py-1 rounded-full">
                      {skill.name} ({skill.category})
                    </span>
                      ))}
                    </div>
                  </div>
              )}

              {/* 경력 사항 */}
              {userInfo.careers.length > 0 && (
                  <div>
                    <label className="block font-semibold mb-2 text-gray-700">경력 사항</label>
                    <div className="space-y-2">
                      {userInfo.careers.map((career, index) => (
                          <div key={index} className="rounded-md border border-gray-200 bg-gray-50 px-4 py-3 text-gray-700">
                            {career}
                          </div>
                      ))}
                    </div>
                  </div>
              )}

              {/* 소셜 링크 */}
              <div>
                <label className="block font-semibold mb-2 text-gray-700">소셜 링크</label>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {userInfo.websiteUrl && (
                      <div>
                        <label className="block text-sm font-medium mb-1 text-gray-600">
                          <Globe size={14} className="inline mr-1" />
                          웹사이트
                        </label>
                        <a href={userInfo.websiteUrl} target="_blank" rel="noopener noreferrer" className="flex items-center gap-2 w-full rounded-md border border-gray-200 bg-gray-50 px-4 py-3 text-blue-600 hover:text-blue-800 transition">
                          <span className="truncate">{userInfo.websiteUrl}</span>
                          <ExternalLink size={14} />
                        </a>
                      </div>
                  )}

                  {userInfo.facebookUrl && (
                      <div>
                        <label className="block text-sm font-medium mb-1 text-gray-600">
                          <Facebook size={14} className="inline mr-1" />
                          Facebook
                        </label>
                        <a href={userInfo.facebookUrl} target="_blank" rel="noopener noreferrer" className="flex items-center gap-2 w-full rounded-md border border-gray-200 bg-gray-50 px-4 py-3 text-blue-600 hover:text-blue-800 transition">
                          <span className="truncate">{userInfo.facebookUrl}</span>
                          <ExternalLink size={14} />
                        </a>
                      </div>
                  )}

                  {userInfo.instagramUrl && (
                      <div>
                        <label className="block text-sm font-medium mb-1 text-gray-600">
                          <Instagram size={14} className="inline mr-1" />
                          Instagram
                        </label>
                        <a href={userInfo.instagramUrl} target="_blank" rel="noopener noreferrer" className="flex items-center gap-2 w-full rounded-md border border-gray-200 bg-gray-50 px-4 py-3 text-blue-600 hover:text-blue-800 transition">
                          <span className="truncate">{userInfo.instagramUrl}</span>
                          <ExternalLink size={14} />
                        </a>
                      </div>
                  )}

                  {userInfo.xUrl && (
                      <div>
                        <label className="block text-sm font-medium mb-1 text-gray-600">
                          <Twitter size={14} className="inline mr-1" />X (Twitter)
                        </label>
                        <a href={userInfo.xUrl} target="_blank" rel="noopener noreferrer" className="flex items-center gap-2 w-full rounded-md border border-gray-200 bg-gray-50 px-4 py-3 text-blue-600 hover:text-blue-800 transition">
                          <span className="truncate">{userInfo.xUrl}</span>
                          <ExternalLink size={14} />
                        </a>
                      </div>
                  )}
                </div>
              </div>

              {/* 콘텐츠 및 포트폴리오 */}
              <div className="grid grid-cols-1 gap-8">
                {/* 콘텐츠 */}
                <div>
                  <div className="flex items-center justify-between mb-4">
                    <label className="block font-semibold text-gray-700">콘텐츠 ({userInfo.contents.length})</label>
                    <Link
                        to="/content/register"
                        className="flex items-center gap-1 text-sm text-blue-600 hover:text-blue-800 hover:underline transition"
                    >
                      <Plus size={14} />
                      등록하기
                    </Link>
                  </div>
                  <div className="space-y-3">
                    {userInfo.contents.length === 0 ? (
                        <Link
                            to="/expert/content/register"
                            className="bg-white rounded-lg border-2 p-6 flex flex-col items-center justify-center border-dashed border-gray-200 text-gray-400 hover:border-blue-300 hover:text-blue-500 transition-all duration-200 cursor-pointer"
                        >
                          <svg className="w-10 h-10 mb-2" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
                          </svg>
                          <div className="text-sm">등록된 항목이 없습니다</div>
                          <div className="text-xs mt-1">클릭하여 콘텐츠를 등록하세요</div>
                        </Link>
                    ) : (
                        <>
                          {userInfo.contents.slice(0, 3).map((content, idx) => (
                              <div key={content.contentId} className="bg-white rounded-lg border-2 p-3 flex gap-3 items-center transition-all duration-200 border-gray-200 hover:border-blue-400">
                                <div className="flex-shrink-0">
                                  <img src={content.thumbnailUrl} alt={content.title} className="w-16 h-16 object-cover rounded-lg border border-gray-200" />
                                </div>
                                <div className="flex-1 min-w-0">
                                  <div className="text-sm font-medium text-gray-900 truncate">{content.title}</div>
                                  <div className="text-xs text-gray-500 mt-1">{content.category}</div>
                                </div>
                              </div>
                          ))}
                          {userInfo.contents.length > 3 && <div className="text-center text-sm text-gray-500">+{userInfo.contents.length - 3}개 더 보기</div>}
                        </>
                    )}
                  </div>
                </div>

                {/* 포트폴리오 */}
                <div>
                  <div className="flex items-center justify-between mb-4">
                    <label className="block font-semibold text-gray-700">포트폴리오 ({userInfo.portfolios.length})</label>
                    <Link
                        to="/expert/portfolio/create"
                        className="flex items-center gap-1 text-sm text-blue-600 hover:text-blue-800 hover:underline transition"
                    >
                      <Plus size={14} />
                      등록하기
                    </Link>
                  </div>
                  <div className="space-y-3">
                    {userInfo.portfolios.length === 0 ? (
                        <Link
                            to="/expert/portfolio/register"
                            className="bg-white rounded-lg border-2 p-6 flex flex-col items-center justify-center border-dashed border-gray-200 text-gray-400 hover:border-blue-300 hover:text-blue-500 transition-all duration-200 cursor-pointer"
                        >
                          <svg className="w-10 h-10 mb-2" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
                          </svg>
                          <div className="text-sm">등록된 항목이 없습니다</div>
                          <div className="text-xs mt-1">클릭하여 포트폴리오를 등록하세요</div>
                        </Link>
                    ) : (
                        <>
                          {userInfo.portfolios.slice(0, 3).map((portfolio, idx) => (
                              <div key={portfolio.portfolioId} className="bg-white rounded-lg border-2 p-3 flex gap-3 items-center transition-all duration-200 border-gray-200 hover:border-blue-400">
                                <div className="flex-shrink-0">
                                  <img src={portfolio.thumbnailUrl} alt={portfolio.title} className="w-16 h-16 object-cover rounded-lg border border-gray-200" />
                                </div>
                                <div className="flex-1 min-w-0">
                                  <div className="text-sm font-medium text-gray-900 truncate">{portfolio.title}</div>
                                </div>
                              </div>
                          ))}
                          {userInfo.portfolios.length > 3 && <div className="text-center text-sm text-gray-500">+{userInfo.portfolios.length - 3}개 더 보기</div>}
                        </>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
  );
}