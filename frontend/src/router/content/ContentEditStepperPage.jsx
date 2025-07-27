import React, { useState, useEffect } from "react";
import axiosInstance from "../../lib/axios";
import { useNavigate, useParams } from "react-router-dom";
import styles from "./css/StepperPageCommon.module.css";
import toast from "react-hot-toast";

const steps = ["기본정보", "가격설정", "서비스 설명", "이미지"];

const ContentEditStepperPage = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [step, setStep] = useState(0);
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
    etc: "",
  });
  const [loading, setLoading] = useState(false);
  const [existingImages, setExistingImages] = useState([]);
  const [existingThumbnail, setExistingThumbnail] = useState(null);

  // 카테고리 트리 불러오기
  useEffect(() => {
    axiosInstance.get("/api/categories/tree").then((res) => {
      setCategoryTree(res.data);
    });
  }, []);

  // 기존 콘텐츠 상세 불러오기
  useEffect(() => {
    axiosInstance.get(`/api/content/${id}`).then((res) => {
      const data = res.data;
      setForm({
        title: data.title || "",
        categoryId: data.categoryId || "",
        budget: data.budget || "",
        questions:
          data.questions && data.questions.length > 0
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
        etc: "",
      });
      setExistingImages(data.imageUrls || []);
      setExistingThumbnail(data.contentUrl || null);
    });
  }, [id]);

  // 카테고리 트리와 form.categoryId가 모두 준비된 후, selectedCategory1/2/3을 항상 동기화하고, 셀렉트 박스에서 직접 변경이 일어나면 동기화 useEffect가 다시 실행되지 않도록 개선한다. (selectedCategory1/2/3을 form.categoryId에서만 derive, setState로 직접 변경하지 않음)
  const [selectedCategory1, setSelectedCategory1] = useState("");
  const [selectedCategory2, setSelectedCategory2] = useState("");
  const [selectedCategory3, setSelectedCategory3] = useState("");

  // form.categoryId, categoryTree가 바뀔 때마다 selectedCategory1/2/3 동기화
  useEffect(() => {
    if (!categoryTree || categoryTree.length === 0) return;
    if (!form.categoryId) {
      setSelectedCategory1("");
      setSelectedCategory2("");
      setSelectedCategory3("");
      return;
    }
    function findCategoryPath(tree, targetId, path = []) {
      for (const node of tree) {
        if (String(node.id) === String(targetId)) {
          return [...path, node.id];
        }
        if (node.children && node.children.length > 0) {
          const result = findCategoryPath(node.children, targetId, [...path, node.id]);
          if (result) return result;
        }
      }
      return null;
    }
    const path = findCategoryPath(categoryTree, form.categoryId);
    setSelectedCategory1(path?.[0] || "");
    setSelectedCategory2(path?.[1] || "");
    setSelectedCategory3(path?.[2] || "");
  }, [categoryTree, form.categoryId]);

  // 카테고리 단계별 옵션 추출
  const category1Options = categoryTree;
  const category2Options = selectedCategory1 ? categoryTree.find((cat) => String(cat.id) === String(selectedCategory1))?.children || [] : [];
  const category3Options = selectedCategory2 ? category2Options.find((cat) => String(cat.id) === String(selectedCategory2))?.children || [] : [];

  // 카테고리 선택 핸들러
  const handleCategory1Change = (e) => {
    const val = e.target.value;
    setSelectedCategory1(val);
    setSelectedCategory2("");
    setSelectedCategory3("");
    if (!val) {
      setForm((prev) => ({ ...prev, categoryId: "" }));
      return;
    }
    if ((categoryTree.find((cat) => String(cat.id) === String(val))?.children || []).length === 0) {
      setForm((prev) => ({ ...prev, categoryId: val }));
    } else {
      setForm((prev) => ({ ...prev, categoryId: "" }));
    }
  };
  const handleCategory2Change = (e) => {
    const val = e.target.value;
    setSelectedCategory2(val);
    setSelectedCategory3("");
    if (!val) {
      setForm((prev) => ({ ...prev, categoryId: selectedCategory1 }));
      return;
    }
    if ((category2Options.find((cat) => String(cat.id) === String(val))?.children || []).length === 0) {
      setForm((prev) => ({ ...prev, categoryId: val }));
    } else {
      setForm((prev) => ({ ...prev, categoryId: "" }));
    }
  };
  const handleCategory3Change = (e) => {
    const val = e.target.value;
    setSelectedCategory3(val);
    setForm((prev) => ({ ...prev, categoryId: val }));
  };
  useEffect(() => {
    if (selectedCategory2 && category3Options.length === 0) {
      setForm((prev) => ({ ...prev, categoryId: selectedCategory2 }));
    }
  }, [selectedCategory2, category3Options.length]);
  useEffect(() => {
    if (selectedCategory1 && category2Options.length === 0) {
      setForm((prev) => ({ ...prev, categoryId: selectedCategory1 }));
    }
  }, [selectedCategory1, category2Options.length]);

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
      questions: [...prev.questions, { questionText: "", multipleChoice: false, options: [{ optionText: "", additionalPrice: 0 }] }],
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
  const goToStep = (idx) => setStep(idx);

  // 제출
  const handleSubmit = async () => {
    setLoading(true);
    try {
      // 1. content 수정
      await axiosInstance.put(`/api/content/${id}`, {
        title: form.title,
        description: form.description,
        budget: Number(form.budget),
        categoryId: Number(form.categoryId),
        questions: form.questions,
      });
      // 2. 이미지 업로드 (등록과 동일하게 동작, 기존 이미지는 서버에서 soft delete 처리)
      if (form.images.length > 0 || form.thumbnail) {
        const formData = new FormData();
        form.images.forEach((img) => formData.append("images", img));
        if (form.thumbnail) formData.append("thumbnail", form.thumbnail);
        await axiosInstance.put(`/api/content/${id}/images`, formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });
      }
      toast.success("콘텐츠가 수정되었습니다!");
      navigate(`/content/${id}`);
    } catch (err) {
      toast.error("수정 중 오류가 발생했습니다");
    } finally {
      setLoading(false);
    }
  };

  // 단계별 폼 렌더링 (등록과 동일, 단 form 상태만 다름)
  const renderStep = () => {
    switch (step) {
      case 0:
        return (
          <div>
            <div className="mb-4">
              <label className="block font-semibold mb-1">제목</label>
              <input type="text" className="w-full border rounded px-3 py-2" value={form.title} onChange={(e) => handleChange("title", e.target.value)} required maxLength={30} />
              <div className="text-right text-xs text-gray-400">{form.title.length} / 30</div>
            </div>
            <div className="mb-4">
              <label className="block font-semibold mb-1">카테고리</label>
              <div className="flex flex-col gap-2">
                <select className="w-full border rounded px-3 py-2" value={selectedCategory1} onChange={handleCategory1Change} required>
                  <option value="">1차 카테고리 선택</option>
                  {category1Options.map((cat) => (
                    <option key={cat.id} value={cat.id}>
                      {cat.name}
                    </option>
                  ))}
                </select>
                <select className="w-full border rounded px-3 py-2" value={selectedCategory2} onChange={handleCategory2Change} required disabled={!selectedCategory1}>
                  <option value="">2차 카테고리 선택</option>
                  {selectedCategory1 &&
                    category2Options.map((cat) => (
                      <option key={cat.id} value={cat.id}>
                        {cat.name}
                      </option>
                    ))}
                </select>
                {selectedCategory2 && category3Options.length > 0 && (
                  <select className="w-full border rounded px-3 py-2" value={selectedCategory3} onChange={handleCategory3Change} required>
                    <option value="">3차 카테고리 선택</option>
                    {category3Options.map((cat) => (
                      <option key={cat.id} value={cat.id}>
                        {cat.name}
                      </option>
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
              <input type="number" className="w-full border rounded px-3 py-2" value={form.budget} onChange={(e) => handleChange("budget", e.target.value)} required min={0} />
            </div>
            <div className="mb-4">
              <label className="block font-semibold mb-1">옵션/질문 리스트</label>
              {form.questions.map((q, qIdx) => (
                <div key={qIdx} className="border rounded p-3 mb-2 bg-gray-50">
                  <div className="flex gap-2 mb-2">
                    <input type="text" className="flex-1 border rounded px-2 py-1" placeholder="질문 내용" value={q.questionText} onChange={(e) => handleQuestionChange(qIdx, "questionText", e.target.value)} required />
                    <label className="flex items-center gap-1">
                      <input type="checkbox" checked={q.multipleChoice} onChange={(e) => handleQuestionChange(qIdx, "multipleChoice", e.target.checked)} /> 중복 허용 여부
                    </label>
                    <button type="button" className="text-red-500" onClick={() => removeQuestion(qIdx)} disabled={form.questions.length === 1}>
                      삭제
                    </button>
                  </div>
                  <div className="ml-4">
                    {q.options.map((opt, oIdx) => (
                      <div key={oIdx} className="flex gap-2 mb-1">
                        <input type="text" className="border rounded px-2 py-1" placeholder="옵션명" value={opt.optionText} onChange={(e) => handleOptionChange(qIdx, oIdx, "optionText", e.target.value)} required />
                        <input type="number" className="border rounded px-2 py-1 w-24" placeholder="추가금액" value={opt.additionalPrice} onChange={(e) => handleOptionChange(qIdx, oIdx, "additionalPrice", Number(e.target.value))} min={0} required />
                        <button type="button" className="text-red-400" onClick={() => removeOption(qIdx, oIdx)} disabled={q.options.length === 1}>
                          옵션 삭제
                        </button>
                      </div>
                    ))}
                    <button type="button" className="text-blue-500 mt-1" onClick={() => addOption(qIdx)}>
                      옵션 추가
                    </button>
                  </div>
                </div>
              ))}
              <button type="button" className="text-blue-600 mt-2" onClick={addQuestion}>
                질문 추가
              </button>
            </div>
          </div>
        );
      case 2:
        return (
          <div>
            <div className="mb-4">
              <label className="block font-semibold mb-1">서비스 설명</label>
              <textarea className="w-full border rounded px-3 py-2 min-h-[120px]" value={form.description} onChange={(e) => handleChange("description", e.target.value)} required maxLength={1000} />
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
              <div className="relative w-[326px] h-[244px] border rounded flex flex-col items-center justify-center cursor-pointer bg-gray-50" onClick={() => document.getElementById("main-image-input-edit").click()}>
                {form.thumbnail ? (
                  <img src={URL.createObjectURL(form.thumbnail)} alt="thumbnail" className="w-full h-full object-cover rounded" />
                ) : existingThumbnail ? (
                  <img src={existingThumbnail} alt="thumbnail" className="w-full h-full object-cover rounded" />
                ) : (
                  <>
                    <div className="flex flex-col items-center">
                      <svg width="48" height="48" fill="none" viewBox="0 0 24 24">
                        <path d="M12 5v14m7-7H5" stroke="#bbb" strokeWidth="2" strokeLinecap="round" />
                      </svg>
                      <div className="text-gray-400 text-sm mt-2">
                        652 × 488px
                        <br />
                        (4:3 비율)
                      </div>
                    </div>
                  </>
                )}
                <input id="main-image-input-edit" type="file" accept="image/*" className="hidden" onChange={handleThumbnailChange} />
              </div>
            </div>
            {/* 상세 이미지 업로드 */}
            <div className="mb-6">
              <label className="block font-semibold mb-1">이미지 업로드 (최대 5장)</label>
              <div className="relative w-[326px] h-[244px] border rounded flex flex-col items-center justify-center cursor-pointer bg-gray-50" onClick={() => document.getElementById("detail-image-input-edit").click()}>
                {form.images && form.images.length > 0 ? (
                  <div className="flex gap-2 flex-wrap">
                    {Array.from(form.images).map((img, idx) => (
                      <img key={idx} src={URL.createObjectURL(img)} alt="preview" className="w-16 h-16 object-cover rounded" />
                    ))}
                  </div>
                ) : existingImages && existingImages.length > 0 ? (
                  <div className="flex gap-2 flex-wrap">
                    {existingImages.map((img, idx) => (
                      <img key={idx} src={img} alt="preview" className="w-16 h-16 object-cover rounded" />
                    ))}
                  </div>
                ) : (
                  <>
                    <div className="flex flex-col items-center">
                      <svg width="48" height="48" fill="none" viewBox="0 0 24 24">
                        <path d="M12 5v14m7-7H5" stroke="#bbb" strokeWidth="2" strokeLinecap="round" />
                      </svg>
                      <div className="text-gray-400 text-sm mt-2">최대 5장 업로드 가능</div>
                    </div>
                  </>
                )}
                <input id="detail-image-input-edit" type="file" accept="image/*" multiple className="hidden" onChange={handleImagesChange} />
              </div>
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "flex-start",
        width: "100%",
        minHeight: "100vh",
        background: "#f9fafb",
        padding: "4rem 0",
      }}>
      <div
        style={{
          display: "flex",
          background: "#fff",
          borderRadius: "1.5rem",
          boxShadow: "0 2px 8px rgba(0,0,0,0.04)",
          border: "1px solid #f3f4f6",
          overflow: "hidden",
          minWidth: "800px",
          maxWidth: "1100px",
          width: "100%",
        }}>
        <aside
          style={{
            width: "256px",
            background: "#f9fafb",
            borderRight: "1px solid #e5e7eb",
            display: "flex",
            flexDirection: "column",
            padding: "2rem 1.5rem 0 1.5rem",
            minHeight: "600px",
            marginRight: 0,
          }}>
          {steps.map((label, idx) => (
            <button key={label} className={step === idx ? `${styles.stepperSidebarButton} ${styles.stepperSidebarButtonActive}` : `${styles.stepperSidebarButton} ${styles.stepperSidebarButtonInactive}`} onClick={() => goToStep(idx)}>
              <span className={step === idx ? `${styles.stepperSidebarStepCircle} ${styles.stepperSidebarStepCircleActive}` : styles.stepperSidebarStepCircle}>{idx + 1}</span>
              {label}
            </button>
          ))}
          <div style={{ marginTop: "2rem", padding: "1rem", background: "#fff", borderRadius: "0.75rem", border: "1px solid #f3f4f6", boxShadow: "0 1px 4px rgba(0,0,0,0.03)" }}>
            <span style={{ fontSize: "0.75rem", color: "#2563eb", fontWeight: 600, marginBottom: "0.5rem", display: "block" }}>한 번에 통과하는</span>
            <span style={{ fontSize: "0.75rem", color: "#6b7280" }}>서비스 등록 가이드</span>
          </div>
        </aside>
        {/* 우측 폼 */}
        <main
          style={{
            flex: 1,
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            background: "#fff",
            minHeight: "600px",
            width: "100%",
          }}>
          <div
            style={{
              width: "100%",
              maxWidth: "40rem",
              background: "#fff",
              borderRadius: "1.5rem",
              boxShadow: "none",
              padding: "2.5rem",
              border: "none",
            }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "2rem" }}>
              <h2 style={{ fontSize: "1.25rem", fontWeight: "bold" }}>{steps[step]}</h2>
              <button className={`${styles.stepperButton} ${styles.stepperButtonPrimary}`} onClick={handleSubmit} disabled={loading || step !== steps.length - 1}>
                수정하기
              </button>
            </div>
            {step === 0 && (
              <div style={{ marginBottom: "1.5rem" }}>
                <div className="mb-4">
                  <label className="block font-semibold mb-1">제목</label>
                  <input type="text" className="w-full border border-gray-200 rounded-lg px-4 py-3 bg-gray-50 focus:outline-none focus:ring-2 focus:ring-teal-200" value={form.title} onChange={(e) => handleChange("title", e.target.value)} required maxLength={30} />
                  <div className="text-right text-xs text-gray-400 mt-1">{form.title.length} / 30</div>
                </div>
                <div className="mb-4">
                  <label className="block font-semibold mb-1">카테고리</label>
                  <div className="flex flex-col gap-2 bg-gray-50 rounded-lg p-4 border border-gray-100">
                    <select className="w-full border border-gray-200 rounded-lg px-4 py-3 bg-white focus:outline-none focus:ring-2 focus:ring-teal-200" value={selectedCategory1} onChange={handleCategory1Change} required>
                      <option value="">1차 카테고리 선택</option>
                      {category1Options.map((cat) => (
                        <option key={cat.id} value={cat.id}>
                          {cat.name}
                        </option>
                      ))}
                    </select>
                    <select className="w-full border border-gray-200 rounded-lg px-4 py-3 bg-white focus:outline-none focus:ring-2 focus:ring-teal-200" value={selectedCategory2} onChange={handleCategory2Change} required disabled={!selectedCategory1}>
                      <option value="">2차 카테고리 선택</option>
                      {selectedCategory1 &&
                        category2Options.map((cat) => (
                          <option key={cat.id} value={cat.id}>
                            {cat.name}
                          </option>
                        ))}
                    </select>
                    {selectedCategory2 && category3Options.length > 0 && (
                      <select className="w-full border border-gray-200 rounded-lg px-4 py-3 bg-white focus:outline-none focus:ring-2 focus:ring-teal-200" value={selectedCategory3} onChange={handleCategory3Change} required>
                        <option value="">3차 카테고리 선택</option>
                        {category3Options.map((cat) => (
                          <option key={cat.id} value={cat.id}>
                            {cat.name}
                          </option>
                        ))}
                      </select>
                    )}
                  </div>
                </div>
                <div className="mt-4 p-4 bg-blue-50 border border-blue-200 rounded-lg text-sm text-blue-800">
                  <span className="font-bold mr-2">TIP</span>• 복합적인 성격의 서비스는 적합한 카테고리에 각각 분리하여 등록해 주세요.
                </div>
              </div>
            )}
            {step !== 0 && renderStep()}
          </div>
        </main>
      </div>
    </div>
  );
};

export default ContentEditStepperPage;
