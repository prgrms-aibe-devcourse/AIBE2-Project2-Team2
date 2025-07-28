import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft, ArrowRight, Send, Loader2 } from "lucide-react";
import { useContentForm } from "./useContentForm";
import { useCategories } from "./useCategories";
import StepperSidebar from "./StepperSidebar";
import BasicInfoStep from "./BasicInfoStep";
import PricingStep from "./PricingStep";
import DescriptionStep from "./DescriptionStep";
import ImageStep from "./ImageStep";
import { validateStep } from "./validation";
import axiosInstance from "../../../lib/axios";
import toast from "react-hot-toast";
import { useUserInfoStore } from "../../../store/userInfo";

const steps = ["기본정보", "가격설정", "서비스 설명", "이미지"];

const ContentCreateStepperPage = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [completedSteps, setCompletedSteps] = useState([]);
  const { userInfo } = useUserInfoStore();

  // 로그인 및 전문가 권한 체크 - 컴포넌트 마운트 시
  useEffect(() => {
    if (!userInfo) {
      toast.error("로그인이 필요합니다.");
      navigate("/auth/login");
      return;
    }
    
    // 전문가가 아닌 경우 접근 제한
    if (userInfo.role !== "EXPERT") {
      toast.error("전문가만 서비스를 등록할 수 있습니다.");
      navigate("/");
      return;
    }
  }, [userInfo, navigate]);

  const formProps = useContentForm();
  const categoryProps = useCategories((categoryId) => {
    formProps.handleChange("categoryId", categoryId);
  });

  const isStepValid = () => validateStep(step, formProps.form, categoryProps);

  // 완료된 단계 추적
  useEffect(() => {
    const newCompletedSteps = [];
    for (let i = 0; i < steps.length; i++) {
      if (validateStep(i, formProps.form, categoryProps)) {
        newCompletedSteps.push(i);
      }
    }
    setCompletedSteps(newCompletedSteps);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(formProps.form), categoryProps.selectedCategory1, categoryProps.selectedCategory2, categoryProps.selectedCategory3]);

  const nextStep = () => {
    if (isStepValid()) {
      setStep((s) => Math.min(s + 1, steps.length - 1));
    }
  };

  const prevStep = () => {
    setStep((s) => Math.max(s - 1, 0));
  };

  const goToStep = (idx) => {
    // 해당 단계가 완료되었거나 현재 단계보다 작거나 같으면 이동 가능
    if (completedSteps.includes(idx) || idx <= step) {
      setStep(idx);
    }
  };

  const handleSubmit = async () => {
    // 로그인 체크
    if (!userInfo) {
      toast.error("로그인이 필요합니다.");
      navigate("/auth/login");
      return;
    }

    // 전문가 권한 체크
    if (userInfo.role !== "EXPERT") {
      toast.error("전문가만 서비스를 등록할 수 있습니다.");
      navigate("/");
      return;
    }

    // 필수 정보 검증
    if (!formProps.form.title || !formProps.form.categoryId || !formProps.form.budget || !formProps.form.description) {
      toast.error("모든 필수 정보를 입력해주세요.");
      return;
    }

    // 이미지 검증
    if (!formProps.form.thumbnail || !formProps.form.images || formProps.form.images.length === 0) {
      toast.error("썸네일과 서비스 이미지를 모두 등록해주세요.");
      return;
    }

    setLoading(true);
    try {
      // 콘텐츠 생성
      const contentRes = await axiosInstance.post("/api/content", {
        title: formProps.form.title,
        description: formProps.form.description,
        budget: Number(formProps.form.budget),
        categoryId: Number(formProps.form.categoryId),
        questions: formProps.form.questions,
      });

      const contentId = contentRes.data.contentId;

      // 이미지 업로드
      if (formProps.form.images.length > 0 || formProps.form.thumbnail) {
        const formData = new FormData();
        formProps.form.images.forEach((img) => formData.append("images", img));
        if (formProps.form.thumbnail) formData.append("thumbnail", formProps.form.thumbnail);

        await axiosInstance.post(`/api/content/${contentId}/images/batch`, formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });
      }

      toast.success("서비스가 성공적으로 등록되었습니다!");
      navigate(`/content/${contentId}`);
    } catch (err) {
      console.error("❌ 서비스 등록 실패:", err);
      if (err.response?.status === 401) {
        toast.error("로그인이 필요합니다.");
      } else if (err.response?.status === 400) {
        toast.error("입력 정보를 다시 확인해주세요.");
      } else {
        toast.error("서비스 등록 중 오류가 발생했습니다. 다시 시도해주세요.");
      }
    } finally {
      setLoading(false);
    }
  };

  const renderStep = () => {
    switch (step) {
      case 0:
        return <BasicInfoStep form={formProps.form} onTitleChange={(value) => formProps.handleChange("title", value)} categoryProps={categoryProps} />;
      case 1:
        return (
          <PricingStep
            form={formProps.form}
            onBudgetChange={(value) => formProps.handleChange("budget", value)}
            onQuestionChange={formProps.handleQuestionChange}
            onOptionChange={formProps.handleOptionChange}
            onAddQuestion={formProps.addQuestion}
            onRemoveQuestion={formProps.removeQuestion}
            onAddOption={formProps.addOption}
            onRemoveOption={formProps.removeOption}
          />
        );
      case 2:
        return <DescriptionStep form={formProps.form} onDescriptionChange={(value) => formProps.handleChange("description", value)} />;
      case 3:
        return <ImageStep form={formProps.form} onThumbnailChange={formProps.handleThumbnailChange} onImagesChange={formProps.handleImagesChange} />;
      default:
        return null;
    }
  };

  return (
    <div className="">
      <div className="px-4 py-8">
        <div className="w-5xl mx-auto">
          <div className="bg-white rounded-2xl shadow-xl overflow-hidden border border-slate-200">
            <div className="flex">
              <StepperSidebar steps={steps} currentStep={step} completedSteps={completedSteps} onStepClick={goToStep} />

              <main className="flex-1 p-8 lg:p-12">
                <div className="max-w-3xl mx-auto">
                  {/* Header */}
                  <div className="mb-12">
                    <div className="flex items-center gap-4 mb-6">
                      <div className="w-12 h-12 bg-gradient-to-r from-blue-500 to-indigo-600 rounded-xl flex items-center justify-center text-white font-bold text-lg">{step + 1}</div>
                      <div>
                        <h1 className="text-3xl font-bold text-slate-800">{steps[step]}</h1>
                        <p className="text-slate-600 mt-1">
                          {step === 0 && "서비스의 기본 정보를 입력해주세요"}
                          {step === 1 && "가격과 추가 옵션을 설정해주세요"}
                          {step === 2 && "고객에게 보여줄 상세 설명을 작성해주세요"}
                          {step === 3 && "서비스를 대표할 이미지들을 업로드해주세요"}
                        </p>
                      </div>
                    </div>

                    {/* Progress Bar */}
                    <div className="w-full bg-slate-200 rounded-full h-2">
                      <div className="bg-gradient-to-r from-blue-500 to-indigo-600 h-2 rounded-full transition-all duration-500 ease-out" style={{ width: `${((step + 1) / steps.length) * 100}%` }}></div>
                    </div>
                  </div>

                  {/* Step Content */}
                  <div className="mb-12">{renderStep()}</div>

                  {/* Navigation Buttons */}
                  <div className="flex items-center justify-between pt-8 border-t border-slate-200">
                    <button onClick={prevStep} disabled={step === 0} className={`flex items-center gap-2 px-6 py-3 rounded-xl font-semibold transition-all duration-200 ${step === 0 ? "text-slate-400 cursor-not-allowed" : "text-slate-600 hover:text-slate-800 hover:bg-slate-100"}`}>
                      <ArrowLeft className="w-4 h-4" />
                      이전
                    </button>

                    <div className="flex items-center gap-2">
                      {steps.map((_, idx) => (
                        <div key={idx} className={`w-2 h-2 rounded-full transition-all duration-200 ${idx === step ? "bg-blue-500 w-8" : completedSteps.includes(idx) ? "bg-green-500" : "bg-slate-300"}`} />
                      ))}
                    </div>

                    {step < steps.length - 1 ? (
                      <button
                        onClick={nextStep}
                        disabled={!isStepValid()}
                        className={`flex items-center gap-2 px-8 py-3 rounded-xl font-semibold transition-all duration-200 ${
                          isStepValid() ? "bg-gradient-to-r from-blue-500 to-indigo-600 text-white hover:from-blue-600 hover:to-indigo-700 shadow-lg hover:shadow-xl transform hover:-translate-y-0.5" : "bg-slate-200 text-slate-400 cursor-not-allowed"
                        }`}>
                        다음
                        <ArrowRight className="w-4 h-4" />
                      </button>
                    ) : (
                      <button
                        onClick={handleSubmit}
                        disabled={!isStepValid() || loading}
                        className={`flex items-center gap-2 px-8 py-3 rounded-xl font-semibold transition-all duration-200 ${
                          isStepValid() && !loading ? "bg-gradient-to-r from-green-500 to-emerald-600 text-white hover:from-green-600 hover:to-emerald-700 shadow-lg hover:shadow-xl transform hover:-translate-y-0.5" : "bg-slate-200 text-slate-400 cursor-not-allowed"
                        }`}>
                        {loading ? (
                          <>
                            <Loader2 className="w-4 h-4 animate-spin" />
                            등록 중...
                          </>
                        ) : (
                          <>
                            <Send className="w-4 h-4" />
                            서비스 등록
                          </>
                        )}
                      </button>
                    )}
                  </div>
                </div>
              </main>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContentCreateStepperPage;
