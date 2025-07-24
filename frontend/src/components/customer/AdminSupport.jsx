import { useEffect, useState } from "react";
import axios from "../../api/axiosInstance";
import ReportDetailModal from "../modal/ReportDetailModal";

const AdminSupport = () => {
    const [reports, setReports] = useState([]);
    const [selectedReport, setSelectedReport] = useState(null);
    const [statusFilter, setStatusFilter] = useState("");
    const [error, setError] = useState("");

    const [updatedStatus, setUpdatedStatus] = useState("");
    const [resolverComment, setResolverComment] = useState("");

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

    useEffect(() => {
        fetchReports();
    }, []);

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

    const handleCloseModal = () => {
        setSelectedReport(null);
        setUpdatedStatus("");
        setResolverComment("");
    };

    const handleSubmitUpdate = async () => {
        try {
            await axios.patch(`/reports/${selectedReport.id}`, {
                status: updatedStatus,
                resolverComment,
            });
            handleCloseModal();
            fetchReports(statusFilter); // 필터 반영해서 다시 로드
        } catch (err) {
            console.error(err);
            alert("처리 중 오류가 발생했습니다.");
        }
    };

    return (
        <div className="max-w-5xl mx-auto p-6 min-h-[60vh]">
            <h2 className="text-2xl font-bold mb-4 text-center">전체 신고 목록</h2>

            <div className="mb-6 flex justify-center">
                <label className="mr-2 font-medium">상태 필터:</label>
                <select
                    value={statusFilter}
                    onChange={(e) => {
                        setStatusFilter(e.target.value);
                        fetchReports(e.target.value);
                    }}
                    className="border px-2 py-1 rounded"
                >
                    <option value="">전체</option>
                    <option value="SUBMITTED">SUBMITTED</option>
                    <option value="IN_PROGRESS">IN_PROGRESS</option>
                    <option value="COMPLETED">COMPLETED</option>
                </select>
            </div>

            {error && <p className="text-red-500 text-center">{error}</p>}

            <div className="space-y-4 min-h-[300px] flex flex-col items-center">
                {reports.length === 0 ? (
                    <p className="text-gray-500 text-center">해당 상태의 신고가 없습니다.</p>
                ) : (
                    reports.map((report) => (
                        <div
                            key={report.id}
                            className="w-full max-w-xl border p-4 rounded shadow-sm bg-white cursor-pointer hover:bg-gray-50"
                            onClick={() => handleReportClick(report.id)}
                        >
                            <p><strong>신고자:</strong> {report.reporterNickname}</p>
                            <p><strong>피신고자:</strong> {report.reportedNickname}</p>
                            <p><strong>사유:</strong> {report.reason}</p>
                            <p><strong>상태:</strong> {report.status}</p>
                            <p><strong>처리자:</strong> {report.resolverNickname || "-"}</p>
                            <p><strong>처리일시:</strong> {report.resolvedAt || "-"}</p>
                            <p><strong>처리 의견:</strong> {report.resolverComment?.trim() ? report.resolverComment : "-"}</p>
                        </div>
                    ))
                )}
            </div>

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
