import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { useParams, useNavigate } from "react-router-dom";
import axiosInstance from "../../../lib/axios";
import { useUserInfoStore } from "../../../store/userInfo.js";

export default function PortfolioEdit() {
  const { id } = useParams();
  const navigate = useNavigate();

  // 유저 정보 가져오기
  const { userInfo } = useUserInfoStore();

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [category, setCategory] = useState("");
  const [workingYear, setWorkingYear] = useState("");
  const [images, setImages] = useState([]);
  const [selectedThumbnailIndex, setSelectedThumbnailIndex] = useState(null);
  const [loading, setLoading] = useState(true);
  const [portfolioData, setPortfolioData] = useState(null); // 포트폴리오 데이터 저장

  const currentYear = new Date().getFullYear();
  const yearOptions = [];
  for (let y = currentYear; y >= 2000; y--) {
    yearOptions.push(y);
  }

  const [metaData, setMetaData] = useState(null);
  const [selectedSpecialty, setSelectedSpecialty] = useState("");
  const [detailOptions, setDetailOptions] = useState([]);
  const [errors, setErrors] = useState({});

  // 파일 업로드 조건
  const ALLOWED_TYPES = ["image/png", "image/jpg", "image/jpeg"];
  const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
  const MAX_TOTAL_SIZE = 20 * 1024 * 1024; // 20MB

  // 메타데이터 로딩
  useEffect(() => {
    const fetchMetaData = async () => {
      try {
        const response = await axiosInstance.get("/api/expert/meta");
        setMetaData(response.data);
      } catch (err) {
        console.error("메타데이터 로딩 실패", err);
      }
    };

    fetchMetaData();
  }, []);

  // 포트폴리오 데이터 로딩 및 권한 체크
  useEffect(() => {
    const fetchPortfolio = async () => {
      try {
        const response = await axiosInstance.get(`/api/expert/portfolio/${id}`);
        const data = response.data;

        // 포트폴리오 데이터 저장
        setPortfolioData(data);

        // 권한 체크: 현재 로그인한 사용자가 작성자인지 확인
        if (!userInfo || !data.author || userInfo.id !== data.author.id) {
          toast.error("본인이 작성한 포트폴리오만 수정 가능합니다.");
          navigate(`/expert/portfolio/${id}`);
          return;
        }

        setTitle(data.title);
        setContent(data.content);
        setCategory(data.category);
        setWorkingYear(data.workingYear.toString());

        // 기존 이미지들을 등록폼과 같은 형태로 변환
        const existingImages = await Promise.all(
          (data.images || []).map(async (image, index) => ({
            file: null, // 기존 이미지는 파일 객체가 없음
            preview: image.url, // 서버에서 받은 이미지 URL
            id: image.id, // 기존 이미지 ID
            isExisting: true, // 기존 이미지임을 표시
          }))
        );

        setImages(existingImages);

        // 썸네일 이미지 찾기
        if (data.thumbnailImage) {
          const thumbnailIndex = existingImages.findIndex((img) => img.id === data.thumbnailImage.id);
          if (thumbnailIndex !== -1) {
            setSelectedThumbnailIndex(thumbnailIndex);
          }
        }

        setLoading(false);
      } catch (error) {
        console.error("포트폴리오 조회 실패:", error);
        if (error.response && error.response.status === 403) {
          toast.error("수정 권한이 없습니다.");
        } else if (error.response && error.response.status === 404) {
          toast.error("존재하지 않는 포트폴리오입니다.");
        } else {
          toast.error("포트폴리오를 불러올 수 없습니다.");
        }
        navigate("/expert/profile");
      }
    };

    // 로그인 상태 체크
    if (!userInfo) {
      toast.error("로그인이 필요합니다.");
      navigate("/login");
      return;
    }

    if (id) {
      fetchPortfolio();
    }
  }, [id, navigate, userInfo]);

  // 메타데이터 로딩 후 전문분야 설정
  useEffect(() => {
    if (metaData && category) {
      const foundSpecialty = metaData.detailFields.find((item) => item.detailFields.includes(category));
      if (foundSpecialty) {
        setSelectedSpecialty(foundSpecialty.specialty);
      }
    }
  }, [metaData, category]);

  // 전문분야 변경시 세부분야 옵션 업데이트
  useEffect(() => {
    if (metaData && selectedSpecialty) {
      const found = metaData.detailFields.find((d) => d.specialty === selectedSpecialty);
      setDetailOptions(found ? found.detailFields : []);
      // 기존 카테고리가 새로운 전문분야에 없다면 초기화하지 않음 (수정폼이므로)
    } else {
      setDetailOptions([]);
    }
  }, [selectedSpecialty, metaData]);

  // 이미지 파일들을 URL로 변환하는 함수
  const createImagePreview = (file) => {
    return new Promise((resolve) => {
      const reader = new FileReader();
      reader.onload = (e) => resolve(e.target.result);
      reader.readAsDataURL(file);
    });
  };

  // 이미지 추가 처리
  const handleAddImages = async (e) => {
    const files = Array.from(e.target.files || []);
    if (files.length === 0) return;

    // 최대 4개 체크
    if (images.length + files.length > 5) {
      toast.error("포트폴리오 이미지는 최대 5개까지 추가할 수 있습니다.");
      e.target.value = null;
      return;
    }

    // 파일 유효성 검사
    for (const file of files) {
      if (!ALLOWED_TYPES.includes(file.type)) {
        toast.error("이미지 형식은 PNG, JPG, JPEG만 가능합니다.");
        e.target.value = null;
        return;
      }
      if (file.size > MAX_FILE_SIZE) {
        toast.error("각 이미지 파일은 10MB 이하만 가능합니다.");
        e.target.value = null;
        return;
      }
    }

    // 총 용량 체크 (새로운 파일들만)
    const newFilesTotalSize = files.reduce((acc, cur) => acc + cur.size, 0);
    const existingNewFilesSize = images.filter((img) => !img.isExisting && img.file).reduce((acc, cur) => acc + cur.file.size, 0);

    if (existingNewFilesSize + newFilesTotalSize > MAX_TOTAL_SIZE) {
      toast.error("새로 추가하는 이미지 총 용량은 20MB 이하만 가능합니다.");
      e.target.value = null;
      return;
    }

    // 미리보기 URL 생성
    const newImages = await Promise.all(
      files.map(async (file) => ({
        file,
        preview: await createImagePreview(file),
        id: Date.now() + Math.random(),
        isExisting: false,
      }))
    );

    setImages([...images, ...newImages]);

    // 첫 번째 이미지가 추가될 때 자동으로 썸네일로 설정
    if (images.length === 0 && newImages.length > 0) {
      setSelectedThumbnailIndex(0);
    }

    e.target.value = null;
  };

  // 이미지 삭제
  const removeImage = (indexToRemove) => {
    const newImages = images.filter((_, i) => i !== indexToRemove);
    setImages(newImages);

    // 삭제된 이미지가 썸네일이었다면 썸네일 선택 해제하거나 첫 번째로 변경
    if (selectedThumbnailIndex === indexToRemove) {
      if (newImages.length > 0) {
        setSelectedThumbnailIndex(0);
      } else {
        setSelectedThumbnailIndex(null);
      }
    } else if (selectedThumbnailIndex > indexToRemove) {
      setSelectedThumbnailIndex(selectedThumbnailIndex - 1);
    }
  };

  // 썸네일 선택
  const selectThumbnail = (index) => {
    setSelectedThumbnailIndex(index);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // 다시 한 번 권한 체크 (안전장치)
    if (!userInfo || !portfolioData || userInfo.id !== portfolioData.author.id) {
      toast.error("수정 권한이 없습니다.");
      return;
    }

    // 에러 상태 초기화
    setErrors({});
    const newErrors = {};

    // 필수값 체크
    if (!title.trim()) newErrors.title = true;
    if (!content.trim()) newErrors.content = true;
    if (!selectedSpecialty) newErrors.selectedSpecialty = true;
    if (!category) newErrors.category = true;
    if (!workingYear) newErrors.workingYear = true;
    if (images.length < 1) newErrors.images = true;
    if (selectedThumbnailIndex === null) newErrors.thumbnail = true;

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      toast.error("모든 필드를 빠짐없이 입력해주세요.");

      // 3초 후 에러 스타일 제거
      setTimeout(() => {
        setErrors({});
      }, 3000);
      return;
    }

    // 폼데이터 생성
    const formData = new FormData();
    formData.append("title", title);
    formData.append("content", content);
    formData.append("category", category);
    formData.append("workingYear", workingYear);

    // 유지할 기존 이미지 ID들 추가
    const remainingImageIds = images.filter((img) => img.isExisting).map((img) => img.id);
    remainingImageIds.forEach((id) => formData.append("remainingImageIds", id));

    // 새로운 이미지들 추가
    const newImageFiles = images.filter((img) => !img.isExisting && img.file);
    newImageFiles.forEach((imageObj) => {
      formData.append("images", imageObj.file);
    });

    // 썸네일 처리
    const selectedImage = images[selectedThumbnailIndex];
    if (selectedImage.isExisting) {
      // 기존 이미지를 썸네일로 선택한 경우
      formData.append("thumbnailRemainImageId", selectedImage.id);
    } else {
      // 새로운 이미지를 썸네일로 선택한 경우
      formData.append("thumbnail", selectedImage.file);
    }

    try {
      await axiosInstance.put(`/api/expert/portfolio/${id}`, formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });
      toast.success("수정 완료");
      navigate(`/expert/portfolio/${id}`);
    } catch (error) {
      console.error("수정 실패", error);
      if (error.response && error.response.status === 403) {
        toast.error("수정 권한이 없습니다.");
      } else {
        toast.error("수정 실패");
      }
    }
  };

  // 로그인하지 않은 경우 처리
  if (!userInfo) {
    return (
      <div className="bg-gradient-to-br from-slate-50 to-blue-50 py-8 px-4">
        <div className="max-w-6xl mx-auto">
          <div className="text-center py-20">
            <p className="text-gray-600">로그인이 필요합니다.</p>
          </div>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="bg-gradient-to-br from-slate-50 to-blue-50 py-8 px-4">
        <div className="max-w-6xl mx-auto">
          <div className="text-center py-20">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-4 text-gray-600">포트폴리오를 불러오는 중...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-gradient-to-br from-slate-50 to-blue-50 py-8 px-4">
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-6">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">포트폴리오 수정</h1>
          <p className="text-gray-600">작품 정보를 수정하세요</p>
          {portfolioData && (
            <p className="text-sm text-gray-500 mt-2">
              작성자: {portfolioData.author.name} ({portfolioData.author.email})
            </p>
          )}
        </div>

        <div className="bg-white rounded-2xl shadow-xl backdrop-blur-sm border border-white/20 overflow-hidden">
          <div className="p-6">
            <div className="space-y-6">
              {/* 첫 번째 행 - 제목, 작업연도, 전문분야, 세부분야 */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="space-y-2">
                  <label className="block text-sm font-semibold text-gray-700">
                    제목 <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    placeholder="예: 모던 아파트 인테리어"
                    className={`w-full px-3 py-2.5 rounded-lg border-2 transition-all duration-200 focus:outline-none focus:ring-0 text-sm ${errors.title ? "border-red-400 bg-red-50 focus:border-red-500" : "border-gray-200 focus:border-blue-500 hover:border-gray-300"}`}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <label className="block text-sm font-semibold text-gray-700">
                    작업 연도 <span className="text-red-500">*</span>
                  </label>
                  <select
                    value={workingYear}
                    onChange={(e) => setWorkingYear(e.target.value)}
                    className={`w-full px-3 py-2.5 rounded-lg border-2 transition-all duration-200 focus:outline-none focus:ring-0 text-sm ${errors.workingYear ? "border-red-400 bg-red-50 focus:border-red-500" : "border-gray-200 focus:border-blue-500 hover:border-gray-300"}`}
                    required>
                    <option value="">연도 선택</option>
                    {yearOptions.map((year) => (
                      <option key={year} value={year}>
                        {year}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="space-y-2">
                  <label className="block text-sm font-semibold text-gray-700">
                    전문 분야 <span className="text-red-500">*</span>
                  </label>
                  <select
                    value={selectedSpecialty}
                    onChange={(e) => setSelectedSpecialty(e.target.value)}
                    className={`w-full px-3 py-2.5 rounded-lg border-2 transition-all duration-200 focus:outline-none focus:ring-0 text-sm ${errors.selectedSpecialty ? "border-red-400 bg-red-50 focus:border-red-500" : "border-gray-200 focus:border-blue-500 hover:border-gray-300"}`}
                    required>
                    <option value="">선택하세요</option>
                    {metaData?.detailFields.map((item) => (
                      <option key={item.specialty} value={item.specialty}>
                        {item.specialty}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="space-y-2">
                  <label className="block text-sm font-semibold text-gray-700">
                    세부 분야 <span className="text-red-500">*</span>
                  </label>
                  <select
                    value={category}
                    onChange={(e) => setCategory(e.target.value)}
                    className={`w-full px-3 py-2.5 rounded-lg border-2 transition-all duration-200 focus:outline-none focus:ring-0 text-sm ${
                      errors.category ? "border-red-400 bg-red-50 focus:border-red-500" : !selectedSpecialty ? "border-gray-200 bg-gray-100 cursor-not-allowed" : "border-gray-200 focus:border-blue-500 hover:border-gray-300"
                    }`}
                    required
                    disabled={!selectedSpecialty}>
                    <option value="">{!selectedSpecialty ? "먼저 전문 분야를 선택하세요" : "선택하세요"}</option>
                    {detailOptions.map((field) => (
                      <option key={field} value={field}>
                        {field}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* 두 번째 행 - 내용 */}
              <div className="space-y-2">
                <label className="block text-sm font-semibold text-gray-700">
                  내용 <span className="text-red-500">*</span>
                </label>
                <textarea
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  placeholder="예: 35평 아파트의 거실과 주방을 개방형으로 리모델링했습니다. 모던하고 심플한 디자인으로 공간 활용도를 극대화했으며, 자연광을 최대한 활용할 수 있도록 설계했습니다."
                  className={`w-full px-3 py-2.5 rounded-lg border-2 transition-all duration-200 focus:outline-none focus:ring-0 h-20 resize-none text-sm ${errors.content ? "border-red-400 bg-red-50 focus:border-red-500" : "border-gray-200 focus:border-blue-500 hover:border-gray-300"}`}
                  required
                />
              </div>

              {/* 세 번째 행 - 이미지 업로드 */}
              <div className="bg-gray-50 rounded-xl p-6">
                <h3 className="text-base font-semibold text-gray-800 flex items-center mb-4">
                  <svg className="w-5 h-5 mr-2 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clipRule="evenodd" />
                  </svg>
                  포트폴리오 이미지 수정
                </h3>

                {/* 이미지 추가 버튼 */}
                {images.length < 4 && (
                  <div className="mb-6">
                    <input
                      type="file"
                      accept="image/png,image/jpg,image/jpeg"
                      multiple
                      onChange={handleAddImages}
                      className={`w-full px-4 py-3 rounded-lg border-2 border-dashed transition-all duration-200 focus:outline-none text-sm ${errors.images ? "border-red-400 bg-red-50" : "border-gray-300 hover:border-blue-400 focus:border-blue-500 bg-white"}`}
                    />
                    <p className="text-xs text-gray-500 mt-2">PNG, JPG, JPEG 형식 | 최대 5개 | 각 파일 10MB 이하 | 새 파일 총 용량 20MB 이하</p>
                  </div>
                )}

                {/* 업로드된 이미지들 */}
                {images.length > 0 && (
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium text-gray-700">이미지 목록 ({images.length}/4)</span>
                      {selectedThumbnailIndex !== null && <span className="text-xs text-blue-600 bg-blue-100 px-2 py-1 rounded-full">썸네일: {selectedThumbnailIndex + 1}번 이미지</span>}
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      {images.map((imageObj, index) => (
                        <div key={imageObj.id} className={`bg-white rounded-lg border-2 p-3 transition-all duration-200 ${selectedThumbnailIndex === index ? "border-blue-500 shadow-md" : "border-gray-200 hover:border-gray-300"}`}>
                          <div className="flex gap-3">
                            {/* 이미지 미리보기 */}
                            <div className="flex-shrink-0">
                              <img src={imageObj.preview} alt={`Preview ${index + 1}`} className="w-16 h-16 object-cover rounded-lg border border-gray-200" />
                            </div>

                            {/* 이미지 정보 및 컨트롤 */}
                            <div className="flex-1 min-w-0">
                              <div className="flex items-start justify-between mb-2">
                                <div className="min-w-0 flex-1">
                                  <p className="text-sm font-medium text-gray-900 truncate">
                                    {imageObj.isExisting ? (
                                      <span className="flex items-center">
                                        <span className="w-2 h-2 bg-green-500 rounded-full mr-2"></span>
                                        기존 이미지 {index + 1}
                                      </span>
                                    ) : (
                                      <span className="flex items-center">
                                        <span className="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
                                        {imageObj.file.name}
                                      </span>
                                    )}
                                  </p>
                                  {!imageObj.isExisting && <p className="text-xs text-gray-500">{(imageObj.file.size / 1024 / 1024).toFixed(2)} MB</p>}
                                </div>
                                <button type="button" onClick={() => removeImage(index)} className="ml-2 w-6 h-6 rounded-full bg-red-100 hover:bg-red-200 text-red-600 flex items-center justify-center transition-colors text-xs flex-shrink-0">
                                  ✕
                                </button>
                              </div>

                              {/* 썸네일 선택 버튼 */}
                              <button type="button" onClick={() => selectThumbnail(index)} className={`text-xs px-2 py-1 rounded-full transition-all duration-200 ${selectedThumbnailIndex === index ? "bg-blue-500 text-white" : "bg-gray-100 text-gray-600 hover:bg-blue-100 hover:text-blue-700"}`}>
                                {selectedThumbnailIndex === index ? <span className="flex items-center">⭐ 썸네일</span> : "썸네일로 설정"}
                              </button>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>

                    {/* 썸네일 선택 안내 */}
                    {images.length > 0 && selectedThumbnailIndex === null && (
                      <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3">
                        <p className="text-sm text-yellow-800">
                          <span className="font-medium">⚠️ 썸네일을 선택해주세요!</span>
                          <br />
                          썸네일은 포트폴리오 목록에서 대표 이미지로 표시됩니다.
                        </p>
                      </div>
                    )}
                  </div>
                )}
              </div>

              {/* 수정 버튼 */}
              <div className="pt-2 flex space-x-4">
                <button type="button" onClick={() => navigate(`/portfolio/${id}`)} className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-700 font-semibold py-3 px-6 rounded-xl transition-all duration-200 focus:outline-none focus:ring-4 focus:ring-gray-200">
                  취소
                </button>
                <button
                  type="button"
                  onClick={handleSubmit}
                  className="flex-1 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white font-semibold py-3 px-6 rounded-xl transition-all duration-200 transform hover:scale-[1.01] focus:outline-none focus:ring-4 focus:ring-blue-200 shadow-lg">
                  수정 완료
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
