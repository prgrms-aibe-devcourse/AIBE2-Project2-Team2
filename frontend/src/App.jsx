import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MyReports from './pages/MyReports'; // ✅ 실제 파일 import
// import ReportForm from './pages/ReportForm'; // ← 나중에 만들 거면 일단 주석 처리해도 돼

// 임시 홈 페이지
const Home = () => <h2>홈 페이지입니다</h2>;

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Home />} />
                {/* <Route path="/report/new" element={<ReportForm />} /> */}
                <Route path="/reports" element={<MyReports />} /> {/* ✅ 이제 진짜 컴포넌트 */}
            </Routes>
        </Router>
    );
}

export default App;
