import { Routes, Route } from "react-router-dom";
import Login from "./login/login.jsx";
import SignUp from "./signup/signup.jsx";
import KakaoRedirect from "./callback/kakao/kakaoRedirect.jsx";

export default function Auth() {
  return (
    <>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />
        <Route path="/callback/kakao" element={<KakaoRedirect></KakaoRedirect>}></Route>
      </Routes>
    </>
  );
}
