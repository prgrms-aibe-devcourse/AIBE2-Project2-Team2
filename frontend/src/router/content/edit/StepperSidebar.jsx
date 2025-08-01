import React from "react";
import { Check } from "lucide-react";

const StepperSidebar = ({ steps, currentStep, completedSteps, onStepClick }) => {
  return (
    <div className="w-80 bg-gradient-to-b from-slate-50 to-slate-100 border-r border-slate-200 p-8">
      <div className="mb-8">
        <h2 className="text-2xl font-bold text-slate-800 mb-2">서비스 수정</h2>
        <p className="text-slate-600">단계별로 정보를 입력해주세요</p>
      </div>

      <div className="space-y-4">
        {steps.map((label, idx) => {
          const isActive = currentStep === idx;
          const isCompleted = completedSteps.includes(idx);
          const isClickable = idx <= currentStep || isCompleted;

          return (
            <button
              key={label}
              className={`w-full text-left p-4 rounded-xl transition-all duration-200 ${
                isActive ? "bg-blue-500 text-white shadow-lg shadow-blue-200" : isCompleted ? "bg-green-50 text-green-700 border border-green-200 hover:bg-green-100" : isClickable ? "bg-white text-slate-600 border border-slate-200 hover:bg-slate-50" : "bg-slate-100 text-slate-400 cursor-not-allowed"
              }`}
              onClick={() => isClickable && onStepClick(idx)}
              disabled={!isClickable}>
              <div className="flex items-center gap-3">
                <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-semibold ${isActive ? "bg-white text-blue-500" : isCompleted ? "bg-green-500 text-white" : isClickable ? "bg-slate-200 text-slate-600" : "bg-slate-300 text-slate-500"}`}>
                  {isCompleted ? <Check className="w-4 h-4" /> : idx + 1}
                </div>
                <div>
                  <div className="font-semibold">{label}</div>
                  <div className={`text-xs mt-1 ${isActive ? "text-blue-100" : isCompleted ? "text-green-600" : "text-slate-500"}`}>{isCompleted ? "완료됨" : isActive ? "진행 중" : idx < currentStep ? "완료됨" : "대기 중"}</div>
                </div>
              </div>
            </button>
          );
        })}
      </div>    
    </div>
  );
};

export default StepperSidebar;