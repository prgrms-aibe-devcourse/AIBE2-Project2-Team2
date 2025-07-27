import { useEffect, useState } from "react";
import axios from "axios";
import axios from "../../api/axiosInstance";

const CATEGORIES = ["욕설", "사기", "허위광고", "기타"];

const UserSupport = () => {
    const [reportedNickname, setReportedNickname] = useState("");
    const [category, setCategory] = useState("");
    const [customReason, setCustomReason] = useState("");
    const [myReports, setMyReports] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    // 신고 등록
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!reportedNickname.trim() || !category) {
            alert("닉네임과 신고 사유를 선택해주세요.");
            return;
        }

        if (category === "기타" && !customReason.trim()) {
            alert("기타 사유를 입력해주세요.");
            return;
        }

        // 신고 사유 조합
        const finalReason =
            category === "기타"
                ? `기타: ${customReason.trim()}`
                : `${category}${customReason.trim() ? ": " + customReason.trim() : ""}`;

        try {
            setLoading(true);

            await axios.post("/reports", {
                reportedNickname,
                reason: finalReason,
            });

            alert("신고가 접수되었습니다.");

            // 입력 초기화
            setReportedNickname("");
            setCategory("");
            setCustomReason("");

            // 신고 내역 새로고침
            await fetchMyReports();

            window.scrollTo({ top: 0, behavior: "smooth" });
        } catch (err) {
            console.error(err);
            if (err.response?.data?.errorCode === "MEMBER_NOT_FOUND") {
                alert(err.response.data.message);
            } else {
                alert("신고 등록에 실패했습니다.");
            }
        } finally {
            setLoading(false);
        }
    };

    // 내 신고 내역 조회
    const fetchMyReports = async () => {
        try {
            const response = await axios.get("/reports/my");
            setMyReports(response.data);
        } catch (err) {
            console.error(err);
            setError("신고 내역을 불러올 수 없습니다.");
        }
    };

    // 초기 마운트 시 신고 내역 조회
    useEffect(() => {
        fetchMyReports();
    }, []);

    return (
        <div className="max-w-5xl mx-auto px-8 py-12">
            {/* 신고 접수 헤더 */}
            <h2 className="text-3xl font-bold mb-8 mt-20 text-center">신고 접수</h2>

            {/* 신고 입력 폼 */}
            <form
                onSubmit={handleSubmit}
                className="max-w-2xl w-full mx-auto bg-gray-50 p-10 rounded-lg shadow-md space-y-6"
            >
                {/* 피신고자 닉네임 */}
                <div>
                    <label className="block text-sm font-semibold mb-2">
                        피신고자 닉네임
                    </label>
                    <input
                        type="text"
                        value={reportedNickname}
                        onChange={(e) => setReportedNickname(e.target.value)}
                        required
                        placeholder="ex) honggildong"
                        className="w-full border px-4 py-2 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                    />
                </div>

                {/* 카테고리 선택 */}
                <div>
                    <label className="block text-sm font-semibold mb-2">
                        신고 사유 카테고리
                    </label>
                    <select
                        value={category}
                        onChange={(e) => setCategory(e.target.value)}
                        required
                        className="w-full border px-4 py-2 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
                    >
                        <option value="">카테고리를 선택하세요</option>
                        {CATEGORIES.map((cat) => (
                            <option key={cat} value={cat}>
                                {cat}
                            </option>
                        ))}
                    </select>
                </div>

                {/* 설명 입력 (선택) */}
                {category && (
                    <div>
                        <label className="block text-sm font-semibold mb-2">
                            추가 설명 (선택)
                        </label>
                        <textarea
                            value={customReason}
                            onChange={(e) => setCustomReason(e.target.value)}
                            rows={3}
                            placeholder={
                                category === "기타"
                                    ? "기타 신고 내용을 입력하세요"
                                    : "자세한 설명이 있다면 입력해주세요"
                            }
                            className="w-full border px-4 py-2 rounded-md shadow-sm resize-none focus:outline-none focus:ring-2 focus:ring-blue-400"
                        />
                    </div>
                )}

                {/* 제출 버튼 */}
                <div className="text-right">
                    <button
                        type="submit"
                        disabled={loading}
                        className={`${
                            loading ? "bg-gray-400" : "bg-red-500 hover:bg-red-600"
                        } text-white font-semibold px-6 py-2 rounded-md`}
                    >
                        {loading ? "신고 접수 중..." : "신고하기"}
                    </button>
                </div>
            </form>

            {/* 내 신고 내역 */}
            <h3 className="text-2xl font-bold mt-10 mb-6">내 신고 내역</h3>

            {error && <p className="text-red-600 mb-4">{error}</p>}

            {myReports.length === 0 ? (
                <p className="text-gray-500">신고 내역이 없습니다.</p>
            ) : (
                <ul className="grid gap-6 grid-cols-1 md:grid-cols-2">
                    {myReports.map((report) => (
                        <li
                            key={report.id}
                            className="border p-5 rounded-md shadow-sm bg-white"
                        >
                            <p><strong>피신고자:</strong> {report.reportedNickname}</p>
                            <p><strong>사유:</strong> {report.reason}</p>
                            <p><strong>상태:</strong> {report.status}</p>
                            <p><strong>처리자:</strong> {report.resolverNickname || <span className="text-gray-400">-</span>}</p>
                            <p><strong>처리일시:</strong> {report.resolvedAt || <span className="text-gray-400">-</span>}</p>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default UserSupport;
