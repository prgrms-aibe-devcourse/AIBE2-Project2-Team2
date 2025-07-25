import { Routes, Route } from "react-router-dom";
import Header from "./components/header.jsx";
import Auth from "./router/auth/auth.jsx";
import { Modal } from "./components/modal/Modal.jsx";
import { useEffect } from "react";
import axiosInstance from "./lib/axios.js";
import { useUserInfoStore } from "./store/userInfo.js";
import MatchingRouter from "./router/matching/matching.jsx"; // matching 라우터 import

//api/me
// {
//   "nickname": "홍길동",
//   "profileImageUrl": "https://cdn.example.com/profile.jpg",
//   "email": "hong@example.com",
//   "joinType": "KAKAO"
// }

function App() {
  //store에서 유저 정보를 저장하는 함수 가져오기
  // useUserInfoStore는 Zustand를 사용하여 전역 상태를 관리하는 훅
  const setUserInfo = useUserInfoStore((state) => state.setUserInfo);

  // 사용자 정보를 가져오는 함수
  async function getUserInfo() {
    try {
      const response = await axiosInstance.get("/api/common/check");
      setUserInfo(response.data); // 사용자 정보를 store에 저장
      console.log(response.data);
    } catch (error) {
      console.error("Error fetching user info:", error);
    }
  }

  useEffect(() => {
    getUserInfo();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
      <>
        <div className="w-dvw h-dvh flex flex-col justify-start items-center">
          <Header />
          <div className="h-30"></div>
          <Routes>
            <Route path="/" element={<h2>Home Page</h2>} />
            <Route path="/auth/*" element={<Auth />} />
            <Route path="/about" element={<h2>About Page</h2>} />
            <Route path="/*" element={<MatchingRouter />} />
          </Routes>
          <Modal />
        </div>
      </>
  );
}

export default App;
