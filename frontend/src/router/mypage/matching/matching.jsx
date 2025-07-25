import { Routes, Route } from "react-router-dom";
import MatchingHistory from "./matchingHistory/matchingHistory";

export default function Matching() {
  return (
    <>
      <Routes>
        <Route path="/history" element={<MatchingHistory />} />
      </Routes>
    </>
  );
}
