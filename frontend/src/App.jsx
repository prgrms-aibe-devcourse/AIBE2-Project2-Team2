import { Routes, Route } from "react-router-dom";
import Header from "./components/header.jsx";
import Auth from "./router/auth/auth.jsx";
import { Modal } from "./components/modal/Modal.jsx";
import { useEffect } from "react";
import axiosInstance from "./lib/axios.js";
import { useUserInfoStore } from "./store/userInfo.js";
import ContentDetailPage from "./router/content/ContentDetailPage";
import { ContentEditStepperPage } from "./router/content";
import PaymentPage from "./router/content/PaymentPage";
import MyPage from "./router/mypage/mypage.jsx";
import Expert from "./router/expert/expert.jsx";
import ChatPage from "./router/chat/ChatPage.jsx";
import MatchingClientDetailPage from "./pages/matching/MatchingClientDetailPage.jsx";
import CustomerSupport from "./pages/CustomerSupport.jsx";
import CategoryPage from "./pages/CategoryPage.jsx";
import MainPage from "./pages/MainPage.jsx";
import ContentCreateStepperPage from "./router/content/create/ContentCreateStepperPage.jsx";
import ReviewWrite from "./pages/review/ReviewWrite.jsx";
import CustomToast from "./lib/CustomToast.jsx";
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
      <div className="flex flex-col justify-start items-center">
        <Header />
        <div className="h-30"></div>
        <Routes>
          <Route path="/" element={<MainPage />} />
          <Route path="/auth/*" element={<Auth />} />
          <Route path="/content/create" element={<ContentCreateStepperPage />} />
          <Route path="/content/:id" element={<ContentDetailPage />} />
          <Route path="/content/edit/:id" element={<ContentEditStepperPage />} />
          <Route path="/content/:id/payment" element={<PaymentPage />} />
          <Route path="/mypage/*" element={<MyPage />} />
          <Route path="/expert/*" element={<Expert />} />
          <Route path="/chat" element={<ChatPage />} />
          <Route path="/client/matchings/:matchingId" element={<MatchingClientDetailPage />} />
          <Route path="/customer-support" element={<CustomerSupport />} />
          <Route path="/category/:categoryName" element={<CategoryPage />} />
          <Route path="/review/write/:id" element={<ReviewWrite />} />
        </Routes>
      </div>
      <Modal />
      <CustomToast />
    </>
  );
}

export default App;
