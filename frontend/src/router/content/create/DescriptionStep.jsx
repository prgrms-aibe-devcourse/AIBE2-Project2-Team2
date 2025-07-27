import React from "react";
import { CheckCircle, Circle, FileText } from "lucide-react";

const DescriptionStep = ({ form, onDescriptionChange }) => {
  const isValid = form.description.trim().length > 0;

  return (
    <div className="space-y-8">
      <div className="space-y-4">
        <div className="flex items-center gap-3">
          {isValid ? <CheckCircle className="w-6 h-6 text-green-500" /> : <Circle className="w-6 h-6 text-gray-300" />}
          <h3 className="text-lg font-semibold text-gray-900">서비스 상세 설명</h3>
        </div>
        <div className="ml-9">
          <div className="relative">
            <FileText className="absolute left-4 top-4 w-5 h-5 text-gray-400" />
            <textarea
              className="w-full border-2 border-gray-200 rounded-xl pl-12 pr-5 py-4 min-h-[200px] text-gray-900 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-50 transition-all duration-200 resize-none"
              placeholder="어떤 서비스를 제공하시나요? 고객이 알아야 할 모든 정보를 자세히 설명해주세요.

• 서비스 내용과 범위
• 작업 과정과 방법
• 제공되는 결과물
• 작업 기간
• 기타 주의사항"
              value={form.description}
              onChange={(e) => onDescriptionChange(e.target.value)}
              maxLength={1000}
            />
          </div>
          <div className="flex justify-between items-center mt-2">
            <p className="text-sm text-gray-500">상세하고 명확한 설명이 더 많은 주문으로 이어집니다</p>
            <span className="text-xs text-gray-400 font-medium">{form.description.length}/1000</span>
          </div>
        </div>
      </div>

      {/* Tips */}
      <div className="ml-9 bg-gradient-to-r from-green-50 to-emerald-50 border border-green-200 rounded-xl p-5">
        <div className="flex items-start gap-3">
          <div className="w-2 h-2 bg-green-500 rounded-full mt-2 flex-shrink-0"></div>
          <div>
            <p className="text-green-800 font-medium mb-2">좋은 서비스 설명 작성 팁</p>
            <ul className="text-green-700 text-sm space-y-1">
              <li>• 구체적이고 명확한 표현 사용</li>
              <li>• 고객이 받을 결과물 명시</li>
              <li>• 작업 과정과 소요 시간 안내</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DescriptionStep;
