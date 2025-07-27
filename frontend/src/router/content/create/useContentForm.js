import { useState } from "react";

export const useContentForm = () => {
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

  const handleChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

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
        {
          questionText: "",
          multipleChoice: false,
          options: [{ optionText: "", additionalPrice: 0 }],
        },
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

  // 이미지 추가/삭제 모두 처리 (최대 5개)
  const handleImagesChange = (input) => {
    if (input && input.type === "delete" && Array.isArray(input.images)) {
      setForm((prev) => ({ ...prev, images: input.images }));
      return;
    }
    // 추가 로직
    const newFiles = Array.from(input);
    setForm((prev) => {
      const combined = [...prev.images, ...newFiles].slice(0, 5);
      return { ...prev, images: combined };
    });
  };

  const handleThumbnailChange = (file) => {
    setForm((prev) => ({ ...prev, thumbnail: file }));
  };

  return {
    form,
    handleChange,
    handleQuestionChange,
    handleOptionChange,
    addQuestion,
    removeQuestion,
    addOption,
    removeOption,
    handleImagesChange,
    handleThumbnailChange,
  };
};
