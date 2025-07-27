import { Routes, Route } from "react-router-dom";
import MyInfo from "./myinfo/myinfo.jsx";
import PayHistory from "./payHistory/payHistory.jsx";
import Matching from "./matching/matching.jsx";

export default function MyPage() {
  return (
    <>
      <Routes>
        <Route path="/my-info" element={<MyInfo />} />
        <Route path="/pay-history" element={<PayHistory />} />
        <Route path="/matching/*" element={<Matching />} />
      </Routes>
    </>
  );
}
