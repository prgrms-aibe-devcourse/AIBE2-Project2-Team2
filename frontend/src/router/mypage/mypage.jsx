import { Routes, Route } from "react-router-dom";
import MyInfo from "./myinfo/myinfo.jsx";

export default function MyPage() {
  return (
    <>
      <Routes>
        <Route path="/my-info" element={<MyInfo />} />
      </Routes>
    </>
  );
}
