import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from '../../lib/axios';
import { useUserInfoStore } from '../../store/userInfo';
import toast from "react-hot-toast";

function PaymentPage() {
  const { id } = useParams();
  const [content, setContent] = useState(null);
  // multipleChoice: { 'qIdx-oIdx': true/false }, !multipleChoice: { 'qIdx': oIdx }
  const [selectedOptions, setSelectedOptions] = useState({});
  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(false);
  const [payResult, setPayResult] = useState(null); // 결제 결과 안내
  const userInfo = useUserInfoStore(state => state.userInfo);

  useEffect(() => {
    if (!id) return;
    async function fetchData() {
      setLoading(true);
      try {
        const contentRes = await axios.get(`/api/content/${id}`);
        setContent(contentRes.data);
      } catch (err) {
        console.error("API 요청 에러:", err);
      }
      setLoading(false);
    }
    fetchData();
  }, [id]);

  // 옵션 선택 핸들러 (모든 UI는 체크박스, multipleChoice=false면 한 개만 체크 가능)
  const handleOptionSelect = (qIdx, oIdx, q) => {
    const isMulti = !!(q.multipleChoice ?? q.isMultipleChoice ?? q.is_multiple_choice);
    if (isMulti) {
      const key = `${qIdx}-${oIdx}`;
      setSelectedOptions((prev) => ({ ...prev, [key]: !prev[key] }));
    } else {
      if (selectedOptions[qIdx] === oIdx) {
        setSelectedOptions((prev) => {
          const next = { ...prev };
          delete next[qIdx];
          return next;
        });
      } else {
        setSelectedOptions((prev) => ({ ...prev, [qIdx]: oIdx }));
      }
    }
  };

  // 금액 계산
  const basePrice = content?.budget || 0;
  let extraPrice = 0;
  let selectedOptionIds = [];
  if (content?.questions) {
    content.questions.forEach((q, qIdx) => {
      if (q.options) {
        const isMulti = !!(q.multipleChoice ?? q.isMultipleChoice ?? q.is_multiple_choice);
        if (isMulti) {
          q.options.forEach((opt, oIdx) => {
            const key = `${qIdx}-${oIdx}`;
            if (selectedOptions[key]) {
              extraPrice += opt.additionalPrice || 0;
              selectedOptionIds.push(opt.optionId);
            }
          });
        } else {
          const selectedIdx = selectedOptions[qIdx];
          if (selectedIdx !== undefined && q.options[selectedIdx]) {
            const opt = q.options[selectedIdx];
            extraPrice += opt.additionalPrice || 0;
            selectedOptionIds.push(opt.optionId);
          }
        }
      }
    });
  }
  const totalPrice = basePrice + extraPrice;

  // 결제하기 버튼 클릭 핸들러
  const handlePay = async () => {
    setPaying(true);
    setPayResult(null);
    try {
      // 1. 매칭 생성 (팀원이 만든 API 사용)
      const matchingRequestDto = {
        memberId: userInfo?.memberId, // 실제 로그인 유저 ID 사용
        contentId: parseInt(id),
        items: selectedOptionIds.map(optionId => {
          // 선택된 옵션 정보를 찾아서 items 배열로 변환
          let optionInfo = null;
          content.questions.forEach(q => {
            q.options.forEach(opt => {
              if (opt.optionId === optionId) {
                optionInfo = {
                  name: opt.optionText,
                  price: opt.additionalPrice || 0
                };
              }
            });
          });
          return optionInfo;
        }).filter(item => item !== null)
      };

      // 기본 항목도 추가 (콘텐츠 기본 예산)
      if (content.budget > 0) {
        matchingRequestDto.items.unshift({
          name: content.title,
          price: content.budget
        });
      }

      const matchingRes = await axios.post('/api/matchings', matchingRequestDto);
      const matchingId = matchingRes.data.matchingId;
      if (!matchingId) throw new Error("매칭 생성 실패");

      // 2. 카카오페이 결제 준비 API 호출
      const readyRes = await axios.post(`/api/payment/kakao/ready`, null, {
        params: {
          matchingId: matchingId,
          userId: "user", // 실제 서비스에서는 로그인 유저 ID로 대체
        },
      });
      let kakaoRes;
      try {
        kakaoRes = typeof readyRes.data === "string" ? JSON.parse(readyRes.data) : readyRes.data;
      } catch {
        toast.error("카카오페이 결제 준비 실패");
        setPaying(false);
        return;
      }
      if (kakaoRes.next_redirect_pc_url) {
        window.location.href = kakaoRes.next_redirect_pc_url;
      } else {
        toast.error("카카오페이 결제 URL이 없습니다.");
      }
    } catch (err) {
      setPayResult({ success: false, error: err });
      toast.error('매칭 생성 또는 카카오페이 준비에 실패했습니다.\n' + (err?.response?.data?.message || err.message));
      setPaying(false);
    }
  };

  if (loading || !content) {
    return <div className="flex justify-center items-center h-96">로딩 중...</div>;
  }

  return (
    <div className="w-full min-h-screen bg-gray-50 flex flex-col items-center">
      <div className="w-full max-w-6xl mt-8">
        <h2 className="text-2xl font-bold mb-6">결제하기</h2>
        <div className="flex gap-8">
          {/* 주문 내역 */}
          <div className="flex-1 bg-white rounded-xl shadow p-8">
            <div className="flex items-center gap-6 mb-6">
              <img src={content.contentUrl} alt="썸네일" className="w-28 h-28 object-cover rounded-lg border" />
              <div>
                <div className="flex items-center gap-2 mb-1">
              
                  <span className="font-bold text-lg">{content.title}</span>
                </div>
                <div className="text-xs text-gray-400">{content.expertNickname || '-'}</div>
              </div>
            </div>
            <div className="border-b my-4" />
            {/* 기본항목 */}
            <div className="mb-6">
              <div className="font-bold mb-4 text-2xl">기본항목</div>
              <div className="flex items-center gap-2 mb-2">
                <span className="font-semibold text-2xl">{content.title}</span>            
                <span className="font-bold ml-4">{(content.budget || 0).toLocaleString()}원</span>
              </div>
            </div>
            {/* 옵션항목 */}
            <div className="mb-6">
              <div className="font-bold mb-4 text-2xl">옵션항목</div>
              {content.questions && content.questions.length > 0 ? (
                <ul className="space-y-2">
                  {content.questions.map((q, qIdx) => (
                    <li key={qIdx} className="mb-2">
                      <div className="font-bold text-xl mb-2">{q.questionText}</div>
                      {q.options && q.options.length > 0 && (
                        <ul className="ml-2 text-lg text-gray-600">
                          {q.options.map((opt, oIdx) => {
                            const key = `${qIdx}-${oIdx}`;
                            const isMulti = !!(q.multipleChoice ?? q.isMultipleChoice ?? q.is_multiple_choice);
                            let checked;
                            let disabled = false;
                            if (isMulti) {
                              checked = !!selectedOptions[key];
                            } else {
                              checked = selectedOptions[qIdx] === oIdx;
                              if (selectedOptions[qIdx] !== undefined && selectedOptions[qIdx] !== oIdx) {
                                disabled = true;
                              }
                            }
                            return (
                              <li key={oIdx} className="flex items-center gap-2 mb-1">
                                <input
                                  type="checkbox"
                                  id={`option-${qIdx}-${oIdx}`}
                                  checked={checked}
                                  disabled={disabled}
                                  onChange={() => handleOptionSelect(qIdx, oIdx, q)}
                                  className="accent-yellow-400"
                                />
                                <label htmlFor={`option-${qIdx}-${oIdx}`} className="text-lg cursor-pointer select-none">
                                  {opt.optionText}
                                </label>
                                <span className="text-blue-500 font-semibold ml-2">+{opt.additionalPrice.toLocaleString()}원</span>
                              </li>
                            );
                          })}
                        </ul>
                      )}
                    </li>
                  ))}
                </ul>
              ) : (
                <div className="text-gray-400 text-sm">옵션 없음</div>
              )}
            </div>
          </div>
          {/* 우측 결제 요약 */}
          <aside className="w-full max-w-xs flex-shrink-0 bg-white rounded-xl shadow p-8 mt-2">
            <div className="mb-4">
              <div className="flex justify-between mb-2">
                <span className="text-gray-500">주문 금액</span>
                <span className="font-bold">{basePrice.toLocaleString()}원</span>
              </div>
              <div className="flex justify-between mb-2">
                <span className="text-gray-500">추가 옵션 금액</span>
                <span className="font-bold">{extraPrice.toLocaleString()}원</span>
              </div>
              <div className="border-b my-2" />
              <div className="flex justify-between text-lg font-bold">
                <span>총 결제 금액</span>
                <span className="text-yellow-500">{totalPrice.toLocaleString()}원</span>
              </div>
              <div className="text-xs text-gray-400 mt-2">* VAT 포함</div>
            </div>
            <button className={`w-full bg-yellow-400 py-3 rounded font-bold cursor-pointer hover:bg-yellow-500 transition-colors ${paying ? "opacity-60 cursor-not-allowed" : ""}`} onClick={handlePay} disabled={paying}>
              {paying ? "결제 중..." : "결제하기"}
            </button>
            {payResult && <div className={`mt-4 text-center text-sm ${payResult.success ? "text-green-600" : "text-red-500"}`}>{payResult.success ? "결제 내역이 저장되었습니다." : "결제 저장 실패"}</div>}
          </aside>
        </div>
      </div>
    </div>
  );
}

export default PaymentPage;
