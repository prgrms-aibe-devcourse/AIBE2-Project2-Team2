import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { InputGroup, ModernInput, ModernSelect, ModernTextarea, AddButton, DeleteButton } from "../FormFields";
import { Briefcase, User, MapPin, GraduationCap, Users, Globe, Facebook, Instagram, Twitter, Code, Trash2 } from "lucide-react";
import axiosInstance from "../../../../lib/axios";

export default function EditProfile() {
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
  const [meta, setMeta] = useState({ detailFields: [], skills: [], regions: [] });
  const [errors, setErrors] = useState({});
  const navigate = useNavigate();

  useEffect(() => {
    // 프로필 정보 불러오기
    const fetchProfile = async () => {
      try {
        const res = await axiosInstance.get("/api/expert/profile");
        // API에서 받아온 데이터로 form 세팅
        const data = res.data;
        setForm({
          specialties: data.specialties?.length ? data.specialties : [{ specialty: "", detailFields: [""] }],
          introduction: data.introduction || "",
          region: data.region || "",
          totalCareerYears: data.totalCareerYears || "",
          education: data.education || "",
          employeeCount: data.employeeCount || "",
          websiteUrl: data.websiteUrl || "",
          facebookUrl: data.facebookUrl || "",
          instagramUrl: data.instagramUrl || "",
          xUrl: data.xUrl || "",
          skills: data.skills || [],
          careers: data.careers?.length ? data.careers : [""],
        });
      } catch (err) {
        alert("프로필 정보를 불러오지 못했습니다.");
      }
    };
    fetchProfile();
    // 메타데이터 불러오기
    axiosInstance.get("/api/expert/meta").then((res) => setMeta(res.data));
  }, []);

  // 공통 핸들러
  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors((prev) => ({ ...prev, [name]: undefined }));
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

  // 기술 토글 핸들러 (register.jsx와 동일하게 구현 필요)
  const handleSkillToggle = (categoryName, skillName) => {
    setForm((prev) => {
      const existingIndex = prev.skills.findIndex((skill) => skill.category === categoryName && skill.name === skillName);
      let newSkills;
      if (existingIndex >= 0) {
        newSkills = prev.skills.filter((_, index) => index !== existingIndex);
      } else {
        if (prev.skills.filter((s) => s.category && s.name).length >= 20) {
          alert("최대 20개까지만 선택할 수 있습니다.");
          return prev;
        }
        newSkills = [...prev.skills, { category: categoryName, name: skillName }];
      }
      return { ...prev, skills: newSkills };
    });
    if (errors.skills) setErrors((prev) => ({ ...prev, skills: undefined }));
  };

  // 유효성 검사 (register.jsx의 validateForm 참고, 필요시 추가)
  const validateForm = () => {
    const newErrors = {};
    if (!form.specialties.length || form.specialties.length > 3) {
      newErrors.specialties = "전문 분야는 1~3개 선택해야 합니다.";
    } else {
      form.specialties.forEach((specialty, idx) => {
        if (!specialty.specialty) newErrors[`specialty_${idx}`] = "전문 분야를 선택해주세요.";
        if (!specialty.detailFields.length || specialty.detailFields.some((field) => !field)) newErrors[`detailFields_${idx}`] = "세부 분야를 최소 1개 이상 선택해주세요.";
      });
    }
    if (!form.introduction.trim()) newErrors.introduction = "자기소개를 입력해주세요.";
    if (!form.region) newErrors.region = "활동 지역을 선택해주세요.";
    if (!form.totalCareerYears || form.totalCareerYears < 0) newErrors.totalCareerYears = "총 경력을 입력해주세요.";
    if (!form.education.trim()) newErrors.education = "학력을 입력해주세요.";
    if (form.employeeCount === "" || form.employeeCount < 0) newErrors.employeeCount = "직원 수를 입력해주세요.";
    const validSkills = form.skills.filter((s) => s.category && s.name);
    if (!validSkills.length) newErrors.skills = "기술을 최소 1개 이상 선택해주세요.";
    if (!form.careers.length || form.careers.some((career) => !career.trim())) newErrors.careers = "경력사항을 최소 1개 이상 입력해주세요.";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 제출
  const handleSubmit = async () => {
    if (!validateForm()) {
      alert("필수 입력 항목을 확인해주세요.");
      return;
    }
    try {
      await axiosInstance.put("/api/expert/profile", form);
      alert("프로필이 수정되었습니다!");
      navigate("/expert/profile");
    } catch (error) {
      alert("수정에 실패했습니다. 다시 시도해 주세요.");
    }
  };

  return (
    <div className="py-8 px-4">
      <div className="max-w-5xl mx-auto">
        <div className="text-center mb-10">
          <h1 className="text-5xl font-bold bg-gradient-to-r from-slate-800 via-zinc-700 to-gray-800 bg-clip-text text-transparent mb-4">전문가 정보 수정</h1>
          <p className="text-xl text-gray-600 mb-4">전문가 정보를 수정하세요.</p>
        </div>
        <div className="bg-white/90 backdrop-blur-xl rounded-3xl border border-gray-200/50 shadow-xl p-10 space-y-10">
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
                        <button type="button" onClick={() => removeArrayItem("specialties", sIdx)} className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-red-600 hover:text-red-700 hover:bg-red-50 rounded-xl border border-red-200 hover:border-red-300 transition-all duration-200">
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
          {/* 보유 기술 - 토글 방식으로 변경 필요 (register.jsx의 SkillToggleSelector 활용) */}
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
              className="w-full py-5 px-8 bg-gradient-to-r from-slate-700 via-gray-700 to-zinc-700 text-white font-bold text-xl rounded-2xl shadow-2xl hover:shadow-3xl hover:from-slate-800 hover:via-gray-800 hover:to-zinc-800 transform hover:scale-[1.02] transition-all duration-300 focus:outline-none focus:ring-4 focus:ring-slate-500/30">
              전문가 정보 수정 완료
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
