import { useEffect, useState } from "react";
import ReportDetailModal from "../modal/ReportDetailModal";
import axios from "axios";

const AdminSupport = () => {
    const [reports, setReports] = useState([]);
    const [selectedReport, setSelectedReport] = useState(null);
    const [statusFilter, setStatusFilter] = useState("");
    const [error, setError] = useState("");
    const [updatedStatus, setUpdatedStatus] = useState("");
    const [resolverComment, setResolverComment] = useState("");

    // 전체 신고 목록 조회 (필터 포함)
    const fetchReports = async (status = "") => {
        try {
            const res = await axios.get("/reports", {
                params: status ? { status } : {},
            });
            setReports(res.data);
            setError("");
        } catch (err) {
            console.error(err);
            setError("신고 목록을 불러오지 못했습니다.");
        }
    };

    // 페이지 초기 진입 시 전체 신고 불러오기
    useEffect(() => {
        fetchReports();
    }, []);

    // 특정 신고 클릭 시 상세 조회 후 모달 오픈
    const handleReportClick = async (id) => {
        try {
            const res = await axios.get(`/reports/${id}`);
            setSelectedReport(res.data);
            setUpdatedStatus(res.data.status);
            setResolverComment(res.data.resolverComment || "");
        } catch (err) {
            console.error(err);
            alert("신고 상세 정보를 불러오는 데 실패했습니다.");
        }
    };

    // 모달 닫기
    const handleCloseModal = () => {
        setSelectedReport(null);
        setUpdatedStatus("");
        setResolverComment("");
    };

    // 상태 및 처리 의견 저장 요청
    const handleSubmitUpdate = async () => {
        try {
            await axios.patch(`/reports/${selectedReport.id}`, {
                status: updatedStatus,
                resolverComment,
            });
            handleCloseModal();
            fetchReports(statusFilter);
        } catch (err) {
            console.error(err);
            alert("처리 중 오류가 발생했습니다.");
        }
    };

    // 신고 삭제 처리
    const handleDeleteReport = async (id, e) => {
        e.stopPropagation();
        if (!window.confirm("정말 이 신고를 삭제하시겠습니까?")) return;
        try {
            await axios.delete(`/reports/${id}`);
            alert("신고가 삭제되었습니다.");
            fetchReports(statusFilter);
        } catch (err) {
            console.error(err);
            alert("삭제에 실패했습니다.");
        }
    };

    // 날짜 포맷 YYYY-MM-DD HH:MM:SS 형태로 반환
    const formatDateTime = (dateStr) => {
        if (!dateStr) return "-";
        const date = new Date(dateStr);
        const yyyy = date.getFullYear();
        const mm = String(date.getMonth() + 1).padStart(2, "0");
        const dd = String(date.getDate()).padStart(2, "0");
        const hh = String(date.getHours()).padStart(2, "0");
        const min = String(date.getMinutes()).padStart(2, "0");
        const ss = String(date.getSeconds()).padStart(2, "0");
        return `${yyyy}-${mm}-${dd} ${hh}:${min}:${ss}`;
    };

    // 신고 상태에 따른 뱃지 색상 반환
    const getStatusBadgeColor = (status) => {
        switch (status) {
            case "COMPLETED":
                return "bg-green-500";
            case "IN_PROGRESS":
                return "bg-yellow-500 text-black";
            case "SUBMITTED":
            default:
                return "bg-gray-500";
        }
    };

    return (
        <div className="max-w-6xl mx-auto p-6 min-h-[60vh]">
            {/* 헤더 영역: 제목 + 필터 */}
            <div className="mb-8 text-left border-b border-gray-300 pb-4">
                <h2 className="text-3xl font-extrabold text-gray-800 mb-2">
                    전체 신고 목록
                </h2>
                <div className="flex items-center gap-2 mt-1 w-64">
                    <label className="text-sm font-medium text-gray-700 whitespace-nowrap">
                        상태 필터 :
                    </label>
                    <div className="relative w-full">
                        <select
                            value={statusFilter}
                            onChange={(e) => {
                                setStatusFilter(e.target.value);
                                fetchReports(e.target.value);
                            }}
                            className="w-full appearance-none border border-gray-300 rounded-md px-4 py-2 pr-10 text-sm bg-white shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-blue-500 transition"
                        >
                            <option value="">전체</option>
                            <option value="SUBMITTED">SUBMITTED</option>
                            <option value="IN_PROGRESS">IN_PROGRESS</option>
                            <option value="COMPLETED">COMPLETED</option>
                        </select>
                        <div className="pointer-events-none absolute inset-y-0 right-2 flex items-center text-gray-500 text-xs">
                            ▼
                        </div>
                    </div>
                </div>
            </div>

            {/* 신고 목록 or 빈 메시지 */}
            {error && <p className="text-red-500">{error}</p>}

            {reports.length === 0 ? (
                <div className="text-left w-full">
                    <p className="text-gray-500">해당 상태의 신고가 없습니다.</p>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {reports.map((report) => (
                        <div
                            key={report.id}
                            onClick={() => handleReportClick(report.id)}
                            className="relative border border-gray-200 p-5 rounded-lg shadow-sm bg-white hover:shadow-md transition cursor-pointer"
                        >
                            <div className="flex justify-between items-start gap-4">
                                <h3 className="font-bold text-lg break-words max-w-[75%] leading-snug">
                                    {report.reason}
                                </h3>
                                <div className="flex gap-2 shrink-0">
                                    <span className={`text-white text-xs px-2 py-1 rounded ${getStatusBadgeColor(report.status)}`}>
                                        {report.status}
                                    </span>
                                    <button
                                        onClick={(e) => handleDeleteReport(report.id, e)}
                                        className="text-xs px-2 py-1 bg-red-500 text-white rounded hover:bg-red-600"
                                    >
                                        삭제
                                    </button>
                                </div>
                            </div>

                            <div className="text-sm text-gray-700 space-y-1 mt-4">
                                <p><strong>신고자:</strong> {report.reporterNickname}</p>
                                <p><strong>피신고자:</strong> {report.reportedNickname}</p>
                                <p><strong>카테고리:</strong> {report.category}</p>
                                <p><strong>처리자:</strong> {report.resolverNickname || "-"}</p>
                                <p><strong>처리일시:</strong> {formatDateTime(report.resolvedAt)}</p>
                                <p><strong>처리 의견:</strong> {report.resolverComment?.trim() || "-"}</p>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* 신고 상세 모달 */}
            {selectedReport && (
                <ReportDetailModal
                    report={{
                        ...selectedReport,
                        status: updatedStatus,
                        resolverComment,
                    }}
                    onClose={handleCloseModal}
                    onStatusChange={setUpdatedStatus}
                    onCommentChange={setResolverComment}
                    onSubmit={handleSubmitUpdate}
                />
            )}
        </div>
    );
};

export default AdminSupport;
