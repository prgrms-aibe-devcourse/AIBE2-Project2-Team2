import { Link } from "react-router-dom";
import { useModal } from "../hook/useModal.js";
import { useRef } from "react";
import { useUserInfoStore } from "../store/userInfo.js";
import axiosInstance from "../lib/axios.js";

export default function Header() {
  const { userInfo } = useUserInfoStore();
  // 메뉴
  // '컨텐츠 등록하기'는 전문가만 노출
  const menuList = [...(userInfo?.role === "EXPERT" ? [{ name: "컨텐츠 등록하기", path: "/content/create", icon: "xi-file-add xi-x" }] : []), { name: "채팅", path: "/chat", icon: "xi-forum xi-x" }];

  const profileMenuList = [
    { name: "전문가 프로필 관리", path: "/expert/profile" },
    { name: "내 정보 관리", path: "/mypage/my-info" },
    { name: "결제 내역", path: "/mypage/pay-history" },
    { name: "매칭 내역", path: "/mypage/matching/history" },
    { name: "고객센터", path: "/customer-support" },
    { name: "전문가 등록", path: "/expert/register" },
  ];

  const profileMenuRef = useRef(null);
  const { open } = useModal();

  const handleProfileMenuClick = () => {
    if (profileMenuRef.current && profileMenuList.length > 0) {
      open(<ProfileModal profileMenuList={profileMenuList} />, profileMenuRef, {
        top: 8,
        left: 40,
      });
    }
  };

  return (
    <header className="bg-white fixed top-0 left-0 right-0 z-50 shadow-sm">
      <div className="mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          <div className="flex-1 md:flex md:items-center md:gap-12">
            <Link className="block text-teal-600" to="/">
              <span className="text-2xl font-bold">Logo</span>
            </Link>
          </div>

          <div className="md:flex md:items-center md:gap-12">
            <nav aria-label="Global" className="hidden md:block">
              <ul className="flex items-center gap-6 text-sm">
                {menuList.map((menu) => (
                  <li key={menu.name}>
                    <Link className="text-gray-500 transition hover:text-gray-500/75" to={menu.path}>
                      <i className={menu.icon}></i>
                    </Link>
                  </li>
                ))}
              </ul>
            </nav>

            {/*유저 정보가 있으면 로그인 버튼 대신 프로필 출력 */}
            {!userInfo?.nickname ? (
              <div className="flex items-center gap-4">
                <div className="sm:flex sm:gap-4">
                  <Link className="block rounded-md bg-teal-600 px-5 py-2.5 text-sm font-medium text-white transition hover:bg-teal-700" to="/auth/login">
                    로그인
                  </Link>

                  <Link className="hidden rounded-md bg-gray-100 px-5 py-2.5 text-sm font-medium text-teal-600 transition hover:text-teal-600/75 sm:block" to="/auth/signup">
                    회원가입
                  </Link>
                </div>
              </div>
            ) : (
              <div className="hidden md:relative md:block">
                {/* 프로필 사진 */}
                <button ref={profileMenuRef} onClick={handleProfileMenuClick} type="button" className="overflow-hidden rounded-full border border-gray-300 shadow-inner">
                  <span className="sr-only">Toggle dashboard menu</span>
                  <img src={userInfo?.profileImageUrl} alt="" className="size-10 object-cover" />
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}

function ProfileModal({ profileMenuList }) {
  const { userInfo, setUserInfo } = useUserInfoStore();

  const { close } = useModal();
  //로그아웃 api
  const handleLogout = async (e) => {
    e.preventDefault();
    try {
      await axiosInstance.post("/api/auth/logout");
      window.location.href = "/"; // 로그아웃 후 홈으로 리다이렉트
    } catch (error) {
      console.error("로그아웃 실패:", error);
    }
  };

  //전문가 일반 전환
  const handleExpertTransition = () => {
    setUserInfo({
      ...userInfo,
      roleStatus: userInfo.roleStatus === "전문가" ? "유저" : "전문가", // 전문가로 전환
    });
    // 전문가 전환 로직
    console.log("전문가 전환 클릭");
  };

  return (
    <div className="absolute end-0 z-10 mt-0.5 w-56 divide-y divide-gray-100 rounded-md border border-gray-100 bg-white shadow-lg" role="menu">
      {/*유저 이름등 정 */}
      <div className="p-4 bg-gray-50 rounded-t-md border-b border-gray-200">
        <div className="flex items-center justify-between">
          <div className="flex justify-center items-center gap-2">
            <span className="text-base font-bold text-gray-800">{userInfo?.nickname}</span>
            {/* <span className={`text-xs font-semibold px-2 py-0.5 rounded border ` + (userInfo?.roleStatus === "전문가" ? "bg-blue-100 text-blue-700 border-blue-200" : "bg-green-100 text-green-700 border-green-200")}>{userInfo?.roleStatus || "유저"}</span> */}
          </div>
          {/* {userInfo?.role === "EXPERT" && (
            <span onClick={handleExpertTransition} className="text-xs text-blue-600 hover:underline cursor-pointer">
              {userInfo?.roleStatus === "전문가" ? "유저" : "전문가"} 전환
            </span>
          )} */}
        </div>
      </div>
      <div className="p-2">
        {profileMenuList
          .filter((item) => {
            if (item.name === "전문가 프로필 관리") {
              return userInfo?.role === "EXPERT";
            }
            if (item.name === "전문가 등록") {
              return userInfo?.role !== "EXPERT";
            }
            return true;
          })
          .map((item) => (
            <Link onClick={close} key={item.name} to={item.path} className="block rounded-lg px-4 py-2 text-sm text-gray-500 hover:bg-gray-50 hover:text-gray-700" role="menuitem">
              {item.name}
            </Link>
          ))}
      </div>
      <div className="p-2">
        <form onSubmit={handleLogout}>
          <button type="submit" className="flex w-full items-center gap-2 rounded-lg px-4 py-2 text-sm text-red-700 hover:bg-red-50" role="menuitem">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor" className="size-4">
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 15 3 9m0 0 6-6M3 9h12a6 6 0 0 1 0 12h-3" />
            </svg>
            Logout
          </button>
        </form>
      </div>
    </div>
  );
}
