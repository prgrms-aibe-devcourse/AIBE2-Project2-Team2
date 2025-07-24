import { useEffect, useState } from "react";
import axios from "../../api/axiosInstance";

const CATEGORIES = ["욕설", "사기", "허위광고", "기타"];

const UserSupport = () => {
    const [reportedNickname, setReportedNickname] = useState("");
    const [category, setCategory] = useState("");
    const [customReason, setCustomReason] = useState("");
    const [myReports, setMyReports] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

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
            setReportedNickname("");
            setCategory("");
            setCustomReason("");
            await fetchMyReports();
            window.scrollTo({ top: 0, behavior: "smooth" });
        } catch (err) {
            console.error(err);
            if (err.response?.data?.errorCode === "MEMBER_NOT_FOUND") {
                alert(err.response.data.message); // 예: "존재하지 않는 닉네임입니다."
            } else {
                alert("신고 등록에 실패했습니다.");
            }
        }
        finally {
            setLoading(false);
        }
    };


    const fetchMyReports = async () => {
        try {
            const response = await axios.get("/reports/my");
            setMyReports(response.data);
        } catch (err) {
            console.error(err);
            setError("신고 내역을 불러올 수 없습니다.");
        }
    };

    useEffect(() => {
        fetchMyReports();
    }, []);

    return (
        <div className="max-w-5xl mx-auto px-8 py-12">
            <h2 className="text-3xl font-bold mb-8 text-center">신고 접수</h2>

            <form
                onSubmit={handleSubmit}
                className="w-full bg-gray-50 p-8 rounded-lg shadow-md space-y-6"
            >
                <div>
                    <label className="block text-sm font-semibold mb-2">
                        피신고자 닉네임
                    </label>
                    <input
                        type="text"
                        value={reportedNickname}
                        onChange={(e) => setReportedNickname(e.target.value)}
                        required
                        className="w-full border px-4 py-2 rounded-md shadow-sm"
                        placeholder="ex) honggildong"
                    />
                </div>

                <div>
                    <label className="block text-sm font-semibold mb-2">
                        신고 사유 카테고리
                    </label>
                    <select
                        value={category}
                        onChange={(e) => setCategory(e.target.value)}
                        className="w-full border px-4 py-2 rounded-md shadow-sm"
                        required
                    >
                        <option value="">카테고리를 선택하세요</option>
                        {CATEGORIES.map((cat) => (
                            <option key={cat} value={cat}>
                                {cat}
                            </option>
                        ))}
                    </select>
                </div>

                {(category && category !== "") && (
                    <div>
                        <label className="block text-sm font-semibold mb-2">
                            추가 설명 (선택)
                        </label>
                        <textarea
                            value={customReason}
                            onChange={(e) => setCustomReason(e.target.value)}
                            className="w-full border px-4 py-2 rounded-md shadow-sm resize-none"
                            rows={3}
                            placeholder={
                                category === "기타"
                                    ? "기타 신고 내용을 입력하세요"
                                    : "자세한 설명이 있다면 입력해주세요"
                            }
                        />
                    </div>
                )}

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

            <h3 className="text-2xl font-bold mt-16 mb-6">내 신고 내역</h3>

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
                            <p>
                                <strong>피신고자:</strong> {report.reportedNickname}
                            </p>
                            <p>
                                <strong>사유:</strong> {report.reason}
                            </p>
                            <p>
                                <strong>상태:</strong> {report.status}
                            </p>
                            <p>
                                <strong>처리자:</strong>{" "}
                                {report.resolverNickname || (
                                    <span className="text-gray-400">-</span>
                                )}
                            </p>
                            <p>
                                <strong>처리일시:</strong>{" "}
                                {report.resolvedAt || (
                                    <span className="text-gray-400">-</span>
                                )}
                            </p>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default UserSupport;
