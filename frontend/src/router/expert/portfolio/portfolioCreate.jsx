import { useEffect, useState } from "react";
import axiosInstance from "../../../lib/axios";
import { useNavigate } from "react-router-dom";

export default function PortfolioCreate() {
    const [title, setTitle] = useState("");
    const [content, setContent] = useState("");
    const [category, setCategory] = useState("");
    const [workingYear, setWorkingYear] = useState("");
    const [images, setImages] = useState([]);
    const [thumbnail, setThumbnail] = useState(null);

    const currentYear = new Date().getFullYear();
    const yearOptions = [];
    for (let y = currentYear; y >= 2000; y--) {
        yearOptions.push(y);
    }

    const navigate = useNavigate();

    const [metaData, setMetaData] = useState(null);
    const [selectedSpecialty, setSelectedSpecialty] = useState("");
    const [detailOptions, setDetailOptions] = useState([]);
    const [errors, setErrors] = useState({});

    // 파일 업로드 조건
    const ALLOWED_TYPES = ["image/png", "image/jpg", "image/jpeg"];
    const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    const MAX_TOTAL_SIZE = 20 * 1024 * 1024; // 20MB

    useEffect(() => {
        axiosInstance
            .get("/api/expert/meta")
            .then((res) => {
                setMetaData(res.data);
            })
            .catch((err) => {
                console.error("메타데이터 로딩 실패", err);
            });
    }, []);

    useEffect(() => {
        if (selectedSpecialty && metaData) {
            const found = metaData.detailFields.find(
                (d) => d.specialty === selectedSpecialty
            );
            setDetailOptions(found ? found.detailFields : []);
            setCategory(""); // specialty 바뀌면 category 초기화
        } else {
            setDetailOptions([]);
            setCategory("");
        }
    }, [selectedSpecialty, metaData]);

    // 썸네일 파일 선택 처리
    const handleThumbnailChange = (e) => {
        const file = e.target.files?.[0];
        if (!file) {
            setThumbnail(null);
            return;
        }
        if (!ALLOWED_TYPES.includes(file.type)) {
            alert("썸네일 이미지 형식은 PNG, JPG, JPEG만 가능합니다.");
            e.target.value = null;
            return;
        }
        if (file.size > MAX_FILE_SIZE) {
            alert("썸네일 이미지는 10MB 이하만 가능합니다.");
            e.target.value = null;
            return;
        }
        setThumbnail(file);
    };

    // 썸네일 이미지 삭제
    const removeThumbnail = () => {
        setThumbnail(null);
    };

    // 포트폴리오 이미지 추가
    const handleAddImage = (e) => {
        const file = e.target.files?.[0];
        if (!file) return;

        if (images.length >= 4) {
            alert("포트폴리오 이미지는 최대 4개까지 추가할 수 있습니다.");
            e.target.value = null;
            return;
        }

        if (!ALLOWED_TYPES.includes(file.type)) {
            alert("포트폴리오 이미지 형식은 PNG, JPG, JPEG만 가능합니다.");
            e.target.value = null;
            return;
        }

        if (file.size > MAX_FILE_SIZE) {
            alert("각 이미지 파일은 10MB 이하만 가능합니다.");
            e.target.value = null;
            return;
        }

        const currentTotalSize = images.reduce((acc, cur) => acc + cur.size, 0);
        if (currentTotalSize + file.size > MAX_TOTAL_SIZE) {
            alert("포트폴리오 이미지 총 용량은 20MB 이하만 가능합니다.");
            e.target.value = null;
            return;
        }

        setImages([...images, file]);
        e.target.value = null; // 같은 파일을 다시 선택할 수 있도록 초기화
    };

    // 포트폴리오 이미지 삭제
    const removeImage = (index) => {
        setImages(images.filter((_, i) => i !== index));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // 에러 상태 초기화
        setErrors({});
        const newErrors = {};

        // 필수값 체크
        if (!title.trim()) newErrors.title = true;
        if (!content.trim()) newErrors.content = true;
        if (!selectedSpecialty) newErrors.selectedSpecialty = true;
        if (!category) newErrors.category = true;
        if (!workingYear) newErrors.workingYear = true;
        if (!thumbnail) newErrors.thumbnail = true;
        if (images.length < 1) newErrors.images = true;

        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            alert("모든 필드를 빠짐없이 입력해주세요.");

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
        images.forEach((file) => formData.append("images", file));
        formData.append("thumbnail", thumbnail);

        try {
            await axiosInstance.post("/api/expert/portfolio", formData, {
                headers: {
                    "Content-Type": "multipart/form-data",
                },
            });
            alert("등록 성공");
            navigate("/expert/profile");
        } catch (error) {
            console.error("등록 실패", error);
            alert("등록 실패");
        }
    };

    return (
        <div className="bg-gradient-to-br from-slate-50 to-blue-50 py-8 px-4">
            <div className="max-w-6xl mx-auto">
                <div className="text-center mb-6">
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">포트폴리오 등록</h1>
                    <p className="text-gray-600">나의 작품을 세상에 선보이세요</p>
                </div>

                <div className="bg-white rounded-2xl shadow-xl backdrop-blur-sm border border-white/20 overflow-hidden">
                    <div className="p-6">
                        <form onSubmit={handleSubmit} className="space-y-6">
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
                                        className={`w-full px-3 py-2.5 rounded-lg border-2 transition-all duration-200 focus:outline-none focus:ring-0 text-sm ${
                                            errors.title
                                                ? 'border-red-400 bg-red-50 focus:border-red-500'
                                                : 'border-gray-200 focus:border-blue-500 hover:border-gray-300'
                                        }`}
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
                                        className={`w-full px-3 py-2.5 rounded-lg border-2 transition-all duration-200 focus:outline-none focus:ring-0 text-sm ${
                                            errors.workingYear
                                                ? 'border-red-400 bg-red-50 focus:border-red-500'
                                                : 'border-gray-200 focus:border-blue-500 hover:border-gray-300'
                                        }`}
                                        required
                                    >
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
                                        className={`w-full px-3 py-2.5 rounded-lg border-2 transition-all duration-200 focus:outline-none focus:ring-0 text-sm ${
                                            errors.selectedSpecialty
                                                ? 'border-red-400 bg-red-50 focus:border-red-500'
                                                : 'border-gray-200 focus:border-blue-500 hover:border-gray-300'
                                        }`}
                                        required
                                    >
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
                                            errors.category
                                                ? 'border-red-400 bg-red-50 focus:border-red-500'
                                                : !selectedSpecialty
                                                    ? 'border-gray-200 bg-gray-100 cursor-not-allowed'
                                                    : 'border-gray-200 focus:border-blue-500 hover:border-gray-300'
                                        }`}
                                        required
                                        disabled={!selectedSpecialty}
                                    >
                                        <option value="">
                                            {!selectedSpecialty ? "먼저 전문 분야를 선택하세요" : "선택하세요"}
                                        </option>
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
                                    className={`w-full px-3 py-2.5 rounded-lg border-2 transition-all duration-200 focus:outline-none focus:ring-0 h-20 resize-none text-sm ${
                                        errors.content
                                            ? 'border-red-400 bg-red-50 focus:border-red-500'
                                            : 'border-gray-200 focus:border-blue-500 hover:border-gray-300'
                                    }`}
                                    required
                                />
                            </div>

                            {/* 세 번째 행 - 이미지 업로드 */}
                            <div className="bg-gray-50 rounded-xl p-4">
                                <h3 className="text-base font-semibold text-gray-800 flex items-center mb-4">
                                    <svg className="w-4 h-4 mr-2 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                                        <path fillRule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clipRule="evenodd" />
                                    </svg>
                                    이미지 업로드
                                </h3>

                                <div className="grid md:grid-cols-2 gap-6">
                                    {/* 썸네일 이미지 */}
                                    <div className="space-y-3">
                                        <label className="block text-sm font-semibold text-gray-700">
                                            썸네일 이미지 <span className="text-red-500">*</span>
                                        </label>
                                        <input
                                            type="file"
                                            accept="image/png,image/jpg,image/jpeg"
                                            onChange={handleThumbnailChange}
                                            className={`w-full px-3 py-2 rounded-lg border-2 border-dashed transition-all duration-200 focus:outline-none text-sm ${
                                                errors.thumbnail
                                                    ? 'border-red-400 bg-red-50'
                                                    : 'border-gray-300 hover:border-blue-400 focus:border-blue-500'
                                            }`}
                                            required
                                        />
                                        {thumbnail && (
                                            <div className="bg-white rounded-lg p-2 border border-gray-200 flex items-center justify-between">
                                                <div className="flex items-center">
                                                    <div className="w-6 h-6 bg-blue-100 rounded flex items-center justify-center mr-2">
                                                        <svg className="w-3 h-3 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                                                            <path fillRule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clipRule="evenodd" />
                                                        </svg>
                                                    </div>
                                                    <span className="text-xs font-medium text-gray-700 truncate">{thumbnail.name}</span>
                                                </div>
                                                <button
                                                    type="button"
                                                    onClick={removeThumbnail}
                                                    className="w-6 h-6 rounded-full bg-red-100 hover:bg-red-200 text-red-600 flex items-center justify-center transition-colors text-xs"
                                                >
                                                    ✕
                                                </button>
                                            </div>
                                        )}
                                    </div>

                                    {/* 포트폴리오 이미지 */}
                                    <div className="space-y-3">
                                        <div className="flex items-center justify-between">
                                            <label className="block text-sm font-semibold text-gray-700">
                                                포트폴리오 이미지 <span className="text-red-500">*</span>
                                            </label>
                                            <div className="flex space-x-1">
                                                {[...Array(4)].map((_, i) => (
                                                    <div
                                                        key={i}
                                                        className={`w-2 h-2 rounded-full ${
                                                            i < images.length ? 'bg-blue-500' : 'bg-gray-200'
                                                        }`}
                                                    />
                                                ))}
                                            </div>
                                        </div>

                                        {/* 이미지 추가 버튼 */}
                                        {images.length < 4 && (
                                            <input
                                                type="file"
                                                accept="image/png,image/jpg,image/jpeg"
                                                onChange={handleAddImage}
                                                className={`w-full px-3 py-2 rounded-lg border-2 border-dashed transition-all duration-200 focus:outline-none text-sm ${
                                                    errors.images
                                                        ? 'border-red-400 bg-red-50'
                                                        : 'border-gray-300 hover:border-blue-400 focus:border-blue-500'
                                                }`}
                                                id={`image-input-${images.length}`}
                                            />
                                        )}

                                        {/* 추가된 이미지들 표시 */}
                                        {images.length > 0 && (
                                            <div className="space-y-2 max-h-24 overflow-y-auto">
                                                {images.map((image, index) => (
                                                    <div key={index} className="bg-white rounded-lg p-2 border border-gray-200 flex items-center justify-between">
                                                        <div className="flex items-center min-w-0">
                                                            <div className="w-6 h-6 bg-green-100 rounded flex items-center justify-center mr-2 flex-shrink-0">
                                                                <span className="text-xs font-bold text-green-600">{index + 1}</span>
                                                            </div>
                                                            <span className="text-xs font-medium text-gray-700 truncate">{image.name}</span>
                                                        </div>
                                                        <button
                                                            type="button"
                                                            onClick={() => removeImage(index)}
                                                            className="w-6 h-6 rounded-full bg-red-100 hover:bg-red-200 text-red-600 flex items-center justify-center transition-colors text-xs flex-shrink-0 ml-2"
                                                        >
                                                            ✕
                                                        </button>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            </div>

                            {/* 등록 버튼 */}
                            <div className="pt-2">
                                <button
                                    type="submit"
                                    className="w-full bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white font-semibold py-3 px-6 rounded-xl transition-all duration-200 transform hover:scale-[1.01] focus:outline-none focus:ring-4 focus:ring-blue-200 shadow-lg"
                                >
                                    포트폴리오 등록하기
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
}