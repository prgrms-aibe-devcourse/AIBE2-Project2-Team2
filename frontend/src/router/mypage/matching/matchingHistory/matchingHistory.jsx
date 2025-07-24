import { useEffect, useState } from "react";
import dayjs from "dayjs";
// import axiosInstance from "../../../../lib/axios";
// mock 데이터 30개 생성 (user/expert 구분)
// const mockData = Array.from({ length: 30 }, (_, i) => {
//   const isExpert = i % 2 === 0;
//   return {
//     matchingId: i + 1,
//     contentTitle: isExpert ? `전문가 프로젝트 ${i + 1}` : `유저 프로젝트 ${i + 1}`,
//     contentThumbnailUrl: `https://picsum.photos/seed/${i + 1}/200/200`,
//     expertName: isExpert ? `전문가${i + 1}` : `유저${i + 1}`,
//     expertProfileImageUrl: `https://i.pravatar.cc/50?img=${(i % 70) + 1}`,
//     expertPhone: `010-${String(1000 + i).padStart(4, "0")}-${String(1000 + i * 2).padStart(4, "0")}`,
//     matchingStatus: ["ACCEPTED", "IN_PROGRESS", "COMPLETED", "CANCELLED"][i % 4],
//     workStartDate: `2024-${String((i % 12) + 1).padStart(2, "0")}-01`,
//     workEndDate: `2024-${String((i % 12) + 1).padStart(2, "0")}-15`,
//     totalPrice: 100000 * ((i % 5) + 1),
//     selectedItems: [
//       { itemName: `서비스A-${i + 1}`, itemPrice: 10000 * ((i % 3) + 1) },
//       { itemName: `서비스B-${i + 1}`, itemPrice: 20000 * ((i % 2) + 1) },
//     ],
//     isExpert,
//   };
// });
//
// mock axiosInstance 구현 (user/expert 분기, 페이징, 필터)
const mockAxiosInstance = {
  get: async (url, { params } = {}) => {
    let filtered = [...mockData];
    // user/expert 분기
    if (url.includes("expert")) {
      filtered = filtered.filter((item) => item.isExpert);
    } else {
      filtered = filtered.filter((item) => !item.isExpert);
    }
    // 필터
    if (params?.matchingStatus) filtered = filtered.filter((item) => item.matchingStatus === params.matchingStatus);
    if (params?.nickname) filtered = filtered.filter((item) => item.expertName.includes(params.nickname));
    if (params?.matchingId) filtered = filtered.filter((item) => item.matchingId === parseInt(params.matchingId));
    // fromMonth/toMonth 필터 (YYYY-MM)
    if (params?.fromMonth) filtered = filtered.filter((item) => item.workStartDate.slice(0, 7) >= params.fromMonth);
    if (params?.toMonth) filtered = filtered.filter((item) => item.workEndDate.slice(0, 7) <= params.toMonth);
    // 페이징
    const page = params?.page ?? 0;
    const size = params?.size ?? 5;
    const totalElements = filtered.length;
    const totalPages = Math.max(1, Math.ceil(totalElements / size));
    const startIdx = page * size;
    const endIdx = startIdx + size;
    const content = filtered.slice(startIdx, endIdx);
    return {
      data: {
        content,
        pageable: {
          pageNumber: page,
          pageSize: size,
          offset: startIdx,
          paged: true,
          unpaged: false,
        },
        totalPages,
        totalElements,
        last: page + 1 >= totalPages,
        size,
        number: page,
        sort: {
          sorted: true,
          unsorted: false,
          empty: false,
        },
        numberOfElements: content.length,
        first: page === 0,
        empty: content.length === 0,
      },
    };
  },
};
import { useUserInfoStore } from "../../../../store/userInfo";
import axiosInstance from "../../../../lib/axios.js";
// import axiosInstance from "../../../lib/axios.js";

// Mock axios instance for demo (페이징 구조 포함)
const statusFilters = [
  { label: "전체", value: null },
  { label: "매칭성사", value: "ACCEPTED" },
  { label: "진행중", value: "IN_PROGRESS" },
  { label: "완료", value: "COMPLETED" },
  { label: "취소", value: "CANCELLED" },
];

const getStatusText = (status) => {
  switch (status) {
    case "ACCEPTED":
      return "매칭성사";
    case "IN_PROGRESS":
      return "진행중";
    case "COMPLETED":
      return "완료";
    case "CANCELLED":
      return "취소";
    default:
      return status;
  }
};

const getStatusColor = (status) => {
  switch (status) {
    case "ACCEPTED":
      return "text-blue-600";
    case "IN_PROGRESS":
      return "text-orange-600";
    case "COMPLETED":
      return "text-green-600";
    case "CANCELLED":
      return "text-red-600";
    default:
      return "text-gray-600";
  }
};

export default function MatchingHistory() {
  const { userInfo } = useUserInfoStore();
  const [selectedStatus, setSelectedStatus] = useState(null);
  const [matchings, setMatchings] = useState([]);
  const [pageInfo, setPageInfo] = useState({
    pageNumber: 0,
    totalPages: 1,
    totalElements: 0,
    size: 5,
    last: true,
    first: true,
  });
  const [searchFilters, setSearchFilters] = useState({
    fromMonth: "",
    toMonth: "",
    nickname: "",
    matchingId: "",
  });
  const [firstLoad, setFirstLoad] = useState(true);
  const [showFilters, setShowFilters] = useState(false);
  // 유저/전문가 선택 (expert만 노출)
  const [viewType, setViewType] = useState("user");

  // 조회 버튼을 눌렀을 때만 fetchMatchings 실행
  const fetchMatchings = async (customStatus = null, customFilters = null, isFirst = false, page = 0) => {
    try {
      let params = {
        page,
        size: pageInfo.size || 5,
      };
      if (!isFirst) {
        if ((customStatus ?? selectedStatus) !== null) params.matchingStatus = customStatus ?? selectedStatus;
        const filters = customFilters ?? searchFilters;
        Object.entries(filters).forEach(([key, value]) => {
          if (value !== "" && value !== null) params[key] = value;
        });
      }
      // user/expert API 분기
      const endpoint = viewType === "expert" ? "/api/matching-histories/expert" : "/api/matching-histories/user";
      const response = await axiosInstance.get(endpoint, { params });
      if (Array.isArray(response.data)) {
        setMatchings(response.data);
        setPageInfo({ pageNumber: 0, totalPages: 2, totalElements: response.data.length, size: 2, last: true, first: true });
      } else {
        setMatchings(response.data.content || []);
        setPageInfo({
          pageNumber: response.data.pageable?.pageNumber ?? 0,
          totalPages: response.data.totalPages ?? 1,
          totalElements: response.data.totalElements ?? 0,
          size: response.data.size ?? 5,
          last: response.data.last ?? true,
          first: response.data.first ?? true,
        });
      }
    } catch (error) {
      console.error("매칭 내역 조회 실패:", error);
    }
  };

  // 첫 진입 시 전체 출력 (쿼리스트링 없이)
  useEffect(() => {
    fetchMatchings(null, null, true);
    setFirstLoad(false);
    // eslint-disable-next-line
  }, []);

  // 조회 버튼
  const handleSearch = () => {
    fetchMatchings(null, null, false, 0);
  };

  // 페이지 이동
  const handlePageChange = (page) => {
    fetchMatchings(null, null, false, page);
  };

  const handleStatusSelect = (value) => {
    setSelectedStatus(value);
  };

  const handleFilterChange = (key, value) => {
    setSearchFilters((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  const clearFilters = () => {
    setSearchFilters({
      fromMonth: "",
      toMonth: "",
      nickname: "",
      matchingId: "",
    });
    setSelectedStatus(null);
  };

  return (
    <div className="w-full max-w-4xl mx-auto py-6 px-4">
      {/* 유저/전문가 선택 (expert만 노출) */}
      {userInfo?.role === "EXPERT" && (
        <div className="mb-4 flex gap-2">
          <button className={`px-4 py-2 rounded-md border text-sm font-medium ${viewType === "user" ? "bg-blue-600 text-white border-blue-600" : "bg-white text-gray-700 border-gray-300 hover:bg-blue-50"}`} onClick={() => setViewType("user")} disabled={viewType === "user"}>
            유저로 보기
          </button>
          <button className={`px-4 py-2 rounded-md border text-sm font-medium ${viewType === "expert" ? "bg-blue-600 text-white border-blue-600" : "bg-white text-gray-700 border-gray-300 hover:bg-blue-50"}`} onClick={() => setViewType("expert")} disabled={viewType === "expert"}>
            전문가로 보기
          </button>
        </div>
      )}
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold mb-2">매칭내역</h1>
        <p className="text-gray-500 text-sm">매칭내역 | 총 {pageInfo.totalElements}개 매칭내역</p>
      </div>

      {/* Filter Tabs */}
      <div className="mb-6">
        <div className="flex gap-1 bg-gray-100 p-1 rounded-lg w-fit">
          {statusFilters.map((filter) => (
            <button key={filter.label} onClick={() => handleStatusSelect(filter.value)} className={`px-4 py-2 rounded-md text-sm font-medium transition-all ${selectedStatus === filter.value ? "bg-white text-black shadow-sm" : "text-gray-600 hover:text-black"}`}>
              {filter.label}
            </button>
          ))}
        </div>
      </div>

      {/* Advanced Filters */}
      <div className="mb-6">
        <button onClick={() => setShowFilters(!showFilters)} className="flex items-center gap-2 text-sm text-gray-600 hover:text-black transition-colors">
          <span>상세 필터</span>
          <span className={`transform transition-transform ${showFilters ? "rotate-180" : ""}`}>▼</span>
        </button>

        {showFilters && (
          <div className="mt-4 p-4 bg-gray-50 rounded-lg">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">시작 월</label>
                <input type="month" value={searchFilters.fromMonth} onChange={(e) => handleFilterChange("fromMonth", e.target.value)} className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">종료 월</label>
                <input type="month" value={searchFilters.toMonth} onChange={(e) => handleFilterChange("toMonth", e.target.value)} className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">전문가 닉네임</label>
                <input type="text" placeholder="전문가 닉네임 입력" value={searchFilters.nickname} onChange={(e) => handleFilterChange("nickname", e.target.value)} className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">매칭 ID</label>
                <input type="number" placeholder="매칭 ID 입력" value={searchFilters.matchingId} onChange={(e) => handleFilterChange("matchingId", e.target.value)} className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
            </div>
            <div className="flex gap-2 mt-4">
              <button onClick={handleSearch} className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors">
                검색
              </button>
              <button onClick={clearFilters} className="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 transition-colors">
                초기화
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Matching List */}
      {matchings.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg">매칭 내역이 없습니다.</p>
        </div>
      ) : (
        <>
          <div className="space-y-4">
            {matchings.map((matching) => (
              <div key={matching.matchingId} className="bg-white border border-gray-200 rounded-xl p-6 hover:shadow-md transition-shadow">
                {/* Date Header */}
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center gap-2">
                    <span className="text-lg font-semibold">
                      {dayjs(matching.workStartDate).format("YYYY.MM.DD")} ~ {dayjs(matching.workEndDate).format("YYYY.MM.DD")}
                    </span>
                    <span className={`text-sm font-medium ${getStatusColor(matching.matchingStatus)}`}>{getStatusText(matching.matchingStatus)}</span>
                  </div>
                  <span className="text-sm text-gray-500">매칭 ID: {matching.matchingId}</span>
                </div>

                {/* Content */}
                <div className="flex items-start gap-4">
                  {/* Content Thumbnail */}
                  <div className="w-16 h-16 rounded-lg overflow-hidden flex-shrink-0">
                    <img src={matching.contentThumbnailUrl} alt={matching.contentTitle} className="w-full h-full object-cover" />
                  </div>

                  {/* Main Content */}
                  <div className="flex-grow">
                    <h3 className="font-semibold text-lg mb-2">{matching.contentTitle}</h3>

                    {/* Expert Info */}
                    <div className="flex items-center gap-3 mb-3">
                      <div className="w-8 h-8 rounded-full overflow-hidden">
                        <img src={matching.expertProfileImageUrl} alt={matching.expertName} className="w-full h-full object-cover" />
                      </div>
                      <span className="text-sm font-medium">{matching.expertName}</span>
                      <span className="text-sm text-gray-500">{matching.expertPhone}</span>
                    </div>

                    {/* Selected Items */}
                    <div className="mb-3">
                      <p className="text-sm text-gray-600 mb-1">선택된 서비스:</p>
                      <div className="flex flex-wrap gap-2">
                        {matching.selectedItems.map((item, index) => (
                          <span key={index} className="text-xs bg-gray-100 px-2 py-1 rounded">
                            {item.itemName} ({item.itemPrice.toLocaleString()}원)
                          </span>
                        ))}
                      </div>
                    </div>

                    <div className="flex justify-between items-center">
                      {/* Total Price */}
                      <div className="text-right mb-2">
                        <span className="text-lg font-bold text-blue-600">총 {matching.totalPrice.toLocaleString()}원</span>
                      </div>
                      {/* 상세 페이지 이동 버튼 */}
                      <div className="text-right">
                        <button
                          className="px-4 py-1 bg-gray-100 text-gray-700 rounded hover:bg-blue-100 border border-gray-300 text-sm"
                          // TODO: onClick={() => navigate(`/matching-history/${matching.matchingId}`)}
                          type="button">
                          상세 보기
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
          {/* Pagination */}
          <div className="flex justify-center mt-8 gap-2">
            {Array.from({ length: pageInfo.totalPages }, (_, i) => (
              <button key={i} onClick={() => handlePageChange(i)} className={`px-3 py-1 rounded border text-sm font-medium ${pageInfo.pageNumber === i ? "bg-blue-600 text-white border-blue-600" : "bg-white text-gray-700 border-gray-300 hover:bg-blue-50"}`} disabled={pageInfo.pageNumber === i}>
                {i + 1}
              </button>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
