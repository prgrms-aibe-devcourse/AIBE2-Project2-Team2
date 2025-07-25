import React, { useMemo, useEffect, useRef } from "react";
import { CheckCircle, Circle, Upload, Image as ImageIcon, X } from "lucide-react";

const ImageStep = ({ form, onThumbnailChange, onImagesChange }) => {
  const isValid = form.thumbnail && form.images && form.images.length > 0;

  // Memoize object URLs for thumbnail and images
  const thumbnailUrl = useMemo(() => {
    if (!form.thumbnail) return null;
    return URL.createObjectURL(form.thumbnail);
  }, [form.thumbnail]);

  const imagesUrls = useMemo(() => {
    if (!form.images) return [];
    return form.images.map((img) => URL.createObjectURL(img));
  }, [form.images]);

  // Clean up object URLs on unmount or when files change
  useEffect(() => {
    return () => {
      if (thumbnailUrl) URL.revokeObjectURL(thumbnailUrl);
      imagesUrls.forEach((url) => URL.revokeObjectURL(url));
    };
  }, [thumbnailUrl, imagesUrls]);

  // Delete thumbnail
  const handleDeleteThumbnail = (e) => {
    e.stopPropagation();
    onThumbnailChange(null);
  };

  // Delete detail image
  const handleDeleteDetailImage = (idx, e) => {
    e.stopPropagation();
    // 삭제는 직접 setForm을 써야 함 (handleImagesChange는 추가만 처리)
    if (typeof onImagesChange === "function" && form.images) {
      // 삭제된 배열을 직접 반영
      onImagesChange({ type: "delete", images: form.images.filter((_, i) => i !== idx) });
    }
  };

  return (
    <div className="space-y-8">
      {/* Thumbnail Section */}
      <div className="space-y-4">
        <div className="flex items-center gap-3">
          {form.thumbnail ? <CheckCircle className="w-6 h-6 text-green-500" /> : <Circle className="w-6 h-6 text-gray-300" />}
          <h3 className="text-lg font-semibold text-gray-900">대표 이미지</h3>
        </div>
        <div className="ml-9">
          <div className="relative w-full h-64 border-2 border-dashed border-gray-300 rounded-xl flex flex-col items-center justify-center cursor-pointer hover:border-blue-400 hover:bg-blue-50 transition-all duration-200 group" onClick={() => document.getElementById("main-image-input").click()}>
            {form.thumbnail ? (
              <div className="relative w-full h-full">
                <img src={thumbnailUrl} alt="thumbnail" className="w-full h-full object-cover rounded-lg" />
                <button type="button" className="absolute top-2 right-2 bg-black bg-opacity-60 hover:bg-opacity-80 text-white rounded-full p-1 z-10" onClick={handleDeleteThumbnail}>
                  <X className="w-5 h-5" />
                </button>
                <div className="absolute inset-0 bg-black/0  group-hover:bg-opacity-20 transition-all duration-200 rounded-lg flex items-center justify-center pointer-events-none">
                  <Upload className="w-8 h-8 text-white opacity-0 group-hover:opacity-100 transition-opacity duration-200" />
                </div>
              </div>
            ) : (
              <div className="text-center">
                <Upload className="w-12 h-12 text-gray-400 group-hover:text-blue-500 transition-colors duration-200 mx-auto mb-4" />
                <p className="text-gray-600 font-medium mb-2">대표 이미지를 업로드하세요</p>
                <p className="text-gray-400 text-sm">권장 크기: 652 × 488px (4:3 비율)</p>
              </div>
            )}
            <input id="main-image-input" type="file" accept="image/*" className="hidden" onChange={(e) => onThumbnailChange(e.target.files[0])} />
          </div>
          <p className="text-sm text-gray-500 mt-2">서비스를 대표할 수 있는 매력적인 이미지를 선택해주세요</p>
        </div>
      </div>

      {/* Detail Images Section */}
      <div className="space-y-4">
        <div className="flex items-center gap-3">
          {form.images && form.images.length > 0 ? <CheckCircle className="w-6 h-6 text-green-500" /> : <Circle className="w-6 h-6 text-gray-300" />}
          <h3 className="text-lg font-semibold text-gray-900">상세 이미지</h3>
        </div>
        <div className="ml-9">
          <div
            className="relative w-full min-h-32 border-2 border-dashed border-gray-300 rounded-xl flex flex-col items-center justify-center cursor-pointer hover:border-blue-400 hover:bg-blue-50 transition-all duration-200 group p-6"
            onClick={() => document.getElementById("detail-image-input").click()}>
            {form.images && form.images.length > 0 ? (
              <div className="w-full">
                <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mb-4">
                  {imagesUrls.slice(0, 5).map((url, idx) => (
                    <div key={idx} className="relative group/item">
                      <img src={url} alt="preview" className="w-full h-24 object-cover rounded-lg border border-gray-200" />
                      <button type="button" className="absolute top-2 right-2 bg-black bg-opacity-60 hover:bg-opacity-80 text-white rounded-full p-1 z-10" onClick={(e) => handleDeleteDetailImage(idx, e)}>
                        <X className="w-4 h-4" />
                      </button>
                      <div className="absolute inset-0 bg-black/0 group-hover/item:bg-opacity-20 transition-all duration-200 rounded-lg pointer-events-none"></div>
                    </div>
                  ))}
                </div>
                <div className="text-center">
                  <ImageIcon className="w-8 h-8 text-blue-500 mx-auto mb-2" />
                  <p className="text-blue-600 font-medium">클릭하여 이미지 추가/변경</p>
                  <p className="text-gray-400 text-sm">최대 5장까지 업로드 가능</p>
                </div>
              </div>
            ) : (
              <div className="text-center">
                <ImageIcon className="w-12 h-12 text-gray-400 group-hover:text-blue-500 transition-colors duration-200 mx-auto mb-4" />
                <p className="text-gray-600 font-medium mb-2">상세 이미지를 업로드하세요</p>
                <p className="text-gray-400 text-sm">최대 5장까지 업로드 가능</p>
              </div>
            )}
            <input id="detail-image-input" type="file" accept="image/*" multiple className="hidden" onChange={(e) => onImagesChange(e.target.files)} />
          </div>
          <p className="text-sm text-gray-500 mt-2">서비스의 작업 과정이나 결과물을 보여주는 이미지들을 추가해주세요</p>
        </div>
      </div>

      {/* Image Tips */}
      <div className="ml-9 bg-gradient-to-r from-purple-50 to-pink-50 border border-purple-200 rounded-xl p-5">
        <div className="flex items-start gap-3">
          <div className="w-2 h-2 bg-purple-500 rounded-full mt-2 flex-shrink-0"></div>
          <div>
            <p className="text-purple-800 font-medium mb-2">이미지 업로드 가이드</p>
            <ul className="text-purple-700 text-sm space-y-1">
              <li>• 고화질의 선명한 이미지 사용</li>
              <li>• 서비스 내용과 관련된 이미지 선택</li>
              <li>• 다양한 각도와 상황의 이미지 포함</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ImageStep;
