import { useEffect, useState } from "react";
import axiosInstance from "../../../lib/axios.js";
// Mock axios instance for demo
// const axiosInstance = {
//     get: async (url, config) => {
//         // Mock data based on backend API example
//         const mockData = [
//             {
//                 paymentId: 12345,
//                 amount: 10000,
//                 paymentDate: "2023-10-01T12:00:00Z",
//                 status: "PAID",
//                 contentId: 6789,
//                 contentTitle: "전문가 이름의 컨텐츠 제목",
//                 expertName: "카키오빠이",
//                 expertProfileImageUrl: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=50&h=50&fit=crop&crop=face"
//             },
//             {
//                 paymentId: 12346,
//                 amount: 15000,
//                 paymentDate: "2023-09-28T14:30:00Z",
//                 status: "PAID",
//                 contentId: 6790,
//                 contentTitle: "마케팅 전략 컨설팅",
//                 expertName: "마케팅 전문가",
//                 expertProfileImageUrl: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=50&h=50&fit=crop&crop=face"
//             },
//             {
//                 paymentId: 12347,
//                 amount: 8000,
//                 paymentDate: "2023-09-25T09:15:00Z",
//                 status: "REFUNDED",
//                 contentId: 6791,
//                 contentTitle: "디자인 피드백 서비스",
//                 expertName: "디자인 멘토",
//                 expertProfileImageUrl: "https://images.unsplash.com/photo-1494790108755-2616b612b786?w=50&h=50&fit=crop&crop=face"
//             }
//         ];
//
//         // Filter by status if provided
//         const filteredData = config?.params?.status
//             ? mockData.filter(item => item.status === config.params.status)
//             : mockData;
//
//         return { data: filteredData };
//     }
// };

const statusFilters = [
    { label: "전체", value: null },
    { label: "결제완료", value: "PAID" },
    { label: "결제실패", value: "FAILED" },
    { label: "결제취소", value: "CANCELLED" }, // 여기 수정
    { label: "환불완료", value: "REFUNDED" },
];

const getStatusText = (status) => {
    switch (status) {
        case "PAID": return "결제완료";
        case "FAILED": return "결제실패";
        case "CANCELLED": return "결제취소";  // 여기 수정
        case "REFUNDED": return "환불완료";
        default: return status;
    }
};

const getStatusColor = (status) => {
    switch (status) {
        case "PAID": return "text-green-600";
        case "FAILED": return "text-red-600";
        case "CANCELLED": return "text-yellow-600"; // 여기 수정
        case "REFUNDED": return "text-blue-600";
        default: return "text-gray-600";
    }
};

export default function PaymentHistory() {
    const [selectedStatus, setSelectedStatus] = useState(null);
    const [payments, setPayments] = useState([]);

    useEffect(() => {
        const fetchPayments = async () => {
            try {
                const response = await axiosInstance.get("/api/me/payments", {
                    params: {
                        status: selectedStatus,
                    },
                });
                setPayments(response.data);
            } catch (error) {
                console.error("결제 내역 조회 실패:", error);
            }
        };

        fetchPayments();
    }, [selectedStatus]);

    return (
        <div className="w-full max-w-4xl mx-auto py-6 px-4">
            {/* Header */}
            <div className="mb-8">
                <h1 className="text-2xl font-bold mb-2">거래내역</h1>
                <p className="text-gray-500 text-sm">결제내역 | 총 {payments.length}개 거래내역</p>
            </div>

            {/* Filter Tabs */}
            <div className="mb-6">
                <div className="flex gap-1 bg-gray-100 p-1 rounded-lg w-fit">
                    {statusFilters.map((filter) => (
                        <button
                            key={filter.label}
                            onClick={() => setSelectedStatus(filter.value)}
                            className={`px-4 py-2 rounded-md text-sm font-medium transition-all ${
                                selectedStatus === filter.value
                                    ? "bg-white text-black shadow-sm"
                                    : "text-gray-600 hover:text-black"
                            }`}
                        >
                            {filter.label}
                        </button>
                    ))}
                </div>
            </div>

            {/* Payment List */}
            {payments.length === 0 ? (
                <div className="text-center py-12">
                    <p className="text-gray-500 text-lg">결제 내역이 없습니다.</p>
                </div>
            ) : (
                <div className="space-y-4">
                    {payments.map((payment) => (
                        <div
                            key={payment.paymentId}
                            className="bg-white border border-gray-200 rounded-xl p-6 hover:shadow-md transition-shadow"
                        >
                            {/* Date Header */}
                            <div className="flex items-center gap-2 mb-4">
                                <span className="text-lg font-semibold">
                                    {new Date(payment.paymentDate).toLocaleDateString('ko-KR', {
                                        month: 'long',
                                        day: 'numeric',
                                        weekday: 'short'
                                    })} {new Date(payment.paymentDate).toLocaleTimeString('ko-KR', {
                                    hour: '2-digit',
                                    minute: '2-digit',
                                    hour12: false
                                })}
                                </span>
                                <span className={`text-sm font-medium ${getStatusColor(payment.status)}`}>
                                    {getStatusText(payment.status)}
                                </span>
                            </div>

                            {/* Content */}
                            <div className="flex items-center gap-4">
                                {/* Profile Image */}
                                <div className="w-12 h-12 rounded-full overflow-hidden flex-shrink-0">
                                    <img
                                        src={payment.expertProfileImageUrl}
                                        alt={payment.expertName}
                                        className="w-full h-full object-cover"
                                    />
                                </div>

                                {/* Content Info */}
                                <div className="flex-grow">
                                    <h3 className="font-semibold text-lg mb-1">
                                        【전문가 이름】{payment.contentTitle}
                                    </h3>
                                    <div className="text-sm text-gray-600 space-y-1">
                                        <p>결제 번호: {payment.paymentId}</p>
                                        <p>결제 수단: 카키오페이</p>
                                        <p>총 결제 금액: {payment.amount.toLocaleString()}원</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}