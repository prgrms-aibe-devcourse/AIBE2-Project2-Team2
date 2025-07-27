import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
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

const steps = ["기본정보", "가격설정", "서비스 설명", "이미지"];

const ContentEditStepperPage = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [step, setStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [completedSteps, setCompletedSteps] = useState([]);

  const formProps = useContentForm();
  const categoryProps = useCategories((categoryId) => {
    formProps.handleChange("categoryId", categoryId);
  });

  // 기존 콘텐츠 데이터 불러오기
  useEffect(() => {
    if (id) {
      axiosInstance.get(`/api/content/${id}`).then((res) => {
        const data = res.data;
        formProps.setForm({
          title: data.title || "",
          categoryId: data.categoryId || "",
          budget: data.budget || "",
          questions: data.questions && data.questions.length > 0
            ? data.questions.map((q) => ({
                questionText: q.questionText,
                multipleChoice: q.isMultipleChoice,
                options: q.options.map((opt) => ({
                  optionText: opt.optionText,
                  additionalPrice: opt.additionalPrice,
                })),
              }))
            : [
                {
                  questionText: "",
                  multipleChoice: false,
                  options: [{ optionText: "", additionalPrice: 0 }],
                },
              ],
          description: data.description || "",
          images: [],
          thumbnail: null,
          existingThumbnail: data.contentUrl || null,
          existingImages: data.imageUrls || [],
        });
      });
    }
  }, [id, formProps.setForm]);

  // 카테고리 트리가 로드된 후 기존 카테고리 ID 설정
  useEffect(() => {
    if (formProps.form.categoryId && categoryProps.category1Options.length > 0) {
      categoryProps.setInitialCategory(formProps.form.categoryId);
    }
  }, [formProps.form.categoryId, categoryProps.category1Options.length, categoryProps.setInitialCategory]);

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
    if (!formProps.form.title || !formProps.form.categoryId || !formProps.form.budget || !formProps.form.description) {
      alert("모든 필수 정보를 입력해주세요.");
      return;
    }

    setLoading(true);
    try {
      await axiosInstance.put(`/api/content/${id}`, {
        title: formProps.form.title,
        description: formProps.form.description,
        budget: Number(formProps.form.budget),
        categoryId: Number(formProps.form.categoryId),
        questions: formProps.form.questions,
      });

      if (formProps.form.images.length > 0 || formProps.form.thumbnail) {
        const formData = new FormData();
        
        // 기존 이미지 ID 목록 (빈 배열로 전송 - 모든 기존 이미지 삭제)
        formData.append("remainingImageIds", "");
        
        formProps.form.images.forEach((img) => formData.append("images", img));
        if (formProps.form.thumbnail) formData.append("thumbnail", formProps.form.thumbnail);

        await axiosInstance.put(`/api/content/${id}/images`, formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });
      }

      alert("서비스가 성공적으로 수정되었습니다!");
      navigate(`/content/${id}`);
    } catch (err) {
      alert("수정 중 오류가 발생했습니다: " + (err.response?.data?.message || err.message));
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
        return <ImageStep form={formProps.form} onThumbnailChange={formProps.handleThumbnailChange} onImagesChange={formProps.handleImagesChange} existingThumbnail={formProps.form.existingThumbnail} existingImages={formProps.form.existingImages} />;
      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-7xl mx-auto">
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
                          {step === 0 && "서비스의 기본 정보를 수정해주세요"}
                          {step === 1 && "가격과 추가 옵션을 수정해주세요"}
                          {step === 2 && "고객에게 보여줄 상세 설명을 수정해주세요"}
                          {step === 3 && "서비스를 대표할 이미지들을 수정해주세요"}
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
                            수정 중...
                          </>
                        ) : (
                          <>
                            <Send className="w-4 h-4" />
                            서비스 수정
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

export default ContentEditStepperPage;
