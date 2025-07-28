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
    WAITING_PAYMENT: "bg-sky-200 text-sky-700",
    CANCELLED: "bg-gray-200 text-gray-500",
};

const statusLabel = {
    ACCEPTED: "작업 대기 중",
    IN_PROGRESS: "작업 중",
    WORK_COMPLETED: "작업 완료",
    CONFIRMED: "최종 확정",
    REJECTED: "거절됨",
    REQUESTED: "매칭 요청",
    WAITING_PAYMENT: "결제 대기",
    CANCELLED: "결제 취소됨",
};

export default function MatchingClientDetailPage() {
    const { matchingId } = useParams();
    const navigate = useNavigate();

    const [matching, setMatching] = useState(null);
    const [loading, setLoading] = useState(true);
    const [me, setMe] = useState(null); // 현재 로그인한 사용자 정보

    // 현재 로그인 유저 정보 조회
    const fetchMe = async () => {
        try {
            const res = await axiosInstance.get("/api/me");
            setMe(res.data);
        } catch (e) {
            alert("로그인이 필요합니다.");
            navigate("/login");
        }
    };

    const fetchMatching = async () => {
        setLoading(true);
        try {
            const res = await axiosInstance.get(`/api/matchings/${matchingId}`);
            setMatching(res.data);
        } catch (e) {
            if (e?.response?.status === 403) {
                alert("접근 권한이 없습니다.");
                navigate("/");
            } else {
                alert("매칭 정보를 불러올 수 없습니다.");
            }
        }
        setLoading(false);
    };

    useEffect(() => {
        fetchMe();
    }, []);

    useEffect(() => {
        if (me) fetchMatching();
        // eslint-disable-next-line
    }, [me]);

    const handleConfirmPurchase = async () => {
        if (!window.confirm("작업 완료를 확정하시겠습니까?")) return;
        try {
            await axiosInstance.patch(`/api/matchings/${matchingId}/confirm`);
            alert("구매가 확정되었습니다.");

            // 리뷰 작성 여부 확인
            const writeReview = window.confirm("리뷰를 작성하시겠습니까?");
            if (writeReview) {
                navigate(`/review/write/${matchingId}`);
            } else {
                fetchMatching(); // 리뷰 작성하지 않으면 현재 페이지에서 상태만 업데이트
            }
        } catch (e) {
            alert(e?.response?.data?.message || "구매 확정 실패");
        }
    };

    if (loading) return <div className="text-center py-12">로딩중...</div>;
    if (!matching) return <div className="text-center py-12">매칭 정보를 불러올 수 없습니다.</div>;

    const isClient = me?.email === matching.memberEmail;

    return (
        <div className="w-full min-h-screen bg-gradient-to-b from-slate-50 to-slate-100 flex flex-col">
            <div className="flex-1 flex items-start justify-center pt-36 pb-16">
                <div className="max-w-3xl w-full bg-white rounded-3xl shadow-2xl p-12 flex flex-col gap-8">
                    {/* 헤더 */}
                    <div className="flex justify-between items-center border-b pb-5">
                        <div>
                            <h2 className="text-3xl font-extrabold tracking-tight text-slate-900 mb-1">매칭 상세</h2>
                            <div className="text-slate-400 text-lg">매칭 ID #{matching.matchingId}</div>
                        </div>
                        <span className={`px-4 py-2 rounded-xl text-base font-bold ${statusColor[matching.status]}`}>
                            {statusLabel[matching.status] || matching.status}
                        </span>
                    </div>

                    {/* 정보 섹션 */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
                        <div className="flex flex-col gap-5">
                            <div>
                                <div className="text-gray-500 text-sm font-bold mb-1">전문가 이메일</div>
                                <div>{matching.expertEmail || <span className="text-gray-300">-</span>}</div>
                            </div>
                            <div>
                                <div className="text-gray-500 text-sm font-bold mb-1">콘텐츠 제목</div>
                                <div>{matching.contentTitle || <span className="text-gray-300">-</span>}</div>
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

                    {/* 버튼 섹션 */}
                    <div className="flex flex-wrap gap-4 justify-end pt-6 border-t">
                        {isClient && matching.status === "WORK_COMPLETED" && (
                            <button
                                className="px-8 py-3 rounded-2xl bg-gradient-to-r from-green-500 to-emerald-400 text-white font-bold text-lg shadow-lg hover:from-emerald-500 hover:to-green-700 transition-all duration-200"
                                onClick={handleConfirmPurchase}
                            >
                                구매 확정
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
