import React, { useState, useEffect } from "react";
import axiosInstance from "../../lib/axios";
import { useNavigate } from "react-router-dom";
import styles from "./css/StepperPageCommon.module.css";

const steps = [
  "기본정보",
  "가격설정",
  "서비스 설명",
  "이미지"
];

const ContentCreateStepperPage = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(0); // 0부터 시작
  const [categoryTree, setCategoryTree] = useState([]);
  const [form, setForm] = useState({
    title: "",
    categoryId: "",
    budget: "",
    questions: [
      {
        questionText: "",
        multipleChoice: false,
        options: [{ optionText: "", additionalPrice: 0 }],
      },
    ],
    description: "",
    images: [],
    thumbnail: null,
  });
  const [loading, setLoading] = useState(false);

  // 카테고리 트리 불러오기
  useEffect(() => {
    axiosInstance.get("/api/categories/tree").then((res) => {
      setCategoryTree(res.data);
    });
  }, []);

  // 카테고리 단계별 선택 상태
  const [selectedCategory1, setSelectedCategory1] = useState("");
  const [selectedCategory2, setSelectedCategory2] = useState("");
  const [selectedCategory3, setSelectedCategory3] = useState("");

  // 카테고리 단계별 옵션 추출
  const category1Options = categoryTree;
  const category2Options = selectedCategory1
    ? categoryTree.find(cat => String(cat.id) === String(selectedCategory1))?.children || []
    : [];
  const category3Options = selectedCategory2
    ? category2Options.find(cat => String(cat.id) === String(selectedCategory2))?.children || []
    : [];

  // 카테고리 선택 핸들러
  const handleCategory1Change = (e) => {
    setSelectedCategory1(e.target.value);
    setSelectedCategory2("");
    setSelectedCategory3("");
    // form.categoryId는 아직 미지정
    setForm(prev => ({ ...prev, categoryId: "" }));
  };
  const handleCategory2Change = (e) => {
    setSelectedCategory2(e.target.value);
    setSelectedCategory3("");
    setForm(prev => ({ ...prev, categoryId: "" }));
  };
  const handleCategory3Change = (e) => {
    setSelectedCategory3(e.target.value);
    setForm(prev => ({ ...prev, categoryId: e.target.value }));
  };
  // 2차까지 선택하고 3차가 없는 경우 2차 id를 categoryId로 사용
  useEffect(() => {
    if (selectedCategory2 && category3Options.length === 0) {
      setForm(prev => ({ ...prev, categoryId: selectedCategory2 }));
    }
  }, [selectedCategory2, category3Options.length]);
  // 1차만 선택하고 2차가 없는 경우 1차 id를 categoryId로 사용
  useEffect(() => {
    if (selectedCategory1 && category2Options.length === 0) {
      setForm(prev => ({ ...prev, categoryId: selectedCategory1 }));
    }
  }, [selectedCategory1, category2Options.length]);

  // 카테고리 트리 렌더링 (재귀)

  // 단계별 입력값 변경 핸들러
  const handleChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  // 질문/옵션 핸들러
  const handleQuestionChange = (idx, field, value) => {
    const updated = [...form.questions];
    updated[idx][field] = value;
    setForm((prev) => ({ ...prev, questions: updated }));
  };
  const handleOptionChange = (qIdx, oIdx, field, value) => {
    const updated = [...form.questions];
    updated[qIdx].options[oIdx][field] = value;
    setForm((prev) => ({ ...prev, questions: updated }));
  };
  const addQuestion = () => {
    setForm((prev) => ({
      ...prev,
      questions: [
        ...prev.questions,
        { questionText: "", multipleChoice: false, options: [{ optionText: "", additionalPrice: 0 }] },
      ],
    }));
  };
  const removeQuestion = (idx) => {
    if (form.questions.length === 1) return;
    setForm((prev) => ({
      ...prev,
      questions: prev.questions.filter((_, i) => i !== idx),
    }));
  };
  const addOption = (qIdx) => {
    const updated = [...form.questions];
    updated[qIdx].options.push({ optionText: "", additionalPrice: 0 });
    setForm((prev) => ({ ...prev, questions: updated }));
  };
  const removeOption = (qIdx, oIdx) => {
    const updated = [...form.questions];
    if (updated[qIdx].options.length === 1) return;
    updated[qIdx].options = updated[qIdx].options.filter((_, i) => i !== oIdx);
    setForm((prev) => ({ ...prev, questions: updated }));
  };

  // 이미지 핸들러
  const handleImagesChange = (e) => {
    const files = Array.from(e.target.files).slice(0, 5);
    setForm((prev) => ({ ...prev, images: files }));
  };
  const handleThumbnailChange = (e) => {
    setForm((prev) => ({ ...prev, thumbnail: e.target.files[0] }));
  };

  // 단계 이동
  const nextStep = () => setStep((s) => Math.min(s + 1, steps.length - 1));
  const prevStep = () => setStep((s) => Math.max(s - 1, 0));
  const goToStep = (idx) => setStep(idx);

  // 제출
  const handleSubmit = async () => {
    setLoading(true);
    try {
      // 1. content 등록
      const contentRes = await axiosInstance.post("/api/content", {
        title: form.title,
        description: form.description,
        budget: Number(form.budget),
        categoryId: Number(form.categoryId),
        questions: form.questions,
      });
      const contentId = contentRes.data.contentId;
      // 2. 이미지 업로드
      if (form.images.length > 0 || form.thumbnail) {
        const formData = new FormData();
        form.images.forEach((img) => formData.append("images", img));
        if (form.thumbnail) formData.append("thumbnail", form.thumbnail);
        await axiosInstance.post(`/api/content/${contentId}/images/batch`, formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });
      }
      alert("등록이 완료되었습니다.");
      navigate(`/content/${contentId}`);
    } catch (err) {
      alert("등록 실패: " + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  // 단계별 폼 렌더링
  const renderStep = () => {
    switch (step) {
      case 0:
        return (
          <div>
            <div className="mb-4">
              <label className="block font-semibold mb-1">제목</label>
              <input type="text" className="w-full border rounded px-3 py-2" value={form.title} onChange={e => handleChange("title", e.target.value)} required maxLength={30} />
              <div className="text-right text-xs text-gray-400">{form.title.length} / 30</div>
            </div>
            <div className="mb-4">
              <label className="block font-semibold mb-1">카테고리</label>
              <div className="flex flex-col gap-2">
                {/* 1차 카테고리 */}
                <select className="w-full border rounded px-3 py-2" value={selectedCategory1} onChange={handleCategory1Change} required>
                  <option value="">1차 카테고리 선택</option>
                  {category1Options.map(cat => (
                    <option key={cat.id} value={cat.id}>{cat.name}</option>
                  ))}
                </select>
                {/* 2차 카테고리: 항상 표시, 1차 선택 전에는 disabled */}
                <select className="w-full border rounded px-3 py-2" value={selectedCategory2} onChange={handleCategory2Change} required disabled={!selectedCategory1}>
                  <option value="">2차 카테고리 선택</option>
                  {selectedCategory1 && category2Options.map(cat => (
                    <option key={cat.id} value={cat.id}>{cat.name}</option>
                  ))}
                </select>
                {/* 3차 카테고리: 2차 선택 시에만 표시 */}
                {selectedCategory2 && category3Options.length > 0 && (
                  <select className="w-full border rounded px-3 py-2" value={selectedCategory3} onChange={handleCategory3Change} required>
                    <option value="">3차 카테고리 선택</option>
                    {category3Options.map(cat => (
                      <option key={cat.id} value={cat.id}>{cat.name}</option>
                    ))}
                  </select>
                )}
              </div>
            </div>
          </div>
        );
      case 1:
        return (
          <div>
            <div className="mb-4">
              <label className="block font-semibold mb-1">예산(원)</label>
              <input type="number" className="w-full border rounded px-3 py-2" value={form.budget} onChange={e => handleChange("budget", e.target.value)} required min={0} />
            </div>
            <div className="mb-4">
              <label className="block font-semibold mb-1">옵션 추가</label>
              {form.questions.map((q, qIdx) => (
                <div key={qIdx} className="border rounded p-3 mb-2 bg-gray-50">
                  <div className="flex gap-2 mb-2">
                    <input type="text" className="flex-1 border rounded px-2 py-1" placeholder="옵션" value={q.questionText} onChange={e => handleQuestionChange(qIdx, "questionText", e.target.value)} required />
                    <label className="flex items-center gap-1">
                      <input type="checkbox" checked={q.multipleChoice} onChange={e => handleQuestionChange(qIdx, "multipleChoice", e.target.checked)} /> 중복 선택 가능
                    </label>
                    <button type="button" className="text-red-500" onClick={() => removeQuestion(qIdx)} disabled={form.questions.length === 1}>삭제</button>
                  </div>
                  <div className="ml-4">
                    {q.options.map((opt, oIdx) => (
                      <div key={oIdx} className="flex gap-2 mb-1">
                        <input type="text" className="border rounded px-2 py-1" placeholder="옵션 세부 사항" value={opt.optionText} onChange={e => handleOptionChange(qIdx, oIdx, "optionText", e.target.value)} required />
                        <input type="number" className="border rounded px-2 py-1 w-24" placeholder="추가금액" value={opt.additionalPrice} onChange={e => handleOptionChange(qIdx, oIdx, "additionalPrice", Number(e.target.value))} min={0} required />
                        <button type="button" className="text-red-400" onClick={() => removeOption(qIdx, oIdx)} disabled={q.options.length === 1}>옵션 삭제</button>
                      </div>
                    ))}
                    <button type="button" className="text-blue-500 mt-1" onClick={() => addOption(qIdx)}>옵션 추가</button>
                  </div>
                </div>
              ))}
              <button type="button" className="text-blue-600 mt-2" onClick={addQuestion}>질문 추가</button>
            </div>
          </div>
        );
      case 2:
        return (
          <div>
            <div className="mb-4">
              <label className="block font-semibold mb-1">서비스 설명</label>
              <textarea className="w-full border rounded px-3 py-2 min-h-[120px]" value={form.description} onChange={e => handleChange("description", e.target.value)} required maxLength={1000} />
              <div className="text-right text-xs text-gray-400">{form.description.length} / 1000</div>
            </div>
          </div>
        );
      case 3:
        return (
          <div>
            {/* 메인 이미지 업로드 */}
            <div className="mb-4">
              <label className="block font-semibold mb-1">썸네일 이미지(필수)</label>
              <div
                className="relative w-[326px] h-[244px] border rounded flex flex-col items-center justify-center cursor-pointer bg-gray-50"
                onClick={() => document.getElementById('main-image-input').click()}
              >
                {form.thumbnail ? (
                  <img src={URL.createObjectURL(form.thumbnail)} alt="thumbnail" className="w-full h-full object-cover rounded" />
                ) : (
                  <>
                    <div className="flex flex-col items-center">
                      <svg width="48" height="48" fill="none" viewBox="0 0 24 24"><path d="M12 5v14m7-7H5" stroke="#bbb" strokeWidth="2" strokeLinecap="round" /></svg>
                      <div className="text-gray-400 text-sm mt-2">652 × 488px<br/>(4:3 비율)</div>
                    </div>
                  </>
                )}
                <input
                  id="main-image-input"
                  type="file"
                  accept="image/*"
                  className="hidden"
                  onChange={handleThumbnailChange}
                />
              </div>
            </div>
            {/* 상세 이미지 업로드 */}
            <div className="mb-6">
              <label className="block font-semibold mb-1">이미지 업로드 (최대 5장)</label>
              <div
                className="relative w-[326px] h-[244px] border rounded flex flex-col items-center justify-center cursor-pointer bg-gray-50"
                onClick={() => document.getElementById('detail-image-input').click()}
              >
                {form.images && form.images.length > 0 ? (
                  <div className="flex gap-2 flex-wrap">
                    {Array.from(form.images).map((img, idx) => (
                      <img key={idx} src={URL.createObjectURL(img)} alt="preview" className="w-16 h-16 object-cover rounded" />
                    ))}
                  </div>
                ) : (
                  <>
                    <div className="flex flex-col items-center">
                      <svg width="48" height="48" fill="none" viewBox="0 0 24 24"><path d="M12 5v14m7-7H5" stroke="#bbb" strokeWidth="2" strokeLinecap="round" /></svg>
                      <div className="text-gray-400 text-sm mt-2">여러 장 등록 가능</div>
                    </div>
                  </>
                )}
                <input
                  id="detail-image-input"
                  type="file"
                  accept="image/*"
                  multiple
                  className="hidden"
                  onChange={handleImagesChange}
                />
              </div>
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'flex-start',
      width: '100%',
      minHeight: '100vh',
      background: '#f9fafb',
      padding: '4rem 0'
    }}>
      <div style={{
        display: 'flex',
        background: '#fff',
        borderRadius: '1.5rem',
        boxShadow: '0 2px 8px rgba(0,0,0,0.04)',
        border: '1px solid #f3f4f6',
        overflow: 'hidden',
        minWidth: '800px',
        maxWidth: '1100px',
        width: '100%'
      }}>
        <aside
          style={{
            width: '256px',
            background: '#f9fafb',
            borderRight: '1px solid #e5e7eb',
            display: 'flex',
            flexDirection: 'column',
            
            minHeight: '600px',
            marginRight: 0
          }}
        >
          {steps.map((label, idx) => (
            <button
              key={label}
              className={
                step === idx
                  ? `${styles.stepperSidebarButton} ${styles.stepperSidebarButtonActive}`
                  : `${styles.stepperSidebarButton} ${styles.stepperSidebarButtonInactive}`
              }
              onClick={() => goToStep(idx)}
        
            >
              <span className={
                step === idx
                  ? `${styles.stepperSidebarStepCircle} ${styles.stepperSidebarStepCircleActive}`
                  : styles.stepperSidebarStepCircle
              }>{idx + 1}</span>
              {label}
            </button>
          ))}
          <div style={{ marginTop: '2rem', padding: '1rem', background: '#fff', borderRadius: '0.75rem', border: '1px solid #f3f4f6', boxShadow: '0 1px 4px rgba(0,0,0,0.03)' }}>
          <span style={{ fontSize: '0.75rem', color: '#6b7280' }}>서비스 등록 가이드</span>
            <span style={{ fontSize: '0.75rem', color: '#2563eb', fontWeight: 600, marginBottom: '0.5rem', display: 'block' }}>위 버튼을 눌러 서비스를 등록해주세요.</span>
            
          </div>
        </aside>
        {/* 우측 폼 */}
        <main style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          
          background: '#fff',
          minHeight: '600px',
          width: '100%'
        }}>
          <div style={{
            width: '100%',
            maxWidth: '40rem',
            background: '#fff',
            borderRadius: '1.5rem',
            boxShadow: 'none',
            padding: '2.5rem',
            border: 'none'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
              <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>{steps[step]}</h2>
              <button
                className={`${styles.stepperButton} ${styles.stepperButtonPrimary}`}
                onClick={handleSubmit}
                disabled={loading || step !== steps.length - 1}
              >
                제출하기
              </button>
            </div>
            {step === 0 && (
              <div style={{ marginBottom: '1.5rem' }}>
                <div className="mb-4">
                  <label className="block font-semibold mb-1">제목</label>
                  <input type="text" className="w-full border border-gray-200 rounded-lg px-4 py-3 bg-gray-50 focus:outline-none focus:ring-2 focus:ring-teal-200" value={form.title} onChange={e => handleChange("title", e.target.value)} required maxLength={30} />
                  <div className="text-right text-xs text-gray-400 mt-1">{form.title.length} / 30</div>
                </div>
                <div className="mb-4">
                  <label className="block font-semibold mb-1">카테고리</label>
                  <div className="flex flex-col gap-2 bg-gray-50 rounded-lg p-4 border border-gray-100">
                    {/* 1차 카테고리 */}
                    <select className="w-full border border-gray-200 rounded-lg px-4 py-3 bg-white focus:outline-none focus:ring-2 focus:ring-teal-200" value={selectedCategory1} onChange={handleCategory1Change} required>
                      <option value="">1차 카테고리 선택</option>
                      {category1Options.map(cat => (
                        <option key={cat.id} value={cat.id}>{cat.name}</option>
                      ))}
                    </select>
                    {/* 2차 카테고리: 항상 표시, 1차 선택 전에는 disabled */}
                    <select className="w-full border border-gray-200 rounded-lg px-4 py-3 bg-white focus:outline-none focus:ring-2 focus:ring-teal-200" value={selectedCategory2} onChange={handleCategory2Change} required disabled={!selectedCategory1}>
                      <option value="">2차 카테고리 선택</option>
                      {selectedCategory1 && category2Options.map(cat => (
                        <option key={cat.id} value={cat.id}>{cat.name}</option>
                      ))}
                    </select>
                    {/* 3차 카테고리: 2차 선택 시에만 표시 */}
                    {selectedCategory2 && category3Options.length > 0 && (
                      <select className="w-full border border-gray-200 rounded-lg px-4 py-3 bg-white focus:outline-none focus:ring-2 focus:ring-teal-200" value={selectedCategory3} onChange={handleCategory3Change} required>
                        <option value="">3차 카테고리 선택</option>
                        {category3Options.map(cat => (
                          <option key={cat.id} value={cat.id}>{cat.name}</option>
                        ))}
                      </select>
                    )}
                  </div>
                </div>
                {/* TIP 박스 */}
                <div className="mt-4 p-4 bg-blue-50 border border-blue-200 rounded-lg text-sm text-blue-800">
                  <span className="font-bold mr-2">TIP</span>
                  • 복합적인 성격의 서비스는 적합한 카테고리에 각각 분리하여 등록해 주세요.
                </div>
              </div>
            )}
            {/* 나머지 단계는 기존과 동일하게 렌더링 */}
            {step !== 0 && renderStep()}
            {/* 이전, 다음 버튼 제거됨 */}
          </div>
        </main>
      </div>
    </div>
  );
};

export default ContentCreateStepperPage; 