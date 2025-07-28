import React from "react";
import { CheckCircle, Circle } from "lucide-react";

const BasicInfoStep = ({ form, onTitleChange, categoryProps }) => {
  const { selectedCategory1, selectedCategory2, selectedCategory3, category1Options, category2Options, category3Options, handleCategory1Change, handleCategory2Change, handleCategory3Change } = categoryProps;

  return (
    <div className="space-y-8">
      {/* Title Section */}
      <div className="space-y-4">
        <div className="flex items-center gap-3">
          {form.title.trim().length > 0 ? <CheckCircle className="w-6 h-6 text-green-500" /> : <Circle className="w-6 h-6 text-gray-300" />}
          <h3 className="text-lg font-semibold text-gray-900">서비스 제목</h3>
        </div>
        <div className="ml-9">
          <input
            type="text"
            className="w-full border-2 border-gray-200 rounded-xl px-5 py-4 text-gray-900 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-50 transition-all duration-200"
            placeholder="어떤 서비스를 제공하시나요?"
            value={form.title}
            onChange={(e) => onTitleChange(e.target.value)}
            maxLength={30}
          />
          <div className="flex justify-between items-center mt-2">
            <p className="text-sm text-gray-500">명확하고 매력적인 제목을 작성해주세요</p>
            <span className="text-xs text-gray-400 font-medium">{form.title.length}/30</span>
          </div>
        </div>
      </div>

      {/* Category Section */}
      <div className="space-y-4">
        <div className="flex items-center gap-3">
          {form.categoryId ? <CheckCircle className="w-6 h-6 text-green-500" /> : <Circle className="w-6 h-6 text-gray-300" />}
          <h3 className="text-lg font-semibold text-gray-900">카테고리 선택</h3>
        </div>
        <div className="ml-9 space-y-4">
          <select className="w-full border-2 border-gray-200 rounded-xl px-5 py-4 text-gray-900 focus:outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-50 transition-all duration-200 bg-white" value={selectedCategory1} onChange={(e) => handleCategory1Change(e.target.value)}>
            <option value="">1차 카테고리를 선택해주세요</option>
            {category1Options.map((cat) => (
              <option key={cat.id} value={cat.id}>
                {cat.name}
              </option>
            ))}
          </select>

          {selectedCategory1 && (
            <select className="w-full border-2 border-gray-200 rounded-xl px-5 py-4 text-gray-900 focus:outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-50 transition-all duration-200 bg-white" value={selectedCategory2} onChange={(e) => handleCategory2Change(e.target.value)}>
              <option value="">2차 카테고리를 선택해주세요</option>
              {category2Options.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.name}
                </option>
              ))}
            </select>
          )}

          {selectedCategory2 && category3Options.length > 0 && (
            <select className="w-full border-2 border-gray-200 rounded-xl px-5 py-4 text-gray-900 focus:outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-50 transition-all duration-200 bg-white" value={selectedCategory3} onChange={(e) => handleCategory3Change(e.target.value)}>
              <option value="">3차 카테고리를 선택해주세요</option>
              {category3Options.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.name}
                </option>
              ))}
            </select>
          )}
        </div>
      </div>

      {/* Info Box */}
      <div className="ml-9 bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-xl p-5">
        <div className="flex items-start gap-3">
          <div className="w-2 h-2 bg-blue-500 rounded-full mt-2 flex-shrink-0"></div>
          <div>
            <p className="text-blue-800 font-medium mb-1">카테고리 선택 팁</p>
            <p className="text-blue-700 text-sm leading-relaxed">정확한 카테고리를 선택하면 더 많은 고객이 서비스를 찾을 수 있어요</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BasicInfoStep;
