import { useParams, useNavigate, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import axios from "axios";

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
                const res = await axios.get("/api/categories/tree");
                setCategoryTree(res.data);
            } catch (err) {
                console.error("카테고리 트리 불러오기 실패:", err);
            }
        };
        fetchCategories();
    }, []);

    useEffect(() => {
        if (categoryTree.length > 0) {
            const main = categoryTree.find(c => c.id === categoryId);
            setMainCategoryName(main?.name || "");

            const sub = main?.children?.find(child => child.id === subCategoryId);
            setSubCategoryName(sub?.name || "");
        }
    }, [categoryTree, categoryId, subCategoryId]);

    useEffect(() => {
        const fetchContents = async () => {
            setLoading(true);
            try {
                const targetId = subCategoryId || categoryId;
                const res = await axios.get(`/api/public/content/category/${targetId}`, {
                    params: {
                        page: currentPage,
                        size: 12
                    }
                });
                setServices(Array.isArray(res.data.content) ? res.data.content : []);
                setTotalPages(res.data.totalPages);
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

    return (
        <div className="flex max-w-7xl mx-auto px-6 pt-24 min-h-screen gap-10 items-start">
            <aside className="w-[240px] hidden lg:block">
                <h3 className="text-lg font-semibold mb-4">카테고리</h3>
                <ul className="space-y-4">
                    {categoryTree.map((mainCategory) => (
                        <li key={mainCategory.id}>
                            <div
                                onClick={() => navigate(`/category/${mainCategory.id}`)}
                                className={`cursor-pointer px-2 py-1 font-semibold hover:bg-gray-100 rounded-md ${
                                    mainCategory.id === categoryId ? "bg-gray-200 text-teal-700" : ""
                                }`}
                            >
                                {mainCategory.name}
                            </div>
                            <ul className="ml-3 mt-1 space-y-1 text-sm">
                                {mainCategory.children?.length > 0 ? (
                                    mainCategory.children.map((sub) => (
                                        <li
                                            key={sub.id}
                                            onClick={() => navigate(`/category/${mainCategory.id}?sub=${sub.id}`)}
                                            className={`cursor-pointer px-2 py-1 rounded-md hover:text-teal-600 transition ${
                                                sub.id === subCategoryId ? "text-black font-medium" : "text-gray-500"
                                            }`}
                                        >
                                            {sub.name}
                                        </li>
                                    ))
                                ) : (
                                    <li className="text-gray-400 text-sm pl-2">하위 카테고리 없음</li>
                                )}
                            </ul>
                        </li>
                    ))}
                </ul>
            </aside>

            {/* Main */}
            <main className="flex-1 flex flex-col justify-start min-h-[600px]">
                <h2 className="text-2xl sm:text-3xl font-bold mb-6 tracking-tight">
                    {mainCategoryName}
                    {subCategoryName ? ` > ${subCategoryName}` : ""} 관련 서비스
                </h2>

                <div className="flex-1">
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-6 min-h-[320px]">
                        {loading ? (
                            <div className="col-span-full flex justify-center items-center text-gray-500 h-[160px]">
                                로딩 중...
                            </div>
                        ) : services.length === 0 ? (
                            <>
                                {[...Array(4)].map((_, idx) => (
                                    <div key={idx} className="invisible">-</div>
                                ))}
                                <div className="col-span-full flex flex-col items-center justify-center text-gray-400 h-[200px]">
                                    <span className="text-4xl mb-2">😮</span>
                                    <p className="text-sm">등록된 서비스가 없습니다.</p>
                                </div>
                            </>
                        ) : (
                            services.map((service) => (
                                <div
                                    key={service.contentId}
                                    className="group bg-white border border-gray-200 rounded-2xl shadow-sm hover:shadow-xl hover:border-teal-500 transition-all duration-300 pt-5 px-5 pb-4 cursor-pointer h-auto"
                                >


                                    <div className="overflow-hidden rounded-xl h-40 bg-gray-100">
                                        <img
                                            src={service.contentUrl || "/default-image.jpg"}
                                            alt={service.title}
                                            className="w-full h-full object-cover transform group-hover:scale-105 transition-transform duration-300"
                                        />
                                    </div>
                                    <h3 className="text-lg font-semibold mt-4 text-gray-900 group-hover:text-teal-700 transition-colors">
                                        {service.title}
                                    </h3>
                                    <p className="text-sm text-gray-600 mt-2 mb-1 line-clamp-2">{service.description}</p>
                                    <p className="font-bold text-teal-600 text-right pb-2">
                                        {service.budget?.toLocaleString() ?? 0}원~
                                    </p>
                                </div>
                            ))
                        )}
                    </div>
                </div>

                {/* Pagination */}
                {services.length > 0 && (
                    <div className="mt-8 flex justify-center items-center gap-2">
                        <button
                            onClick={() => currentPage > 0 && handlePageChange(currentPage - 1)}
                            disabled={currentPage === 0}
                            className="px-3 py-1 text-sm border rounded-md disabled:text-gray-300 disabled:border-gray-300"
                        >
                            &lt;
                        </button>
                        {Array.from({ length: totalPages }, (_, i) => (
                            <button
                                key={i}
                                onClick={() => handlePageChange(i)}
                                className={`px-3 py-1 text-sm border rounded-md ${
                                    currentPage === i
                                        ? "bg-teal-500 text-white border-transparent"
                                        : "bg-white text-gray-700 border-gray-300 hover:bg-gray-100"
                                }`}
                            >
                                {i + 1}
                            </button>
                        ))}
                        <button
                            onClick={() => currentPage < totalPages - 1 && handlePageChange(currentPage + 1)}
                            disabled={currentPage === totalPages - 1}
                            className="px-3 py-1 text-sm border rounded-md disabled:text-gray-300 disabled:border-gray-300"
                        >
                            &gt;
                        </button>
                    </div>
                )}
            </main>
        </div>
    );
};

export default CategoryPage;