import { useParams, useNavigate, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import axiosInstance from "../lib/axios.js";

// --- MOCK DATA ---
// const mockCategoryContentResponse = {
//   content: [
//     {
//       contentId: 1,
//       memberId: 3,
//       title: "로고 디자인2342342342342342343223423423423432",
//       description: "브랜드 맞춤형 로고 디자인 서비스",
//       budget: 150000,
//       status: "ACTIVE",
//       regTime: "2024-07-25T10:30:00",
//       updateTime: "2024-07-25T10:30:00",
//       createdBy: "expert@example.com",
//       modifiedBy: "admin@example.com",
//       categoryId: 5,
//       categoryName: "로고 디자인",
//       contentUrl: "/images/study.jpg",
//       imageUrls: ["/images/study.jpg", "/images/study.jpg"],
//       expertName: "김전문",
//       rating: 4.7,
//       reviewCount: 23,
//     },
//     // 추가 mock 데이터 필요시 여기에 배열로 추가 가능
//   ],
//   pageable: {
//     pageNumber: 0,
//     pageSize: 12,
//   },
//   totalPages: 3,
//   totalElements: 25,
//   last: false,
//   first: true,
//   size: 12,
//   numberOfElements: 12,
// };

const CategoryPage = () => {
  const { categoryName } = useParams();
  const categoryId = Number(categoryName);
  const { search } = useLocation();
  const navigate = useNavigate();

  const [categoryTree, setCategoryTree] = useState([]);
  const [services, setServices] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);

  const subCategory = new URLSearchParams(search).get("sub");
  const subCategoryId = subCategory ? Number(subCategory) : null;

  const [mainCategoryName, setMainCategoryName] = useState("");
  const [subCategoryName, setSubCategoryName] = useState("");

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const res = await axiosInstance.get("/api/categories/tree");
        setCategoryTree(res.data);
      } catch (err) {
        console.error("카테고리 트리 불러오기 실패:", err);
      }
    };
    fetchCategories();
  }, []);

  useEffect(() => {
    if (categoryTree.length > 0) {
      const main = categoryTree.find((c) => c.id === categoryId);
      setMainCategoryName(main?.name || "");

      const sub = main?.children?.find((child) => child.id === subCategoryId);
      setSubCategoryName(sub?.name || "");
    }
  }, [categoryTree, categoryId, subCategoryId]);

  useEffect(() => {
    const fetchContents = async () => {
      setLoading(true);
      try {
        // MOCK: 실제 API 대신 mock 데이터 사용
        const targetId = subCategoryId || categoryId;
        const res = await axiosInstance.get(`/api/search/categories/${targetId}`, {
          params: {
            page: currentPage,
            size: 12,
          },
        });
        setServices(Array.isArray(res.data.content) ? res.data.content : []);
        setTotalPages(res.data.totalPages);
        // setServices(Array.isArray(mockCategoryContentResponse.content) ? mockCategoryContentResponse.content : []);
        // setTotalPages(mockCategoryContentResponse.totalPages);
      } catch (err) {
        console.error("컨텐츠 불러오기 실패:", err);
        setServices([]);
      } finally {
        setLoading(false);
      }
    };
    fetchContents();
  }, [categoryId, subCategoryId, currentPage]);

  const handlePageChange = (page) => {
    setCurrentPage(page);
  };

  // Track expanded main category for dropdown
  const [expandedCategoryId, setExpandedCategoryId] = useState(null);

  return (
    <div className="flex max-w-7xl mx-auto px-6 p-24 gap-10 items-start">
      <aside className="w-[240px] hidden lg:block">
        <h3 className="text-xl font-bold mb-4">카테고리</h3>
        <ul className="space-y-4">
          {categoryTree.map((mainCategory) => (
            <li key={mainCategory.id}>
              <div
                onClick={() => {
                  // Toggle dropdown
                  setExpandedCategoryId(expandedCategoryId === mainCategory.id ? null : mainCategory.id);
                  // Navigate to main category
                  navigate(`/category/${mainCategory.id}`);
                }}
                className={`text-gray-600 cursor-pointer px-2 py-1 font-semibold hover:bg-gray-100 rounded-md flex items-center justify-between ${mainCategory.id === categoryId ? "bg-gray-200 text-teal-700" : ""}`}>
                <span>{mainCategory.name}</span>
                {mainCategory.children?.length > 0 && (
                  <span className={`ml-2 transition-transform duration-500 ${expandedCategoryId === mainCategory.id ? "rotate-x-180" : "rotate-0"}`}>
                    <i className="xi-angle-down-min xi-x"></i>
                  </span>
                )}
              </div>
              {/* Children dropdown */}
              <div
                style={{
                  maxHeight: expandedCategoryId === mainCategory.id ? "500px" : "0px",
                  opacity: expandedCategoryId === mainCategory.id ? 1 : 0,
                  transition: "max-height 0.3s cubic-bezier(0.4,0,0.2,1), opacity 0.2s",
                  overflow: "hidden",
                }}>
                {mainCategory.children?.length > 0 && (
                  <ul className="ml-3 mt-1 space-y-1 text-sm border-l border-gray-200 pl-2">
                    {mainCategory.children.map((sub) => (
                      <li key={sub.id} onClick={() => navigate(`/category/${mainCategory.id}?sub=${sub.id}`)} className={`cursor-pointer px-2 py-1 rounded-md hover:text-teal-600 transition ${sub.id === subCategoryId ? "text-black font-medium" : "text-gray-500"}`}>
                        {sub.name}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </li>
          ))}
        </ul>
      </aside>

      {/* Main */}
      <main className="flex-1 flex flex-col justify-start min-h-[1800px] w-[1000]">
        <h2 className="text-2xl sm:text-3xl font-bold mb-6 tracking-tight">
          {mainCategoryName}
          {subCategoryName ? ` > ${subCategoryName}` : ""} 관련 서비스
        </h2>

        <div className="flex-1">
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-6 min-h-[320px]">
            {loading ? (
              <div className="col-span-full flex justify-center items-center text-gray-500 h-[160px]">로딩 중...</div>
            ) : services.length === 0 ? (
              <>
                {[...Array(4)].map((_, idx) => (
                  <div key={idx} className="invisible">
                    -
                  </div>
                ))}
                <div className="col-span-full flex flex-col items-center justify-center text-gray-400 h-[200px]">
                  <span className="text-4xl mb-2">😮</span>
                  <p className="text-sm">등록된 서비스가 없습니다.</p>
                </div>
              </>
            ) : (
              services.map((service) => (
                <div key={service.contentId} className="group bg-white rounded-2xl transition-all duration-300 p-4 cursor-pointer h-auto flex flex-col gap-1">
                  {/* 썸네일 */}
                  <div className="relative overflow-hidden rounded-xl aspect-[4/3] bg-gray-100 mb-2">
                    <img src={service.contentThumbnailUrl || "/default-image.jpg"} alt={service.title} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" />
                    <span className="absolute bottom-2 left-2 bg-white bg-opacity-80 text-xs text-gray-700 px-2 py-1 rounded-md">{service.categoryName}</span>
                  </div>
                  {/* 제목 */}
                  <h3 className="text-base font-bold text-gray-900 leading-tight text-ellipsis overflow-hidden">{service.title}</h3>
                  {/* 별점/리뷰 */}
                  <div className="flex items-center gap-1 text-yellow-500 text-sm">
                    <i className="xi-star">{service.rating?.toFixed(1) ?? "0.0"}</i>
                    <span className="text-gray-500">({service.reviewCount ?? 0})</span>
                  </div>
                  {/* 가격 */}
                  <div className="font-bold text-teal-600 text-base">{service.budget?.toLocaleString() ?? 0}원~</div>
                  {/* 전문가명/회사명 */}
                  <div className="text-xs text-gray-500">
                    {service.expertName} {/* 회사명 등 추가 정보 */}
                  </div>
                  {/* 부가정보(예: 세금계산서 등) */}
                  <div className="text-xs text-blue-600">세금계산서</div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Pagination */}
        {services.length > 0 && (
          <div className="mt-8 flex justify-center items-center gap-2">
            <button onClick={() => currentPage > 0 && handlePageChange(currentPage - 1)} disabled={currentPage === 0} className="px-3 py-1 text-sm border rounded-md disabled:text-gray-300 disabled:border-gray-300">
              &lt;
            </button>
            {Array.from({ length: totalPages }, (_, i) => (
              <button key={i} onClick={() => handlePageChange(i)} className={`px-3 py-1 text-sm border rounded-md ${currentPage === i ? "bg-teal-500 text-white border-transparent" : "bg-white text-gray-700 border-gray-300 hover:bg-gray-100"}`}>
                {i + 1}
              </button>
            ))}
            <button onClick={() => currentPage < totalPages - 1 && handlePageChange(currentPage + 1)} disabled={currentPage === totalPages - 1} className="px-3 py-1 text-sm border rounded-md disabled:text-gray-300 disabled:border-gray-300">
              &gt;
            </button>
          </div>
        )}
      </main>
    </div>
  );
};

export default CategoryPage;
