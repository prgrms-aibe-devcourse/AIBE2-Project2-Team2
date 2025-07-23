import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../../../lib/axios.js";
import { useUserInfoStore } from "../../../store/userInfo.js";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [emailError, setEmailError] = useState("");
  const [passwordError, setPasswordError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { setUserInfo } = useUserInfoStore(); // Assuming useUserInfoStore is imported from your Zustand store

  const navigate = useNavigate();

  const validateEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const handleEmailChange = (e) => {
    const value = e.target.value;
    setEmail(value);
    if (emailError) setEmailError("");
  };

  const handlePasswordChange = (e) => {
    const value = e.target.value;
    setPassword(value);
    if (passwordError) setPasswordError("");
  };

  const sendLoginRequest = async (email, password) => {
    try {
      const response = await axiosInstance.post("/api/auth/login", {
        email,
        password,
      });
      return response.data;
    } catch (error) {
      console.error("Login request failed:", error);
      throw error;
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // 입력값 검증
    let hasError = false;

    if (!email.trim()) {
      setEmailError("이메일을 입력해 주세요.");
      hasError = true;
    } else if (!validateEmail(email)) {
      setEmailError("올바른 이메일 주소를 입력해 주세요.");
      hasError = true;
    }

    if (!password.trim()) {
      setPasswordError("비밀번호를 입력해 주세요.");
      hasError = true;
    }

    if (hasError) return;

    setIsSubmitting(true);

    try {
      // 로그인 처리 로직
      console.log("로그인 시도:", { email, password });

      // API 호출 시뮬레이션
      const response = await sendLoginRequest(email, password);
      setUserInfo(response); // 사용자 정보를 store에 저장

      // 로그인 성공 후 리다리렉트 홈으로
      console.log("로그인 성공:", response);
      navigate("/");
    } catch (error) {
      console.error("로그인 오류:", error);
      alert("로그인에 실패했습니다. 다시 시도해 주세요.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleFindEmail = () => {
    console.log("이메일 찾기 클릭");
    // 이메일 찾기 로직 또는 페이지 이동
  };

  const handleFindPassword = () => {
    console.log("비밀번호 찾기 클릭");
    // 비밀번호 찾기 로직 또는 페이지 이동
  };

  return (
    <div className="max-w-[300px] flex flex-col items-center ">
      <div className="bg-white w-full">
        <form onSubmit={handleSubmit}>
          {/* 이메일 입력 */}
          <div className="mb-4">
            <label htmlFor="email" className="block mb-2 font-semibold text-gray-700">
              이메일
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={handleEmailChange}
              placeholder="이메일을 입력해 주세요"
              disabled={isSubmitting}
              className={`w-full px-3 py-2 border rounded-md transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed ${
                emailError ? "border-red-500 focus:ring-red-500" : "border-gray-300 hover:border-gray-400"
              }`}
            />
            {emailError && (
              <p className="text-red-500 text-sm mt-1" role="alert">
                {emailError}
              </p>
            )}
          </div>

          {/* 비밀번호 입력 */}
          <div className="mb-6">
            <label htmlFor="password" className="block mb-2 font-semibold text-gray-700">
              비밀번호
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={handlePasswordChange}
              placeholder="비밀번호를 입력해 주세요"
              disabled={isSubmitting}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  handleSubmit(e);
                }
              }}
              className={`w-full px-3 py-2 border rounded-md transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed ${
                passwordError ? "border-red-500 focus:ring-red-500" : "border-gray-300 hover:border-gray-400"
              }`}
            />
            {passwordError && (
              <p className="text-red-500 text-sm mt-1" role="alert">
                {passwordError}
              </p>
            )}
          </div>

          {/* 로그인 버튼 */}
          <button
            type="button"
            onClick={handleSubmit}
            disabled={isSubmitting}
            className="h-[44px] w-full py-2 px-4 rounded-md bg-[#6C4EFF] text-white font-semibold text-sm transition-colors duration-200 hover:bg-[#5a3fd6] focus:outline-none focus:ring-2 focus:ring-[#6C4EFF] focus:ring-offset-2 disabled:bg-gray-400 disabled:cursor-not-allowed">
            {isSubmitting ? "로그인 중..." : "이메일 로그인"}
          </button>

          {/* 이메일/비밀번호 찾기 */}
          <div className="flex justify-center items-center gap-4 mt-4 text-sm text-gray-500">
            <button type="button" onClick={handleFindEmail} disabled={isSubmitting} className="hover:underline hover:text-gray-700 focus:outline-none focus:underline disabled:cursor-not-allowed">
              이메일 찾기
            </button>
            <span className="text-gray-300">|</span>
            <button type="button" onClick={handleFindPassword} disabled={isSubmitting} className="hover:underline hover:text-gray-700 focus:outline-none focus:underline disabled:cursor-not-allowed">
              비밀번호 찾기
            </button>
          </div>
          <img
            className="mt-10 h-[44px] w-full"
            src="/images/kakao_login.png"
            alt="Kakao Login"
            onClick={() => {
              location.href = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + import.meta.env.VITE_APP_CLIENT_ID + "&redirect_uri=" + import.meta.env.VITE_APP_REDIRECT_URI;
            }}
            disabled={isSubmitting}
          />
        </form>
      </div>
    </div>
  );
}
