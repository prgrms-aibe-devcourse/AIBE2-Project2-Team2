import { useState, useEffect } from "react";
import { Trash2, User, MapPin, GraduationCap, Users, Globe, Facebook, Instagram, Twitter, Briefcase, Code } from "lucide-react";
import { InputGroup, ModernInput, ModernSelect, ModernTextarea, AddButton, DeleteButton } from "./FormFields";
import axiosInstance from "../../../lib/axios";
import { useNavigate } from "react-router-dom";
import { useUserInfoStore } from "../../../store/userInfo";

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
    skills: [{ category: "", name: "" }],
    careers: [""],
  });
  const [meta, setMeta] = useState({ detailFields: [], skills: [], regions: [] });
  const { userInfo, setUserInfo } = useUserInfoStore(); // Assuming useUserInfoStore is imported from your Zustand store

  const navigate = useNavigate();

  useEffect(() => {
    axiosInstance.get("/api/expert/meta").then((res) => setMeta(res.data));
  }, []);

  // 공통 핸들러
  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  // specialties, skills, careers 배열 핸들러
  const handleArrayChange = (e, idx, key, arrName) => {
    const value = e.target.value;
    setForm((prev) => {
      const arr = [...prev[arrName]];
      if (key) arr[idx][key] = value;
      else arr[idx] = value;
      return { ...prev, [arrName]: arr };
    });
  };

  // detailFields 핸들러
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

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axiosInstance.post("/api/expert/upgrade", form);
      console.log("전문가 등록 성공:", response.data);
      alert("전문가 등록이 완료되었습니다.");
      setUserInfo({ ...userInfo, role: "EXPERT" }); // Update user info in Zustand store
      navigate("/");
    } catch (error) {
      console.error("전문가 등록 실패:", error);
      alert("전문가 등록에 실패했습니다. 다시 시도해 주세요.");
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br py-8 px-4">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">전문가 등록</h1>
          <p className="text-gray-600 mt-2">전문분야를 입력해서 전문가로 전환해보세요!</p>
        </div>

        <div className="bg-white/60 backdrop-blur-lg rounded-2xl border border-white/50 shadow-xl p-8 space-y-8">
          {/* Specialty Section */}
          <InputGroup label="전문분야" icon={Briefcase}>
            <div className="space-y-4">
              {form.specialties.map((s, sIdx) => (
                <div key={sIdx} className="bg-gray-50/80 backdrop-blur-sm rounded-xl p-6 border border-gray-100">
                  <ModernSelect value={s.specialty} onChange={(e) => handleArrayChange(e, sIdx, "specialty", "specialties")} className="mb-4">
                    <option value="">전문분야 선택</option>
                    {meta.detailFields.map((cat) => (
                      <option key={cat.specialty} value={cat.specialty}>
                        {cat.specialty}
                      </option>
                    ))}
                  </ModernSelect>

                  <div className="space-y-3">
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
                        />
                      </div>
                    ))}

                    <div className="flex gap-2 pt-2">
                      <AddButton
                        onClick={() => {
                          const arr = [...form.specialties];
                          arr[sIdx].detailFields.push("");
                          setForm((f) => ({ ...f, specialties: arr }));
                        }}>
                        세부분야 추가
                      </AddButton>

                      {form.specialties.length > 1 && (
                        <button
                          type="button"
                          onClick={() => removeArrayItem("specialties", sIdx)}
                          className="flex items-center gap-2 px-3 py-2 text-sm font-medium text-red-600 
                            hover:text-red-700 hover:bg-red-50 rounded-lg transition-all duration-200">
                          <Trash2 size={14} />
                          전문분야 삭제
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))}

              <AddButton onClick={() => addArrayItem("specialties", { specialty: "", detailFields: [""] })}>전문분야 추가</AddButton>
            </div>
          </InputGroup>

          {/* Introduction */}
          <InputGroup label="자기소개" icon={User}>
            <ModernTextarea name="introduction" value={form.introduction} onChange={handleChange} rows={4} placeholder="전문성과 경험을 간략히 소개해주세요..." />
          </InputGroup>

          {/* Basic Info Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <InputGroup label="지역" icon={MapPin}>
              <ModernSelect name="region" value={form.region} onChange={handleChange}>
                <option value="">지역 선택</option>
                {meta.regions.map((r) => (
                  <option key={r} value={r}>
                    {r}
                  </option>
                ))}
              </ModernSelect>
            </InputGroup>

            <InputGroup label="총 경력" icon={Briefcase}>
              <ModernInput name="totalCareerYears" value={form.totalCareerYears} onChange={handleChange} type="number" min={0} placeholder="5" />
            </InputGroup>

            <InputGroup label="학력" icon={GraduationCap}>
              <ModernInput name="education" value={form.education} onChange={handleChange} placeholder="서울대학교 디자인학과" />
            </InputGroup>

            <InputGroup label="직원 수" icon={Users}>
              <ModernInput name="employeeCount" value={form.employeeCount} onChange={handleChange} type="number" min={0} placeholder="10" />
            </InputGroup>
          </div>

          {/* Social Links */}
          <InputGroup label="웹사이트 & 소셜미디어">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <Globe size={14} />
                  웹사이트
                </div>
                <ModernInput name="websiteUrl" value={form.websiteUrl} onChange={handleChange} placeholder="https://example.com" />
              </div>

              <div className="space-y-2">
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <Facebook size={14} />
                  Facebook
                </div>
                <ModernInput name="facebookUrl" value={form.facebookUrl} onChange={handleChange} placeholder="https://facebook.com/username" />
              </div>

              <div className="space-y-2">
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <Instagram size={14} />
                  Instagram
                </div>
                <ModernInput name="instagramUrl" value={form.instagramUrl} onChange={handleChange} placeholder="https://instagram.com/username" />
              </div>

              <div className="space-y-2">
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <Twitter size={14} />X (구 트위터)
                </div>
                <ModernInput name="xUrl" value={form.xUrl} onChange={handleChange} placeholder="https://x.com/username" />
              </div>
            </div>
          </InputGroup>

          {/* Skills */}
          <InputGroup label="보유 기술" icon={Code}>
            <div className="space-y-3">
              {form.skills.map((s, idx) => (
                <div key={idx} className="flex gap-3 items-center p-4 bg-gray-50/80 rounded-xl">
                  <ModernSelect value={s.category} onChange={(e) => handleArrayChange(e, idx, "category", "skills")} className="flex-1">
                    <option value="">카테고리 선택</option>
                    {meta.skills.map((cat) => (
                      <option key={cat.categoryName} value={cat.categoryName}>
                        {cat.categoryName}
                      </option>
                    ))}
                  </ModernSelect>

                  <ModernSelect value={s.name} onChange={(e) => handleArrayChange(e, idx, "name", "skills")} className="flex-1">
                    <option value="">기술 선택</option>
                    {(meta.skills.find((f) => f.categoryName === s.category)?.skills || []).map((skill) => (
                      <option key={skill} value={skill}>
                        {skill}
                      </option>
                    ))}
                  </ModernSelect>

                  <DeleteButton onClick={() => removeArrayItem("skills", idx)} />
                </div>
              ))}

              <AddButton onClick={() => addArrayItem("skills", { category: "", name: "" })}>기술 추가</AddButton>
            </div>
          </InputGroup>

          {/* Careers */}
          <InputGroup label="경력사항" icon={Briefcase}>
            <div className="space-y-3">
              {form.careers.map((c, idx) => (
                <div key={idx} className="flex gap-3 items-center">
                  <ModernInput placeholder="경력 내용 (예: ABC 회사에서 3년 근무)" value={c} onChange={(e) => handleArrayChange(e, idx, null, "careers")} className="flex-1" />
                  <DeleteButton onClick={() => removeArrayItem("careers", idx)} />
                </div>
              ))}

              <AddButton onClick={() => addArrayItem("careers", "")}>경력 추가</AddButton>
            </div>
          </InputGroup>

          {/* Submit Button */}
          <button
            onClick={handleSubmit}
            type="submit"
            className="w-full py-4 px-6 bg-gradient-to-r from-indigo-600 to-purple-600 
              text-white font-semibold rounded-xl shadow-lg hover:shadow-xl 
              hover:from-indigo-700 hover:to-purple-700 transform hover:scale-[1.02] 
              transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-indigo-500/40">
            전문가 등록 완료
          </button>
        </div>
      </div>
    </div>
  );
}
