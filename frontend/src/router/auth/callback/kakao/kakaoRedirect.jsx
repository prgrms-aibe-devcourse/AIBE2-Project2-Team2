import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../../../../lib/axios.js";
import { useUserInfoStore } from "../../../../store/userInfo.js";

const KakaoRedirect = () => {
  const code = window.location.search;
  const url = "/api/auth/kakao/callback";
  const navigate = useNavigate();
  const { setUserInfo } = useUserInfoStore(); // Assuming useUserInfoStore is imported from your Zustand store
  useEffect(() => {
    axiosInstance
      .get(`${url}${code}`)
      .then((res) => {
        console.log(res);
        setUserInfo(res.data); // Store user info in Zustand store
        navigate("/");
      })
      .catch((error) => {
        navigate("/auth/login");
        console.error(error);
      });
  }, []);
  return <div>로그인 중입니다.</div>;
};

export default KakaoRedirect;
