import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axiosInstance from "../../lib/axios";

const statusColor = {
    ACCEPTED: "bg-blue-200 text-blue-700",
    IN_PROGRESS: "bg-yellow-200 text-yellow-800",
    WORK_COMPLETED: "bg-green-200 text-green-700",
    CONFIRMED: "bg-gray-300 text-gray-700",
    REJECTED: "bg-red-200 text-red-700",
    REQUESTED: "bg-purple-200 text-purple-700",
};

const statusLabel = {
    ACCEPTED: " 대기 중",
    IN_PROGRESS: "작업 중",
    WORK_COMPLETED: "작업 완료",
    CONFIRMED: "최종 확정",
    REJECTED: "거절됨",
    WAITING_PAYMENT: "결제 대기",
    CANCELLED: "결제 취소됨",
};

export default function MatchingDetailPage() {
    const { matchingId } = useParams();
    const navigate = useNavigate();
    const [matching, setMatching] = useState(null);
    const [loading, setLoading] = useState(true);
    const [myEmail, setMyEmail] = useState("");

    const [rejectMode, setRejectMode] = useState(false);
    const [rejectReason, setRejectReason] = useState("");

    const fetchMatching = async () => {
        setLoading(true);
        try {
            const res = await axiosInstance.get(`/api/matchings/${matchingId}`);
            setMatching(res.data);
        } catch {
            alert("매칭 정보를 불러올 수 없습니다.");
        }
        setLoading(false);
    };

    const fetchMe = async () => {
        try {
            const res = await axiosInstance.get("/api/me");
            setMyEmail(res.data.email);
        } catch {
            alert("로그인이 필요합니다.");
        }
    };

    useEffect(() => {
        fetchMe();
        fetchMatching();
        // eslint-disable-next-line
    }, [matchingId]);

    const handleStartWork = async () => {
        if (!window.confirm("정말 작업을 시작하시겠습니까?")) return;
        try {
            await axiosInstance.patch(`/api/matchings/${matchingId}/start`);
            alert("작업을 시작했습니다.");
            fetchMatching();
        } catch (e) {
            alert(e?.response?.data?.message || "작업 시작 실패");
        }
    };

    const handleReject = async () => {
        if (!rejectReason.trim()) {
            alert("거절 사유를 입력하세요.");
            return;
        }
        try {
            await axiosInstance.patch(`/api/matchings/${matchingId}/status`, {
                status: "REJECTED",
                reason: rejectReason,
            });
            alert("작업을 거절했습니다.");
            setRejectMode(false);
            setRejectReason("");
            navigate(-1);
        } catch (e) {
            alert(e?.response?.data?.message || "작업 거절 실패");
        }
    };

    const handleCompleteWork = async () => {
        if (!window.confirm("작업을 완료하시겠습니까?")) return;
        try {
            await axiosInstance.patch(`/api/matchings/${matchingId}/complete`);
            alert("작업을 완료 처리했습니다.");
            fetchMatching();
        } catch (e) {
            alert(e?.response?.data?.message || "작업 완료 실패");
        }
    };

    if (loading) return <div className="text-center py-12">로딩중...</div>;
    if (!matching) return <div className="text-center py-12">매칭 정보를 불러올 수 없습니다.</div>;
    if (myEmail !== matching.expertEmail) return <div className="text-center py-12 text-red-500 font-bold">접근 권한이 없습니다.</div>;

    return (
        <div className="w-full min-h-screen bg-gradient-to-b from-slate-50 to-slate-100 flex flex-col">
            <div className="flex-1 flex items-start justify-center pt-36 pb-16">
                <div className="max-w-3xl w-full bg-white rounded-3xl shadow-2xl p-12 flex flex-col gap-8">
                    <div className="flex justify-between items-center border-b pb-5">
                        <div>
                            <h2 className="text-3xl font-extrabold tracking-tight text-slate-900 mb-1">매칭 상세</h2>
                            <div className="text-slate-400 text-lg">매칭 ID #{matching.matchingId}</div>
                        </div>
                        <span className={`px-4 py-2 rounded-xl text-base font-bold ${statusColor[matching.status]}`}>
                            {statusLabel[matching.status] || matching.status}
                        </span>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
                        <div className="flex flex-col gap-5">
                            <div>
                                <div className="text-gray-500 text-sm font-bold mb-1">의뢰자 이메일</div>
                                <div>{matching.memberEmail}</div>
                            </div>
                            <div>
                                <div className="text-gray-500 text-sm font-bold mb-1">콘텐츠 제목</div>
                                <div>{matching.contentTitle}</div>
                            </div>
                            <div>
                                <div className="text-gray-500 text-sm font-bold mb-1">견적 총액</div>
                                <div className="text-xl font-bold text-blue-700">{matching.totalPrice?.toLocaleString()} 원</div>
                            </div>
                            <div className="flex gap-8">
                                <div>
                                    <div className="text-gray-500 text-sm font-bold mb-1">시작일</div>
                                    <div>{matching.startDate || "-"}</div>
                                </div>
                                <div>
                                    <div className="text-gray-500 text-sm font-bold mb-1">종료일</div>
                                    <div>{matching.endDate || "-"}</div>
                                </div>
                            </div>
                            <div>
                                <div className="text-gray-500 text-sm font-bold mb-1">거절사유</div>
                                <div>{matching.rejectedReason || <span className="text-gray-300">-</span>}</div>
                            </div>
                        </div>
                        <div>
                            <div className="text-gray-500 text-sm font-bold mb-3">견적 항목</div>
                            <div className="flex flex-col gap-2">
                                {Array.isArray(matching.items) && matching.items.length > 0 ? (
                                    matching.items.map((item, idx) => (
                                        <div
                                            key={idx}
                                            className="flex justify-between items-center bg-slate-50 px-4 py-2 rounded-xl border border-slate-100"
                                        >
                                            <span className="font-medium">{item.name}</span>
                                            <span className="font-bold text-blue-700">{item.price?.toLocaleString()} 원</span>
                                        </div>
                                    ))
                                ) : (
                                    <div className="text-gray-300">견적 항목 없음</div>
                                )}
                            </div>
                        </div>
                    </div>
                    <div className="flex flex-wrap gap-4 justify-end pt-6 border-t">
                        {matching.status === "ACCEPTED" && (
                            <>
                                <button
                                    className="px-8 py-3 rounded-2xl bg-gradient-to-r from-blue-500 to-indigo-600 text-white font-bold text-lg shadow-lg hover:from-indigo-600 hover:to-blue-700 transition-all duration-200"
                                    onClick={handleStartWork}
                                >
                                    작업 시작
                                </button>
                                {!rejectMode ? (
                                    <button
                                        className="px-8 py-3 rounded-2xl bg-gradient-to-r from-pink-500 to-red-400 text-white font-bold text-lg shadow-lg hover:from-red-500 hover:to-pink-400 transition-all duration-200"
                                        onClick={() => setRejectMode(true)}
                                    >
                                        작업 거절
                                    </button>
                                ) : (
                                    <div className="flex gap-2 items-center">
                                        <input
                                            type="text"
                                            placeholder="거절 사유 입력"
                                            className="border p-2 rounded focus:ring focus:ring-red-100 w-60"
                                            value={rejectReason}
                                            onChange={(e) => setRejectReason(e.target.value)}
                                        />
                                        <button
                                            className="px-6 py-2 rounded-xl bg-gradient-to-r from-pink-500 to-red-400 text-white font-bold shadow-md hover:from-red-500 hover:to-pink-400 transition-all duration-200"
                                            onClick={handleReject}
                                        >
                                            거절 확정
                                        </button>
                                        <button
                                            className="px-6 py-2 rounded-xl bg-gray-200 text-gray-700 font-bold shadow-sm hover:bg-gray-300 transition"
                                            onClick={() => {
                                                setRejectMode(false);
                                                setRejectReason("");
                                            }}
                                        >
                                            취소
                                        </button>
                                    </div>
                                )}
                            </>
                        )}
                        {matching.status === "IN_PROGRESS" && (
                            <button
                                className="px-8 py-3 rounded-2xl bg-gradient-to-r from-green-500 to-emerald-400 text-white font-bold text-lg shadow-lg hover:from-emerald-500 hover:to-green-700 transition-all duration-200"
                                onClick={handleCompleteWork}
                            >
                                작업 완료
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
