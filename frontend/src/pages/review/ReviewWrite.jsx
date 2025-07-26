import { useEffect, useState } from "react";
import { Star, Upload, X } from "lucide-react";
import { useNavigate, useParams } from "react-router-dom";
import axiosInstance from "../../lib/axios";

function ReviewWrite() {
  const { id } = useParams();
  const [reviewContent, setReviewContent] = useState("");
  const [rating, setRating] = useState(5);

  // 파일 업로드 변수
  const [image, setImage] = useState(null);
  const [previewImage, setPreviewImage] = useState(null);
  const [fileName, setFileName] = useState("");

  // 이미지를 선택하는 함수
  const handleImageChange = (event) => {
    if (event.target.files && event.target.files[0]) {
      setImage(event.target.files[0]);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewImage(reader.result);
      };
      setFileName(event.target.files[0].name);
      reader.readAsDataURL(event.target.files[0]);
    }
  };

  // 미리보기 이미지를 삭제하는 함수
  const handleRemoveClick = () => {
    setImage(null);
    setPreviewImage(null);
    setFileName("");
    const fileInput = document.getElementById("file");
    if (fileInput) fileInput.value = "";
  };

  const navigate = useNavigate();

  // 리뷰 작성 api
  const reviewCreate = () => {
    console.log(reviewContent, rating, image, id);
    if (!reviewContent) {
      alert("리뷰 내용을 입력해주세요");
      return;
    }
    const formData = new FormData();
    if (reviewContent) {
      formData.append("comment", reviewContent);
    }
    if (rating) {
      formData.append("rating", rating);
    }
    if (image) {
      formData.append("image", image);
    }

    axiosInstance
      .post(`/api/reviews/${id}`, formData)
      .then((res) => {
        console.log(res);
        alert("리뷰가 성공적으로 작성되었습니다");
        navigate("/");
      })
      .catch((err) => {
        alert("리뷰 작성에 실패했습니다");
        console.log(err);
      });
  };

  // 별점 설정 함수
  const handleRatingChange = (newRating) => {
    setRating(newRating);
  };

  useEffect(() => {
    console.log(rating);
  }, [rating]);

  // 별점 컴포넌트
  const StarRating = ({ rating, onRatingChange }) => {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(<Star key={i} size={28} onClick={() => onRatingChange(i)} className={`cursor-pointer transition-colors ${rating >= i ? "text-yellow-400 fill-current" : "text-gray-300"}`} />);
    }
    return <div className="flex gap-1">{stars}</div>;
  };

  return (
    <div className="min-h-screen w-full px-4">
      <div className="max-w-2xl mx-auto bg-white rounded-lg shadow-md border border-gray-100 p-8">
        {/* 헤더 */}
        <div className="mb-8">
          <h1 className="text-2xl font-semibold text-gray-800 text-center">리뷰 작성</h1>
          <p className="text-gray-600 text-center mt-2">서비스에 대한 솔직한 후기를 남겨주세요</p>
        </div>

        <div className="space-y-8">
          {/* 별점 섹션 */}
          <div className="text-center">
            <label className="block font-semibold mb-4 text-gray-700">평점을 선택해주세요</label>
            <div className="flex justify-center mb-2">
              <StarRating rating={rating} onRatingChange={handleRatingChange} />
            </div>
            <p className="text-sm text-gray-500">{rating}점 / 5점</p>
          </div>

          {/* 리뷰 내용 */}
          <div>
            <label className="block font-semibold mb-3 text-gray-700">리뷰 내용</label>
            <textarea
              className="w-full h-32 p-4 border border-gray-200 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
              placeholder="서비스 이용 경험에 대해 자세히 알려주세요&#10;다른 사용자들에게 도움이 되는 솔직한 후기를 작성해주시면 감사하겠습니다."
              value={reviewContent}
              onChange={(e) => setReviewContent(e.target.value)}
            />
            <div className="text-right mt-2">
              <span className="text-sm text-gray-500">{reviewContent.length} / 1000</span>
            </div>
          </div>

          {/* 이미지 업로드 */}
          <div>
            <label className="block font-semibold mb-3 text-gray-700">사진 첨부 (선택사항)</label>

            {previewImage ? (
              <div className="relative">
                <div className="relative w-full rounded-lg overflow-hidden border border-gray-200">
                  <img className="w-full h-64 object-cover" src={previewImage} alt="preview" />
                  <button className="absolute top-3 right-3 p-2 bg-black bg-opacity-50 rounded-full text-white hover:bg-opacity-70 transition-all" onClick={handleRemoveClick}>
                    <X size={16} />
                  </button>
                </div>
                <p className="text-sm text-gray-600 mt-2 truncate">{fileName}</p>
              </div>
            ) : (
              <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center hover:border-blue-400 transition-colors">
                <Upload size={32} className="mx-auto text-gray-400 mb-3" />
                <p className="text-gray-600 mb-2">사진을 클릭하여 업로드</p>
                <p className="text-sm text-gray-500 mb-4">JPG, PNG 파일만 지원됩니다</p>
                <label htmlFor="file" className="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 cursor-pointer transition-colors">
                  <Upload size={16} className="mr-2" />
                  파일 선택
                </label>
                <input type="file" id="file" className="hidden" accept="image/*" onChange={handleImageChange} />
              </div>
            )}
          </div>

          {/* 버튼 그룹 */}
          <div className="flex gap-4 pt-4">
            <button
              className="flex-1 py-3 px-6 border border-gray-300 text-gray-700 font-medium rounded-md hover:bg-gray-50 transition-colors"
              onClick={() => {
                setReviewContent("");
                setRating(5);
                handleRemoveClick();
              }}>
              취소
            </button>
            <button className="flex-1 py-3 px-6 bg-blue-600 text-white font-medium rounded-md hover:bg-blue-700 transition-colors" onClick={reviewCreate}>
              리뷰 작성
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ReviewWrite;
