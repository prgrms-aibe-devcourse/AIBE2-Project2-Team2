import { useEffect, useState } from "react";
import dayjs from "dayjs";
import axiosInstance from "../../../../lib/axios.js";
import { useUserInfoStore } from "../../../../store/userInfo.js";

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

  // 유저/전문가 선택 - EXPERT만 사용 가능, 일반 USER는 항상 "user" 뷰
  const [viewType, setViewType] = useState(() => {
    // EXPERT가 아닌 경우 항상 "user" 뷰로 고정
    return userInfo?.role === "EXPERT" ? "user" : "user";
  });

  // 초기 상태 저장 (필터 변화 감지용)
  const [initialFilters, setInitialFilters] = useState({
    status: null,
    searchFilters: {
      fromMonth: "",
      toMonth: "",
      nickname: "",
      matchingId: "",
    },
  });

  // 사용자 역할이 변경되면 viewType도 적절히 조정
  useEffect(() => {
    if (userInfo?.role !== "EXPERT") {
      setViewType("user");
    }
  }, [userInfo?.role]);

  // 필터 변화 감지
  const hasFilterChanges = () => {
    const statusChanged = selectedStatus !== initialFilters.status;
    const filtersChanged = Object.keys(searchFilters).some((key) => searchFilters[key] !== initialFilters.searchFilters[key]);
    return statusChanged || filtersChanged;
  };

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
    // 초기 상태 설정
    setInitialFilters({
      status: null,
      searchFilters: {
        fromMonth: "",
        toMonth: "",
        nickname: "",
        matchingId: "",
      },
    });
    // eslint-disable-next-line
  }, [viewType]); // viewType 변경 시에도 다시 fetch

  // 조회 버튼
  const handleSearch = () => {
    fetchMatchings(null, null, false, 0);
    // 검색 후 현재 상태를 초기 상태로 업데이트
    setInitialFilters({
      status: selectedStatus,
      searchFilters: { ...searchFilters },
    });
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
    // 초기화 후 초기 상태도 업데이트
    setInitialFilters({
      status: null,
      searchFilters: {
        fromMonth: "",
        toMonth: "",
        nickname: "",
        matchingId: "",
      },
    });
  };

  return (
    <div className="w-full max-w-3xl mx-auto py-6 px-4">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold mb-2">매칭내역</h1>
        <p className="text-gray-500 text-sm">매칭내역 | 총 {pageInfo.totalElements}개 매칭내역</p>
      </div>

      {/* Combined Filters */}
      <div className="mb-6">
        <div className="p-4 bg-gray-50 rounded-lg grid grid-cols-4 gap-4">
          {/* Status Filter Tabs */}
          <div className="mb-4 col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-1">매칭 상태</label>
            <div className="flex gap-1 bg-gray-100 p-1 rounded-lg w-full">
              {statusFilters.map((filter) => (
                <button key={filter.label} onClick={() => handleStatusSelect(filter.value)} className={`w-full py-2 rounded-md text-sm font-medium transition-all ${selectedStatus === filter.value ? "bg-white text-black shadow-sm" : "text-gray-600 hover:text-black"}`}>
                  {filter.label}
                </button>
              ))}
            </div>
          </div>
          <div className="flex-1 col-span-1">
            <label className="block text-sm font-medium text-gray-700 mb-1">시작 월</label>
            <input type="month" value={searchFilters.fromMonth} onChange={(e) => handleFilterChange("fromMonth", e.target.value)} className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
          <div className="flex-1 col-span-1">
            <label className="block text-sm font-medium text-gray-700 mb-1">종료 월</label>
            <input type="month" value={searchFilters.toMonth} onChange={(e) => handleFilterChange("toMonth", e.target.value)} className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>

          <div className="flex-1 col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-1">{viewType === "expert" ? "의뢰인 닉네임" : "전문가 닉네임"}</label>
            <input
              type="text"
              placeholder={viewType === "expert" ? "의뢰인 닉네임 입력" : "전문가 닉네임 입력"}
              value={searchFilters.nickname}
              onChange={(e) => handleFilterChange("nickname", e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div className="flex-1 col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-1">매칭 ID</label>
            <input type="number" placeholder="매칭 ID 입력" value={searchFilters.matchingId} onChange={(e) => handleFilterChange("matchingId", e.target.value)} className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>

          {/* View Type Switch - EXPERT 권한일 때만 표시 */}
          {userInfo?.role === "EXPERT" ? (
            <div className="col-span-2 flex justify-center items-center">
              <div className="relative inline-flex items-center bg-gray-200 rounded-full p-1">
                <button onClick={() => setViewType("user")} className={`relative px-6 py-2 text-sm font-medium rounded-full transition-all duration-200 ${viewType === "user" ? "bg-white text-blue-600 shadow-sm" : "text-gray-600 hover:text-gray-800"}`}>
                  의뢰인으로 보기
                </button>
                <button onClick={() => setViewType("expert")} className={`relative px-6 py-2 text-sm font-medium rounded-full transition-all duration-200 ${viewType === "expert" ? "bg-white text-blue-600 shadow-sm" : "text-gray-600 hover:text-gray-800"}`}>
                  전문가로 보기
                </button>
              </div>
            </div>
          ) : (
            // 일반 사용자인 경우 빈 공간으로 처리
            <div className="col-span-2"></div>
          )}

          {/* Action Buttons */}
          <button onClick={handleSearch} disabled={!hasFilterChanges()} className={`col-span-1 h-10 px-4 py-2 rounded-md transition-colors flex items-center justify-center ${hasFilterChanges() ? "bg-blue-600 text-white hover:bg-blue-700" : "bg-gray-300 text-gray-500 cursor-not-allowed"}`}>
            검색
          </button>
          <button onClick={clearFilters} disabled={!hasFilterChanges()} className={`col-span-1 h-10 px-4 py-2 rounded-md transition-colors flex items-center justify-center ${hasFilterChanges() ? "bg-gray-200 text-gray-700 hover:bg-gray-300" : "bg-gray-100 text-gray-400 cursor-not-allowed"}`}>
            초기화
          </button>
        </div>
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

                    {/* Expert/User Info */}
                    <div className="flex items-center gap-3 mb-3">
                      {viewType === "expert" ? (
                        <>
                          <span className="text-sm font-medium">{matching.userName}</span>
                          <span className="text-sm text-gray-500">{matching.userPhone}</span>
                        </>
                      ) : (
                        <>
                          <span className="text-sm font-medium">{matching.expertName}</span>
                          <span className="text-sm text-gray-500">{matching.expertPhone}</span>
                        </>
                      )}
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
                      <div className="flex gap-2">
                        {/* 리뷰 작성 버튼 - 전문가로 보기 상태에서는 숨김 */}
                        {viewType !== "expert" && matching.matchingStatus === "COMPLETED" && (
                          <button
                            className="px-4 py-1 bg-white text-gray-700 border border-gray-300 rounded hover:bg-green-50 text-sm"
                            // TODO: onClick={() => navigate(`/review/write/${matching.matchingId}`)}
                            type="button">
                            리뷰 작성
                          </button>
                        )}
                        <Link to={viewType === "expert" ? `/expert/matchings/${matching.matchingId}` : `/client/matchings/${matching.matchingId}`} className="px-4 py-1 bg-gray-100 text-gray-700 rounded hover:bg-blue-100 border border-gray-300 text-sm" type="button">
                          상세 보기
                        </Link>
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
