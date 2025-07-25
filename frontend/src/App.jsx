import { Routes, Route } from "react-router-dom";
import Header from "./components/header.jsx";
import Auth from "./router/auth/auth.jsx";
import { Modal } from "./components/modal/Modal.jsx";
import { useEffect } from "react";
import axiosInstance from "./lib/axios.js";
import { useUserInfoStore } from "./store/userInfo.js";

// 고객센터 신고 관련 Import
import CustomerSupport from "./pages/CustomerSupport";

// 메인 페이지 import
import MainPage from "./pages/MainPage";

// 카테고리별 상품 리스트 페이지 import
import CategoryPage from "./pages/CategoryPage";

function App() {
  const setUserInfo = useUserInfoStore((state) => state.setUserInfo);

  async function getUserInfo() {
    try {
      const response = await axiosInstance.get("/api/common/check");
      setUserInfo(response.data);
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
            <Route path="/" element={<MainPage />} />
            <Route path="/auth/*" element={<Auth />} />
            <Route path="/about" element={<h2>About Page</h2>} />
            <Route path="/customer-support" element={<CustomerSupport />} />
            <Route path="/category/:categoryName" element={<CategoryPage />} />
          </Routes>
          <Modal />
        </div>
      </>
  );
}

export default App;
