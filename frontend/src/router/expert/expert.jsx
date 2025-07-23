import { useState } from "react";

export default function ExpertForm() {
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

  return (
    <form className="max-w-lg mx-auto p-4 space-y-6">
      {/* Specialty */}
      <div>
        <label className="font-semibold">전문분야</label>
        {form.specialties.map((s, sIdx) => (
          <div key={sIdx} className="mb-2 border p-2 rounded">
            <input className="border px-2 py-1 rounded w-full mb-2" placeholder="전문분야 (예: 디자인)" value={s.specialty} onChange={(e) => handleArrayChange(e, sIdx, "specialty", "specialties")} />
            <div className="space-y-1">
              {s.detailFields.map((d, dIdx) => (
                <div key={dIdx} className="flex gap-2 mb-1">
                  <input className="border px-2 py-1 rounded w-full" placeholder="세부 분야 (예: 웹/모바일 디자인)" value={d} onChange={(e) => handleDetailFieldChange(e, sIdx, dIdx)} />
                  <button type="button" onClick={() => removeArrayItem("specialties", sIdx)} className="text-red-500">
                    삭제
                  </button>
                </div>
              ))}
              <button type="button" onClick={() => addArrayItem("specialties", { specialty: "", detailFields: [""] })} className="text-blue-500 text-xs">
                + 전문분야 추가
              </button>
              <button
                type="button"
                onClick={() =>
                  setForm((f) => {
                    const arr = [...f.specialties];
                    arr[sIdx].detailFields.push("");
                    return { ...f, specialties: arr };
                  })
                }
                className="text-blue-500 text-xs ml-2">
                + 세부분야 추가
              </button>
            </div>
          </div>
        ))}
      </div>
      {/* Introduction */}
      <div>
        <label className="font-semibold">자기소개</label>
        <textarea name="introduction" value={form.introduction} onChange={handleChange} className="border px-2 py-1 rounded w-full" rows={2} placeholder="자기소개를 입력하세요." />
      </div>
      {/* Region */}
      <div>
        <label className="font-semibold">지역</label>
        <input name="region" value={form.region} onChange={handleChange} className="border px-2 py-1 rounded w-full" placeholder="예: 서울특별시 강남구" />
      </div>
      {/* Career Years */}
      <div>
        <label className="font-semibold">총 경력(년)</label>
        <input name="totalCareerYears" value={form.totalCareerYears} onChange={handleChange} className="border px-2 py-1 rounded w-full" type="number" min={0} placeholder="예: 5" />
      </div>
      {/* Education */}
      <div>
        <label className="font-semibold">학력</label>
        <input name="education" value={form.education} onChange={handleChange} className="border px-2 py-1 rounded w-full" placeholder="예: 서울대학교 디자인학과" />
      </div>
      {/* Employee Count */}
      <div>
        <label className="font-semibold">직원 수</label>
        <input name="employeeCount" value={form.employeeCount} onChange={handleChange} className="border px-2 py-1 rounded w-full" type="number" min={0} placeholder="예: 10" />
      </div>
      {/* Website & SNS */}
      <div className="grid grid-cols-2 gap-2">
        <div>
          <label className="font-semibold">웹사이트</label>
          <input name="websiteUrl" value={form.websiteUrl} onChange={handleChange} className="border px-2 py-1 rounded w-full" placeholder="https://example.com" />
        </div>
        <div>
          <label className="font-semibold">Facebook</label>
          <input name="facebookUrl" value={form.facebookUrl} onChange={handleChange} className="border px-2 py-1 rounded w-full" placeholder="https://facebook.com/username" />
        </div>
        <div>
          <label className="font-semibold">Instagram</label>
          <input name="instagramUrl" value={form.instagramUrl} onChange={handleChange} className="border px-2 py-1 rounded w-full" placeholder="https://instagram.com/username" />
        </div>
        <div>
          <label className="font-semibold">X(구 트위터)</label>
          <input name="xUrl" value={form.xUrl} onChange={handleChange} className="border px-2 py-1 rounded w-full" placeholder="https://x.com/username" />
        </div>
      </div>
      {/* Skills */}
      <div>
        <label className="font-semibold">보유 기술</label>
        {form.skills.map((s, idx) => (
          <div key={idx} className="flex gap-2 mb-1">
            <input className="border px-2 py-1 rounded w-1/2" placeholder="카테고리 (예: IT/프로그래밍)" value={s.category} onChange={(e) => handleArrayChange(e, idx, "category", "skills")} />
            <input className="border px-2 py-1 rounded w-1/2" placeholder="기술명 (예: Java)" value={s.name} onChange={(e) => handleArrayChange(e, idx, "name", "skills")} />
            <button type="button" onClick={() => removeArrayItem("skills", idx)} className="text-red-500">
              삭제
            </button>
          </div>
        ))}
        <button type="button" onClick={() => addArrayItem("skills", { category: "", name: "" })} className="text-blue-500 text-xs">
          + 기술 추가
        </button>
      </div>
      {/* Careers */}
      <div>
        <label className="font-semibold">경력</label>
        {form.careers.map((c, idx) => (
          <div key={idx} className="flex gap-2 mb-1">
            <input className="border px-2 py-1 rounded w-full" placeholder="경력 내용 (예: ABC 회사에서 3년 근무)" value={c} onChange={(e) => handleArrayChange(e, idx, null, "careers")} />
            <button type="button" onClick={() => removeArrayItem("careers", idx)} className="text-red-500">
              삭제
            </button>
          </div>
        ))}
        <button type="button" onClick={() => addArrayItem("careers", "")} className="text-blue-500 text-xs">
          + 경력 추가
        </button>
      </div>
      <button type="submit" className="w-full py-2 rounded bg-blue-600 text-white font-semibold mt-4">
        저장
      </button>
    </form>
  );
}
