import React from "react";
import { CheckCircle, Circle, Plus, Minus, DollarSign } from "lucide-react";

const PricingStep = ({ form, onBudgetChange, onQuestionChange, onOptionChange, onAddQuestion, onRemoveQuestion, onAddOption, onRemoveOption }) => {
  const isValid = form.budget && Number(form.budget) > 0;

  return (
    <div className="space-y-8">
      {/* Budget Section */}
      <div className="space-y-4">
        <div className="flex items-center gap-3">
          {isValid ? <CheckCircle className="w-6 h-6 text-green-500" /> : <Circle className="w-6 h-6 text-gray-300" />}
          <h3 className="text-lg font-semibold text-gray-900">기본 가격 설정</h3>
        </div>
        <div className="ml-9">
          <div className="relative">
            <DollarSign className="absolute left-4 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              type="number"
              className="w-full border-2 border-gray-200 rounded-xl pl-12 pr-5 py-4 text-gray-900 placeholder-gray-400 focus:outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-50 transition-all duration-200"
              placeholder="기본 서비스 가격을 입력해주세요"
              value={form.budget}
              onChange={(e) => onBudgetChange(e.target.value)}
              min={0}
            />
          </div>
          <p className="text-sm text-gray-500 mt-2">고객이 지불할 기본 서비스 금액을 설정해주세요</p>
        </div>
      </div>

      {/* Options Section */}
      <div className="space-y-4">
        <div className="flex items-center gap-3">
          <Circle className="w-6 h-6 text-gray-300" />
          <h3 className="text-lg font-semibold text-gray-900">추가 옵션 (선택사항)</h3>
        </div>
        <div className="ml-9 space-y-4">
          {form.questions.map((q, qIdx) => (
            <div key={qIdx} className="bg-gray-50 border-2 border-gray-100 rounded-xl p-6 space-y-4">
              <div className="flex items-center gap-4">
                <input
                  type="text"
                  className="flex-1 border-2 border-gray-200 rounded-lg px-4 py-3 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-50 transition-all duration-200"
                  placeholder="옵션명 (예: 추가 수정, 급하게 처리 등)"
                  value={q.questionText}
                  onChange={(e) => onQuestionChange(qIdx, "questionText", e.target.value)}
                />
                <label className="flex items-center gap-2 text-sm text-gray-600 whitespace-nowrap">
                  <input type="checkbox" className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500" checked={q.multipleChoice} onChange={(e) => onQuestionChange(qIdx, "multipleChoice", e.target.checked)} />
                  중복선택 가능
                </label>
                {form.questions.length > 1 && (
                  <button type="button" className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-colors" onClick={() => onRemoveQuestion(qIdx)}>
                    <Minus className="w-4 h-4" />
                  </button>
                )}
              </div>

              <div className="space-y-3">
                {q.options.map((opt, oIdx) => (
                  <div key={oIdx} className="flex items-center gap-3">
                    <input
                      type="text"
                      className="flex-1 border border-gray-200 rounded-lg px-4 py-2 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-50 transition-all duration-200"
                      placeholder="옵션 세부사항"
                      value={opt.optionText}
                      onChange={(e) => onOptionChange(qIdx, oIdx, "optionText", e.target.value)}
                    />
                    <div className="relative">
                      <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-sm">₩</span>
                      <input
                        type="number"
                        className="w-32 border border-gray-200 rounded-lg pl-8 pr-3 py-2 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-50 transition-all duration-200"
                        placeholder="0"
                        value={opt.additionalPrice}
                        onChange={(e) => onOptionChange(qIdx, oIdx, "additionalPrice", Number(e.target.value))}
                        min={0}
                      />
                    </div>
                    {q.options.length > 1 && (
                      <button type="button" className="p-2 text-red-400 hover:bg-red-50 rounded-lg transition-colors" onClick={() => onRemoveOption(qIdx, oIdx)}>
                        <Minus className="w-4 h-4" />
                      </button>
                    )}
                  </div>
                ))}
                <button type="button" className="flex items-center gap-2 text-blue-600 hover:bg-blue-50 px-3 py-2 rounded-lg transition-colors text-sm font-medium" onClick={() => onAddOption(qIdx)}>
                  <Plus className="w-4 h-4" />
                  옵션 추가
                </button>
              </div>
            </div>
          ))}

          <button type="button" className="flex items-center gap-2 text-blue-600 hover:bg-blue-50 px-4 py-3 rounded-xl transition-colors font-medium border-2 border-dashed border-blue-200 w-full justify-center" onClick={onAddQuestion}>
            <Plus className="w-5 h-5" />새 옵션 그룹 추가
          </button>
        </div>
      </div>
    </div>
  );
};

export default PricingStep;
