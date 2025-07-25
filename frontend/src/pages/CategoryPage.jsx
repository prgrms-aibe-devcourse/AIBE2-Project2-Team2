import { useParams, useNavigate, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import axios from "axios";

const CategoryPage = () => {
    const { categoryName } = useParams();
    const { search } = useLocation();
    const navigate = useNavigate();

    const [categoryTree, setCategoryTree] = useState({});
    const [services, setServices] = useState([]);
    const [loading, setLoading] = useState(true);

    const subCategory = new URLSearchParams(search).get("sub");

    // 1. 카테고리 트리 불러오기
    useEffect(() => {
        const fetchCategories = async () => {
            try {
                const res = await axios.get("/api/categories/tree");
                setCategoryTree(res.data); // 💡 응답이 { [대분류]: [소분류, ...] } 형태라고 가정
            } catch (err) {
                console.error("카테고리 트리 불러오기 실패:", err);
            }
        };
        fetchCategories();
    }, []);

    // 2. 콘텐츠 불러오기
    useEffect(() => {
        const fetchContents = async () => {
            setLoading(true);
            try {
                const res = await axios.get("/api/contents", {
                    params: {
                        category: categoryName,
                        sub: subCategory || undefined,
                    },
                });
                setServices(res.data);
            } catch (err) {
                console.error("컨텐츠 불러오기 실패:", err);
            } finally {
                setLoading(false);
            }
        };
        fetchContents();
    }, [categoryName, subCategory]);

    return (
        <div className="flex max-w-7xl mx-auto px-6 pt-24 min-h-screen gap-10 items-start">
            {/* Sidebar */}
            <aside className="w-[240px] hidden lg:block">
                <h3 className="text-lg font-semibold mb-4">카테고리</h3>
                <ul className="space-y-4">
                    {Object.entries(categoryTree).map(([main, subList]) => (
                        <li key={main}>
                            <div
                                onClick={() => navigate(`/category/${main}`)}
                                className={`cursor-pointer px-2 py-1 font-semibold hover:bg-gray-100 rounded-md ${
                                    main === categoryName ? "bg-gray-200 text-teal-700" : ""
                                }`}
                            >
                                {main}
                            </div>
                            <ul className="ml-3 mt-1 space-y-1 text-sm">
                                {subList.map((sub) => (
                                    <li
                                        key={sub}
                                        onClick={() => navigate(`/category/${main}?sub=${sub}`)}
                                        className={`cursor-pointer px-2 py-1 rounded-md hover:text-teal-600 transition ${
                                            sub === subCategory ? "text-black font-medium" : "text-gray-500"
                                        }`}
                                    >
                                        {sub}
                                    </li>
                                ))}
                            </ul>
                        </li>
                    ))}
                </ul>
            </aside>

            {/* Main content */}
            <main className="flex-1">
                <h2 className="text-2xl sm:text-3xl font-bold mb-8">
                    {categoryName}
                    {subCategory ? ` > ${subCategory}` : ""} 관련 서비스
                </h2>

                {loading ? (
                    <p className="text-gray-500">로딩 중...</p>
                ) : services.length === 0 ? (
                    <p className="text-gray-500">등록된 서비스가 없습니다.</p>
                ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-6">
                        {services.map((service) => (
                            <div
                                key={service.id}
                                className="group bg-white border border-gray-200 rounded-xl shadow-sm hover:shadow-lg hover:border-teal-500 transition-all duration-300 p-4 cursor-pointer"
                            >
                                <div className="overflow-hidden rounded-md">
                                    <img
                                        src={service.imageUrl}
                                        alt={service.title}
                                        className="w-full h-40 object-cover transform group-hover:scale-105 group-hover:brightness-110 transition-transform duration-300"
                                    />
                                </div>
                                <h3 className="text-lg font-semibold mt-4 group-hover:text-teal-700 transition-colors duration-200">
                                    {service.title}
                                </h3>
                                <p className="text-sm text-gray-600 my-2">{service.description}</p>
                                <p className="font-bold text-teal-600">
                                    {service.price.toLocaleString()}원~
                                </p>
                            </div>
                        ))}
                    </div>
                )}
            </main>
        </div>
    );
};

export default CategoryPage;
