import React from "react";

const ReportDetailModal = ({ report, onClose, onStatusChange, onCommentChange, onSubmit }) => {
    if (!report) return null;

    // ✅ 날짜 + 시간 포맷 함수
    const formatDateTime = (dateStr) => {
        if (!dateStr) return "-";
        const date = new Date(dateStr);
        const yyyy = date.getFullYear();
        const mm = String(date.getMonth() + 1).padStart(2, "0");
        const dd = String(date.getDate()).padStart(2, "0");
        const hh = String(date.getHours()).padStart(2, "0");
        const min = String(date.getMinutes()).padStart(2, "0");
        const ss = String(date.getSeconds()).padStart(2, "0");
        return (
            <>
                {yyyy}-{mm}-{dd}<br />
                {hh}:{min}:{ss}
            </>
        );
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50">
            <div className="bg-white p-6 rounded-xl shadow-xl w-[90%] max-w-xl">
                <h2 className="text-xl font-bold mb-4">신고 상세보기</h2>

                <div className="space-y-2 text-sm">
                    <p><strong>신고 ID:</strong> {report.id}</p>
                    <p><strong>신고자:</strong> {report.reporterNickname}</p>
                    <p><strong>피신고자:</strong> {report.reportedNickname}</p>
                    <p><strong>카테고리:</strong> {report.category}</p>
                    <p><strong>신고 사유:</strong> {report.reason}</p>
                    <p><strong>등록일:</strong><br /> {formatDateTime(report.createdAt)}</p>
                    <p><strong>상태:</strong>
                        <select
                            value={report.status}
                            onChange={(e) => onStatusChange(e.target.value)}
                            className="ml-2 px-2 py-1 border rounded"
                        >
                            <option value="SUBMITTED">SUBMITTED</option>
                            <option value="IN_PROGRESS">IN_PROGRESS</option>
                            <option value="COMPLETED">COMPLETED</option>
                        </select>
                    </p>

                    {report.resolverNickname && (
                        <p><strong>처리자:</strong> {report.resolverNickname}</p>
                    )}
                    {report.resolvedAt && (
                        <p><strong>처리일시:</strong><br /> {formatDateTime(report.resolvedAt)}</p>
                    )}
                    <div>
                        <strong>처리 의견:</strong>
                        <textarea
                            className="w-full border rounded mt-1 p-2"
                            rows="3"
                            placeholder="처리 의견을 작성하세요"
                            value={report.resolverComment || ""}
                            onChange={(e) => onCommentChange(e.target.value)}
                        />
                    </div>
                </div>

                <div className="mt-4 flex justify-end gap-2">
                    <button onClick={onClose} className="px-4 py-2 rounded bg-gray-300 hover:bg-gray-400">닫기</button>
                    <button onClick={onSubmit} className="px-4 py-2 rounded bg-blue-500 text-white hover:bg-blue-600">저장</button>
                </div>
            </div>
        </div>
    );
};

export default ReportDetailModal;
