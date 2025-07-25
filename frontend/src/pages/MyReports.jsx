// src/pages/MyReports.jsx
import { useEffect, useState } from 'react';
import axios from '../api/axiosInstance';

const MyReports = () => {
    const [reports, setReports] = useState([]);
    const [error, setError] = useState(null);

    useEffect(() => {

        console.log("useEffect 실행됨");
        const fetchReports = async () => {
            try {
                const response = await axios.get('/reports/my');
                setReports(response.data);
            } catch (err) {
                console.error(err);
                setError("신고 목록을 불러오지 못했습니다.");
            }
        };

        fetchReports();
    }, []);

    return (
        <div>
            <h2>내 신고 내역</h2>
            {error && <p style={{ color: 'red' }}>{error}</p>}
            {reports.length === 0 ? (
                <p>신고 내역이 없습니다.</p>
            ) : (
                <ul>
                    {reports.map((report) => (
                        <li key={report.id}>
                            <p><strong>피신고자:</strong> {report.reportedNickname}</p>
                            <p><strong>사유:</strong> {report.reason}</p>
                            <p><strong>상태:</strong> {report.status}</p>
                            <p><strong>처리자:</strong> {report.resolverNickname || '-'}</p>
                            <p><strong>처리일시:</strong> {report.resolvedAt || '-'}</p>
                            <hr />
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default MyReports;
