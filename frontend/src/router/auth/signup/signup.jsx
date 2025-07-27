import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../../../lib/axios.js";
import toast from "react-hot-toast";

export default function SignUp() {
  const [userForm, setUserForm] = useState({
    nickname: "",
    email: "",
    phone: "",
    password: "",
    passwordCheck: "",
  });

  const [msg, setMsg] = useState({
    nickname: "",
    email: "",
    phone: "",
    password: "",
    passwordCheck: "",
  });

  const navigate = useNavigate();

  // 핸드폰 번호 자동 하이픈
  const handlePhoneChange = (e) => {
    let value = e.target.value.replace(/[^0-9]/g, "");
    if (value.length > 11) value = value.slice(0, 11);
    let formatted = value;
    if (value.length > 3 && value.length <= 7) {
      formatted = value.slice(0, 3) + "-" + value.slice(3);
    } else if (value.length > 7) {
      formatted = value.slice(0, 3) + "-" + value.slice(3, 7) + "-" + value.slice(7);
    }
    setUserForm((prev) => ({ ...prev, phone: formatted }));
    setMsg((prev) => ({ ...prev, phone: formatted.length === 13 ? "" : "핸드폰 번호를 정확히 입력해 주세요." }));
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setUserForm((prev) => ({ ...prev, [name]: value }));
    setMsg((prev) => ({ ...prev, [name]: "" }));
  };

  //   {
  //   "nickname": "user123",
  //   "password": "Passwor23",
  //   "phone": "010-1234-5678",
  //   "email": "example@aver.com",
  //   "joinType": "NORMAL"
  // }

  const sendSignUpRequest = async (userData) => {
    const sendUserData = {
      nickname: userData.nickname,
      password: userData.password,
      phone: userData.phone,
      email: userData.email,
      joinType: "NORMAL",
    };

    try {
      const response = await axiosInstance.post("/api/auth/signup", sendUserData);
      console.log(response.data);
      toast.success("회원가입이 완료되었습니다!");
      navigate("/auth/login");
    } catch (error) {
      console.error("회원가입 실패:", error);
      toast.error(error.response.data.message || "회원가입에 실패했습니다.");
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!userForm.nickname) {
      setMsg((prev) => ({ ...prev, nickname: "닉네임을 입력해 주세요." }));
      return;
    }
    if (!userForm.email.match(/^[^@\s]+@[^@\s]+\.[^@\s]+$/)) {
      setMsg((prev) => ({ ...prev, email: "이메일 주소를 입력해 주세요." }));
      return;
    }
    if (userForm.phone.length !== 13) {
      setMsg((prev) => ({ ...prev, phone: "핸드폰 번호를 정확히 입력해 주세요." }));
      return;
    }
    if (userForm.password.length < 8) {
      setMsg((prev) => ({ ...prev, password: "비밀번호는 8자 이상이어야 합니다." }));
      return;
    }

    if (!userForm.password.match(/[a-zA-Z]/)) {
      setMsg((prev) => ({ ...prev, password: "영어를 포함해야 합니다." }));
      return;
    }
    if (!userForm.password.match(/[0-9]/)) {
      setMsg((prev) => ({ ...prev, password: "숫자(0-9)를 포함해야 합니다." }));
      return;
    }
    if (!userForm.password.match(/[!@#$%^&*]/)) {
      setMsg((prev) => ({ ...prev, password: "특수문자(!@#$%^&*)를 포함해야 합니다." }));
      return;
    }
    if (userForm.password !== userForm.passwordCheck) {
      setMsg((prev) => ({ ...prev, passwordCheck: "비밀번호가 일치하지 않습니다." }));
      return;
    }
    // 회원가입 처리 로직
    sendSignUpRequest(userForm);
  };

  return (
    <div className="mt-10 w-full max-w-[300px]">
      <form onSubmit={handleSubmit}>
        <label className="block mb-2 font-semibold">닉네임</label>
        <input type="text" name="nickname" value={userForm.nickname} onChange={handleChange} placeholder="닉네임을 입력해 주세요." className={`w-full px-3 py-2 border rounded focus:outline-none focus:ring ${msg.nickname ? "border-red-500" : "border-gray-300"}`} />
        {msg.nickname && <p className="text-red-500 text-sm mt-1">{msg.nickname}</p>}

        <label className="block mt-6 mb-2 font-semibold">이메일</label>
        <input type="email" name="email" value={userForm.email} onChange={handleChange} placeholder="이메일을 입력해 주세요." className={`w-full px-3 py-2 border rounded focus:outline-none focus:ring ${msg.email ? "border-red-500" : "border-gray-300"}`} />
        {msg.email && <p className="text-red-500 text-sm mt-1">{msg.email}</p>}

        <label className="block mt-6 mb-2 font-semibold">핸드폰 번호</label>
        <input type="text" name="phone" value={userForm.phone} onChange={handlePhoneChange} placeholder="전화번호를 입력해 주세요." maxLength={13} className={`w-full px-3 py-2 border rounded focus:outline-none focus:ring ${msg.phone ? "border-red-500" : "border-gray-300"}`} />
        {msg.phone && <p className="text-red-500 text-sm mt-1">{msg.phone}</p>}

        <label className="block mt-6 mb-2 font-semibold">비밀번호</label>
        <input type="password" name="password" value={userForm.password} onChange={handleChange} placeholder="비밀번호를 입력해 주세요." className={`w-full px-3 py-2 border rounded focus:outline-none focus:ring ${msg.password ? "border-red-500" : "border-gray-300"}`} />
        {msg.password && <p className="text-red-500 text-sm mt-1">{msg.password}</p>}

        <label className="block mt-6 mb-2 font-semibold">비밀번호 확인</label>
        <input type="password" name="passwordCheck" value={userForm.passwordCheck} onChange={handleChange} placeholder="비밀번호를 다시 입력해 주세요." className={`w-full px-3 py-2 border rounded focus:outline-none focus:ring ${msg.passwordCheck ? "border-red-500" : "border-gray-300"}`} />
        {msg.passwordCheck && <p className="text-red-500 text-sm mt-1">{msg.passwordCheck}</p>}

        <button type="submit" className="w-full mt-6 py-2 rounded bg-[#6C4EFF] text-white font-semibold text-lg hover:bg-[#5a3fd6]">
          회원가입
        </button>
      </form>
    </div>
  );
}
