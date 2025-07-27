import { useState, useEffect } from "react";
import { Trash2, User, MapPin, GraduationCap, Users, Globe, Facebook, Instagram, Twitter, Briefcase, Code, AlertCircle, Plus, ChevronDown, ChevronRight } from "lucide-react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../../../lib/axios";

// Form Components
const InputGroup = ({ label, icon, required, children, error }) => {
  const IconComponent = icon;
  return (
    <div className="space-y-3">
      <div className="flex items-center gap-2">
        {IconComponent && <IconComponent size={18} className="text-slate-700" />}
        <label className="text-sm font-semibold text-gray-800">
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      </div>
      {children}
      {error && (
        <div className="flex items-center gap-2 text-sm text-red-600 bg-red-50/80 px-3 py-2 rounded-lg border border-red-200">
          <AlertCircle size={14} />
          {error}
        </div>
      )}
    </div>
  );
};

const ModernInput = ({ error, ...props }) => (
  <input
    {...props}
    className={`w-full px-4 py-3 rounded-xl border-2 bg-white/90 backdrop-blur-sm
      transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-slate-500/20
      ${error ? "border-red-300 focus:border-red-500" : "border-gray-300 focus:border-slate-500 hover:border-gray-400"}`}
  />
);

const ModernSelect = ({ error, children, ...props }) => (
  <select
    {...props}
    className={`w-full px-4 py-3 rounded-xl border-2 bg-white/90 backdrop-blur-sm
      transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-slate-500/20
      ${error ? "border-red-300 focus:border-red-500" : "border-gray-300 focus:border-slate-500 hover:border-gray-400"}`}>
    {children}
  </select>
);

const ModernTextarea = ({ error, ...props }) => (
  <textarea
    {...props}
    className={`w-full px-4 py-3 rounded-xl border-2 bg-white/90 backdrop-blur-sm
      resize-none transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-slate-500/20
      ${error ? "border-red-300 focus:border-red-500" : "border-gray-300 focus:border-slate-500 hover:border-gray-400"}`}
  />
);

const AddButton = ({ onClick, children }) => (
  <button
    type="button"
    onClick={onClick}
    className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-slate-700
      hover:text-slate-800 hover:bg-slate-50 rounded-xl border-2 border-dashed
      border-slate-300 hover:border-slate-400 transition-all duration-200">
    <Plus size={16} />
    {children}
  </button>
);

const DeleteButton = ({ onClick, disabled }) => (
  <button
    type="button"
    onClick={onClick}
    disabled={disabled}
    className={`flex items-center justify-center w-10 h-10 rounded-xl transition-all duration-200
      border ${disabled ? "text-gray-300 border-gray-200 cursor-not-allowed" : "text-red-500 hover:text-red-700 hover:bg-red-50 border-red-200 hover:border-red-300"}`}>
    <Trash2 size={16} />
  </button>
);

// 토글 방식 기술 선택 컴포넌트
const SkillToggleSelector = ({ meta, selectedSkills, onSkillToggle, error }) => {
  const [expandedCategories, setExpandedCategories] = useState(new Set());
  const [searchTerm, setSearchTerm] = useState("");

  const toggleCategory = (categoryName) => {
    const newExpanded = new Set(expandedCategories);
    if (newExpanded.has(categoryName)) {
      newExpanded.delete(categoryName);
    } else {
      newExpanded.add(categoryName);
    }
    setExpandedCategories(newExpanded);
  };

  const isSkillSelected = (categoryName, skillName) => {
    return selectedSkills.some((skill) => skill.category === categoryName && skill.name === skillName);
  };

  const getSelectedSkillsCount = () => selectedSkills.filter((s) => s.category && s.name).length;

  // 검색 기능: 기술명이나 카테고리명에 검색어가 포함된 것 필터링
  const getFilteredSkills = (category) => {
    if (!searchTerm.trim()) return category.skills;

    return category.skills.filter((skill) => skill.toLowerCase().includes(searchTerm.toLowerCase()));
  };

  // 검색어가 있을 때 카테고리 자체를 숨길지 결정
  const shouldShowCategory = (category) => {
    if (!searchTerm.trim()) return true;

    // 카테고리명에 검색어가 포함되거나, 해당 카테고리의 기술 중 검색어가 포함된 것이 있으면 표시
    return category.categoryName.toLowerCase().includes(searchTerm.toLowerCase()) || category.skills.some((skill) => skill.toLowerCase().includes(searchTerm.toLowerCase()));
  };

  return (
    <div className="space-y-4">
      <div className="text-sm text-gray-600 bg-slate-50 p-3 rounded-lg border border-slate-200">💡 최소 1개, 최대 20개까지 선택 가능합니다. 현재 {getSelectedSkillsCount()}개 선택됨</div>

      {/* 검색 입력창 */}
      <div className="relative">
        <input
          type="text"
          placeholder="기술이나 카테고리를 검색해보세요..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full px-4 py-3 pl-10 rounded-xl border-2 bg-white/90 backdrop-blur-sm
            transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-slate-500/20
            border-gray-300 focus:border-slate-500 hover:border-gray-400"
        />
        <div className="absolute left-3 top-1/2 transform -translate-y-1/2">
          <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
      </div>

      <div className="space-y-2">
        {meta.skills
          .filter((category) => shouldShowCategory(category))
          .map((category) => {
            const filteredSkills = getFilteredSkills(category);

            return (
              <div key={category.categoryName} className="border border-gray-200 rounded-lg overflow-hidden">
                {/* 카테고리 헤더 */}
                <button type="button" onClick={() => toggleCategory(category.categoryName)} className="w-full flex items-center justify-between p-4 bg-gray-50 hover:bg-gray-100 transition-colors">
                  <span className="font-medium text-gray-800">
                    {category.categoryName}
                    {searchTerm && filteredSkills.length !== category.skills.length && <span className="ml-2 text-sm text-blue-600">({filteredSkills.length}개 검색됨)</span>}
                  </span>
                  <div className="flex items-center gap-2">
                    <span className="text-sm text-gray-500">{selectedSkills.filter((s) => s.category === category.categoryName).length}개 선택됨</span>
                    {expandedCategories.has(category.categoryName) ? <ChevronDown size={20} className="text-gray-500" /> : <ChevronRight size={20} className="text-gray-500" />}
                  </div>
                </button>

                {/* 기술 목록 - 고정 높이 + 스크롤 */}
                {expandedCategories.has(category.categoryName) && (
                  <div className="bg-white border-t border-gray-200">
                    <div className="max-h-64 overflow-y-auto p-4">
                      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2">
                        {filteredSkills.map((skill) => {
                          const isSelected = isSkillSelected(category.categoryName, skill);
                          return (
                            <button
                              key={skill}
                              type="button"
                              onClick={() => onSkillToggle(category.categoryName, skill)}
                              className={`px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200 text-left
                                ${isSelected ? "bg-blue-100 text-blue-800 border-2 border-blue-300 shadow-sm" : "bg-gray-50 text-gray-700 border-2 border-transparent hover:bg-gray-100 hover:border-gray-200"}`}>
                              {skill}
                            </button>
                          );
                        })}
                      </div>
                      {filteredSkills.length === 0 && <div className="text-center py-8 text-gray-500">검색 결과가 없습니다.</div>}
                    </div>
                  </div>
                )}
              </div>
            );
          })}
      </div>

      {/* 검색 결과가 없을 때 */}
      {searchTerm && !meta.skills.some((category) => shouldShowCategory(category)) && <div className="text-center py-8 text-gray-500 bg-gray-50 rounded-lg border border-gray-200">"{searchTerm}"에 대한 검색 결과가 없습니다.</div>}

      {/* 선택된 기술 태그 표시 */}
      {getSelectedSkillsCount() > 0 && (
        <div className="mt-6">
          <div className="text-sm font-medium text-gray-700 mb-3">선택된 기술</div>
          <div className="flex flex-wrap gap-2">
            {selectedSkills
              .filter((s) => s.category && s.name)
              .map((s, idx) => (
                <span key={idx} className="inline-flex items-center gap-2 px-3 py-1 bg-slate-100 text-slate-700 rounded-full text-sm border border-slate-200">
                  {s.category} • {s.name}
                  <button type="button" onClick={() => onSkillToggle(s.category, s.name)} className="text-slate-500 hover:text-red-500 transition-colors">
                    ×
                  </button>
                </span>
              ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default function Register() {
  const [form, setForm] = useState({
    specialties: [{ specialty: "", detailFields: [""] }],
    introduction: "",
    region: "",
    totalCareerYears: "",
    education: "",
    employeeCount: "",
    websiteUrl: "",
    facebookUrl: "",
    instagramUrl: "",
    xUrl: "",
    skills: [],
    careers: [""],
  });
  const navigate = useNavigate();
  const [errors, setErrors] = useState({});
  const [meta, setMeta] = useState({ detailFields: [], skills: [], regions: [] });

  useEffect(() => {
    const fetchMeta = async () => {
      try {
        const res = await axiosInstance.get("/api/expert/meta");
        setMeta(res.data);
      } catch (error) {
        console.error("메타데이터 가져오기 실패:", error);
      }
    };

    fetchMeta();
  }, []);

  // 기술 토글 핸들러
  const handleSkillToggle = (categoryName, skillName) => {
    setForm((prev) => {
      const existingIndex = prev.skills.findIndex((skill) => skill.category === categoryName && skill.name === skillName);

      let newSkills;
      if (existingIndex >= 0) {
        // 이미 선택된 기술이면 제거
        newSkills = prev.skills.filter((_, index) => index !== existingIndex);
      } else {
        // 선택되지 않은 기술이면 추가 (최대 20개 제한)
        if (prev.skills.filter((s) => s.category && s.name).length >= 20) {
          toast.error("최대 20개까지만 선택할 수 있습니다.");
          return prev;
        }
        newSkills = [...prev.skills, { category: categoryName, name: skillName }];
      }

      return { ...prev, skills: newSkills };
    });

    // 에러 클리어
    if (errors.skills) {
      setErrors((prev) => ({ ...prev, skills: undefined }));
    }
  };

  // 유효성 검사
  const validateForm = () => {
    const newErrors = {};

    // 전문분야 검증
    if (!form.specialties.length || form.specialties.length > 3) {
      newErrors.specialties = "전문 분야는 1~3개 선택해야 합니다.";
    } else {
      form.specialties.forEach((specialty, idx) => {
        if (!specialty.specialty) {
          newErrors[`specialty_${idx}`] = "전문 분야를 선택해주세요.";
        }
        if (!specialty.detailFields.length || specialty.detailFields.some((field) => !field)) {
          newErrors[`detailFields_${idx}`] = "세부 분야를 최소 1개 이상 선택해주세요.";
        }
      });
    }

    // 필수 필드 검증
    if (!form.introduction.trim()) newErrors.introduction = "자기소개를 입력해주세요.";
    if (!form.region) newErrors.region = "활동 지역을 선택해주세요.";
    if (!form.totalCareerYears || form.totalCareerYears < 0) newErrors.totalCareerYears = "총 경력을 입력해주세요.";
    if (!form.education.trim()) newErrors.education = "학력을 입력해주세요.";
    if (form.employeeCount === "" || form.employeeCount < 0) newErrors.employeeCount = "직원 수를 입력해주세요.";

    // 기술 검증
    const validSkills = form.skills.filter((s) => s.category && s.name);
    if (!validSkills.length) {
      newErrors.skills = "기술을 최소 1개 이상 선택해주세요.";
    }

    // 경력사항 검증
    if (!form.careers.length || form.careers.some((career) => !career.trim())) {
      newErrors.careers = "경력사항을 최소 1개 이상 입력해주세요.";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 공통 핸들러
  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  // 배열 핸들러
  const handleArrayChange = (e, idx, key, arrName) => {
    const value = e.target.value;
    setForm((prev) => {
      const arr = [...prev[arrName]];
      if (key) arr[idx][key] = value;
      else arr[idx] = value;
      return { ...prev, [arrName]: arr };
    });
  };

  // 세부분야 핸들러
  const handleDetailFieldChange = (e, sIdx, dIdx) => {
    const value = e.target.value;
    setForm((prev) => {
      const specialties = [...prev.specialties];
      specialties[sIdx].detailFields[dIdx] = value;
      return { ...prev, specialties };
    });
  };

  // 배열 추가/삭제
  const addArrayItem = (arrName, item) => {
    setForm((prev) => ({ ...prev, [arrName]: [...prev[arrName], item] }));
  };

  const removeArrayItem = (arrName, idx) => {
    setForm((prev) => {
      const arr = [...prev[arrName]];
      arr.splice(idx, 1);
      return { ...prev, [arrName]: arr };
    });
  };

  const handleSubmit = async () => {
    if (!validateForm()) {
      toast.error("필수 입력 항목을 확인해주세요.");
      return;
    }

    try {
      console.log("전문가 등록 데이터:", form);
      toast.success("전문가 등록이 완료되었습니다!");
      await axiosInstance.post("/api/expert/upgrade", form);
      navigate("/expert/profile"); // 등록 후 프로필 페이지로 이동
    } catch (error) {
      console.error("등록 실패:", error);
      toast.error("등록에 실패했습니다. 다시 시도해 주세요.");
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-slate-50 to-zinc-100 py-8 px-4">
      <div className="max-w-5xl mx-auto">
        {/* Header */}
        <div className="text-center mb-10">
          <h1 className="text-5xl font-bold bg-gradient-to-r from-slate-800 via-zinc-700 to-gray-800 bg-clip-text text-transparent mb-4">전문가 등록</h1>
          <p className="text-xl text-gray-600 mb-4">전문분야를 입력해서 전문가로 전환해보세요!</p>
          <div className="inline-flex items-center gap-2 bg-orange-50 text-orange-800 px-4 py-2 rounded-full text-sm font-medium border border-orange-200">
            <span className="text-red-500">*</span>
            필수 입력 항목입니다
          </div>
        </div>

        <div className="bg-white/90 backdrop-blur-xl rounded-3xl border border-gray-200/50 shadow-2xl p-10 space-y-10">
          {/* 전문분야 */}
          <InputGroup label="전문분야" icon={Briefcase} required error={errors.specialties}>
            <div className="space-y-6">
              {form.specialties.map((s, sIdx) => (
                <div key={sIdx} className="bg-gradient-to-r from-slate-50/80 to-gray-50/80 backdrop-blur-sm rounded-2xl p-6 border border-slate-200/50">
                  <div className="mb-4">
                    <ModernSelect value={s.specialty} onChange={(e) => handleArrayChange(e, sIdx, "specialty", "specialties")} error={errors[`specialty_${sIdx}`]}>
                      <option value="">전문분야 선택</option>
                      {meta.detailFields.map((cat) => (
                        <option key={cat.specialty} value={cat.specialty}>
                          {cat.specialty}
                        </option>
                      ))}
                    </ModernSelect>
                  </div>

                  <div className="space-y-3">
                    <div className="text-sm font-medium text-gray-700 mb-3">
                      세부 분야 <span className="text-red-500">*</span>
                    </div>

                    {s.detailFields.map((d, dIdx) => (
                      <div key={dIdx} className="flex gap-3 items-center">
                        <ModernSelect value={d} onChange={(e) => handleDetailFieldChange(e, sIdx, dIdx)} className="flex-1">
                          <option value="">세부 분야 선택</option>
                          {(meta.detailFields.find((f) => f.specialty === s.specialty)?.detailFields || []).map((df) => (
                            <option key={df} value={df}>
                              {df}
                            </option>
                          ))}
                        </ModernSelect>
                        <DeleteButton
                          onClick={() => {
                            const arr = [...form.specialties];
                            arr[sIdx].detailFields.splice(dIdx, 1);
                            setForm((f) => ({ ...f, specialties: arr }));
                          }}
                          disabled={s.detailFields.length === 1}
                        />
                      </div>
                    ))}

                    <div className="flex gap-3 pt-3">
                      {form.specialties[sIdx].detailFields.length < 5 && (
                        <AddButton
                          onClick={() => {
                            const arr = [...form.specialties];
                            arr[sIdx].detailFields.push("");
                            setForm((f) => ({ ...f, specialties: arr }));
                          }}>
                          세부분야 추가 ({form.specialties[sIdx].detailFields.length}/5)
                        </AddButton>
                      )}

                      {form.specialties.length > 1 && (
                        <button
                          type="button"
                          onClick={() => removeArrayItem("specialties", sIdx)}
                          className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-red-600
                            hover:text-red-700 hover:bg-red-50 rounded-xl border border-red-200 hover:border-red-300 transition-all duration-200">
                          <Trash2 size={14} />
                          전문분야 삭제
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))}

              {form.specialties.length < 3 && <AddButton onClick={() => addArrayItem("specialties", { specialty: "", detailFields: [""] })}>전문분야 추가</AddButton>}
            </div>
          </InputGroup>

          {/* 자기소개 */}
          <InputGroup label="자기소개" icon={User} required error={errors.introduction}>
            <ModernTextarea name="introduction" value={form.introduction} onChange={handleChange} rows={5} placeholder="전문성과 경험을 간략히 소개해주세요..." />
          </InputGroup>

          {/* 기본 정보 */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <InputGroup label="활동 지역" icon={MapPin} required error={errors.region}>
              <ModernSelect name="region" value={form.region} onChange={handleChange}>
                <option value="">지역 선택</option>
                {meta.regions.map((r) => (
                  <option key={r} value={r}>
                    {r}
                  </option>
                ))}
              </ModernSelect>
            </InputGroup>

            <InputGroup label="총 경력 (년)" icon={Briefcase} required error={errors.totalCareerYears}>
              <ModernInput name="totalCareerYears" value={form.totalCareerYears} onChange={handleChange} type="number" min={0} placeholder="5" />
            </InputGroup>

            <InputGroup label="학력" icon={GraduationCap} required error={errors.education}>
              <ModernInput name="education" value={form.education} onChange={handleChange} placeholder="서울대학교 디자인학과" />
            </InputGroup>

            <InputGroup label="직원 수 (명)" icon={Users} required error={errors.employeeCount}>
              <ModernInput name="employeeCount" value={form.employeeCount} onChange={handleChange} type="number" min={0} placeholder="10" />
            </InputGroup>
          </div>

          {/* 소셜미디어 */}
          <InputGroup label="웹사이트 & 소셜미디어">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <div className="space-y-3">
                <div className="flex items-center gap-2 text-sm font-medium text-gray-700">
                  <Globe size={16} className="text-slate-600" />
                  웹사이트
                </div>
                <ModernInput name="websiteUrl" value={form.websiteUrl} onChange={handleChange} placeholder="https://example.com" />
              </div>

              <div className="space-y-3">
                <div className="flex items-center gap-2 text-sm font-medium text-gray-700">
                  <Facebook size={16} className="text-blue-600" />
                  Facebook
                </div>
                <ModernInput name="facebookUrl" value={form.facebookUrl} onChange={handleChange} placeholder="https://facebook.com/username" />
              </div>

              <div className="space-y-3">
                <div className="flex items-center gap-2 text-sm font-medium text-gray-700">
                  <Instagram size={16} className="text-pink-600" />
                  Instagram
                </div>
                <ModernInput name="instagramUrl" value={form.instagramUrl} onChange={handleChange} placeholder="https://instagram.com/username" />
              </div>

              <div className="space-y-3">
                <div className="flex items-center gap-2 text-sm font-medium text-gray-700">
                  <Twitter size={16} className="text-slate-600" />X (구 트위터)
                </div>
                <ModernInput name="xUrl" value={form.xUrl} onChange={handleChange} placeholder="https://x.com/username" />
              </div>
            </div>
          </InputGroup>

          {/* 보유 기술 - 토글 방식으로 변경 */}
          <InputGroup label="보유 기술" icon={Code} required error={errors.skills}>
            <SkillToggleSelector meta={meta} selectedSkills={form.skills} onSkillToggle={handleSkillToggle} error={errors.skills} />
          </InputGroup>

          {/* 경력사항 */}
          <InputGroup label="경력사항" icon={Briefcase} required error={errors.careers}>
            <div className="space-y-4">
              {form.careers.map((c, idx) => (
                <div key={idx} className="flex gap-3 items-center">
                  <ModernInput placeholder="경력 내용 (예: ABC 회사에서 3년 근무)" value={c} onChange={(e) => handleArrayChange(e, idx, null, "careers")} className="flex-1" />
                  <DeleteButton onClick={() => removeArrayItem("careers", idx)} />
                </div>
              ))}

              <AddButton onClick={() => addArrayItem("careers", "")}>경력 추가</AddButton>
            </div>
          </InputGroup>

          {/* 제출 버튼 */}
          <div className="pt-8">
            <button
              onClick={handleSubmit}
              className="w-full py-5 px-8 bg-gradient-to-r from-slate-700 via-gray-700 to-zinc-700
                text-white font-bold text-xl rounded-2xl shadow-2xl hover:shadow-3xl
                hover:from-slate-800 hover:via-gray-800 hover:to-zinc-800
                transform hover:scale-[1.02] transition-all duration-300
                focus:outline-none focus:ring-4 focus:ring-slate-500/30">
              전문가 등록 완료
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
