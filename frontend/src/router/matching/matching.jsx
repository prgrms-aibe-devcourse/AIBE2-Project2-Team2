import { Routes, Route } from "react-router-dom";
import MatchingDetailPage from "../../pages/matching/MatchingDetailPage";
import MatchingClientDetailPage from "../../pages/matching/MatchingClientDetailPage";
// 필요한 매칭 관련 페이지들 import

export default function MatchingRouter() {
    return (
        <Routes>
            {/* 매칭 목록 페이지가 있다면: */}
            {/* <Route path="/matching" element={<MatchingListPage />} /> */}

            {/* 매칭 상세 페이지 라우팅 */}
            <Route path="/expert/matchings/:matchingId" element={<MatchingDetailPage />} />
            <Route path="/client/matchings/:matchingId" element={<MatchingClientDetailPage />} />


            {/* 필요에 따라 추가 */}
        </Routes>
    );
}
