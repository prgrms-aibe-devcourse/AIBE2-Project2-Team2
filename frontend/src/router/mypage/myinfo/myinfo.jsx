import { useEffect, useState } from "react";
import { Camera, Edit2, Check, X, Mail, Phone } from "lucide-react";
import axiosInstance from "../../../lib/axios.js";

export default function MyInfo() {
  const [userInfo, setUserInfo] = useState({
    nickname: "",
    profileImageUrl: "",
    email: "",
    joinType: "",
    phone: "",
  });

  const [isEditingNickname, setIsEditingNickname] = useState(false);
  const [newNickname, setNewNickname] = useState("");
  const [isUploading, setIsUploading] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    axiosInstance
      .get("/api/me")
      .then((res) => {
        setUserInfo(res.data);
        setNewNickname(res.data.nickname);
      })
      .catch((err) => {
        console.error("사용자 정보 불러오기 실패:", err);
      })
      .finally(() => setIsLoading(false));
  }, []);

  const handleImageUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    setIsUploading(true);
    try {
      const formData = new FormData();
      formData.append("file", file);

      await axiosInstance.post("/api/me/profile-image", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      const imageUrl = URL.createObjectURL(file);
      setUserInfo((prev) => ({
        ...prev,
        profileImageUrl: imageUrl,
      }));
    } catch (err) {
      console.error("프로필 이미지 업로드 실패:", err);
    } finally {
      setIsUploading(false);
    }
  };

  const handleNicknameEdit = () => {
    setIsEditingNickname(true);
    setNewNickname(userInfo.nickname);
  };

  const handleNicknameSave = async () => {
    if (!newNickname.trim()) return;

    try {
      await axiosInstance.patch("/api/me/nickname", { nickname: newNickname });
      setUserInfo((prev) => ({
        ...prev,
        nickname: newNickname,
      }));
      setIsEditingNickname(false);
    } catch (err) {
      console.error("닉네임 수정 실패:", err);
    }
  };

  const handleNicknameCancel = () => {
    setIsEditingNickname(false);
    setNewNickname(userInfo.nickname);
  };

  if (isLoading) {
    return <div className="text-center py-10 text-gray-400">로딩 중...</div>;
  }

  return (
    <div className="bg-white px-6">
      <div className="max-w-4xl mx-auto bg-white rounded-lg shadow-md border border-gray-100 p-10">
        <h1 className="text-2xl font-semibold mb-8 text-gray-800">내 정보</h1>

        <div className="flex gap-14">
          {/* 좌측: 프로필 이미지 및 변경 버튼 */}
          <div className="flex flex-col items-center space-y-5">
            <div className="relative w-36 h-36 rounded-full bg-gray-100 flex items-center justify-center overflow-hidden border border-gray-200">
              <img src={userInfo.profileImageUrl || "/api/placeholder/150/150"} alt="프로필 이미지" className="w-full h-full object-cover object-center" />
              {isUploading && (
                <div className="absolute inset-0 bg-black bg-opacity-25 flex items-center justify-center">
                  <div className="animate-spin rounded-full h-9 w-9 border-b-2 border-white"></div>
                </div>
              )}
            </div>
            <label className="cursor-pointer inline-block rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-600 hover:bg-gray-100 transition duration-200" aria-label="프로필 이미지 변경">
              프로필 변경
              <input type="file" accept="image/*" onChange={handleImageUpload} className="hidden" disabled={isUploading} />
            </label>
          </div>

          {/* 우측: 정보 입력폼 */}
          <div className="flex-1 space-y-8">
            {/* 닉네임 */}
            <div>
              <label htmlFor="nickname" className="block font-semibold mb-2 text-gray-700">
                닉네임
              </label>
              {isEditingNickname ? (
                <div className="flex items-center gap-3">
                  <input id="nickname" type="text" value={newNickname} onChange={(e) => setNewNickname(e.target.value)} className="flex-1 rounded-md border border-gray-300 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-blue-500 transition" autoFocus maxLength={17} />
                  <button onClick={handleNicknameSave} aria-label="저장" className="text-green-600 hover:text-green-800 transition">
                    <Check size={22} />
                  </button>
                  <button onClick={handleNicknameCancel} aria-label="취소" className="text-red-600 hover:text-red-800 transition">
                    <X size={22} />
                  </button>
                </div>
              ) : (
                <div className="flex items-center justify-between">
                  <input id="nickname" type="text" value={userInfo.nickname} disabled className="flex-1 rounded-md border border-gray-200 bg-gray-50 px-4 py-3 cursor-not-allowed text-gray-500" />
                  <button onClick={handleNicknameEdit} className="ml-4 text-gray-400 hover:text-gray-600 transition" aria-label="닉네임 편집">
                    <Edit2 size={20} />
                  </button>
                </div>
              )}
              <ul className="mt-2 text-xs text-gray-400 space-y-1 leading-tight">
                <li>* 닉네임은 최초 설정 또는 변경 후 30일이 지나야 바꿀 수 있어요. (최근 변경 일시: 2025-07-18 11:19:39)</li>
                <li>* 진행 중인 거래가 있으면 닉네임을 바꿀 수 없어요.</li>
                <li>* 한글/영문/숫자만 사용할 수 있으며, 이메일 아이디와 동일한 문자열은 사용이 불가해요.</li>
              </ul>
            </div>

            {/* 이메일 */}
            <div>
              <label htmlFor="email" className="block font-semibold mb-2 text-gray-700">
                이메일
              </label>
              <input id="email" type="text" value={userInfo.email} disabled className="w-full rounded-md border border-gray-200 bg-gray-50 px-4 py-3 cursor-not-allowed text-gray-500" />
              <p className="mt-1 text-xs text-gray-400">* 이메일을 변경하시려면 고객센터로 문의해 주세요.</p>
            </div>

            {/* 연동된 계정 */}
            <div>
              <p className="block font-semibold mb-2 text-gray-700">가입 경로</p>
              <div className="flex space-x-4 items-center">
                {userInfo.joinType === "KAKAO" && (
                  <div className="rounded-full bg-yellow-300 w-9 h-9 flex items-center justify-center cursor-default shadow-sm">
                    <img src="https://static.cdn.kmong.com/assets/icon/kakao-logo_v2.png" alt="카카오 계정 연동 상태" className="w-6 h-6" />
                  </div>
                )}
                {userInfo.joinType === "NORMAL" && (
                  <div className="rounded-full bg-gray-300 w-9 h-9 flex items-center justify-center cursor-default shadow-sm">
                    <span className="text-xs text-gray-600 select-none font-semibold">E</span>
                  </div>
                )}
              </div>
            </div>

            {/* 휴대폰 번호 */}
            <div>
              <label htmlFor="phone" className="block font-semibold mb-2 text-gray-700">
                휴대폰 번호
              </label>
              <input id="phone" type="text" value={userInfo.phone || "없음"} disabled className="w-full rounded-md border border-gray-200 bg-gray-50 px-4 py-3 cursor-not-allowed text-gray-500" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
