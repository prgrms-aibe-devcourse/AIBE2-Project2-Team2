import { create } from "zustand";

export const useUserInfoStore = create((set) => ({
  userInfo: null,
  setUserInfo: (info) => set({ userInfo: info }),
  clearUserInfo: () => set({ userInfo: null }),
}));

// Example userInfo object:
// {
//   nickname: "홍길동",
//   profileImageUrl: "https://cdn.example.com/profile.jpg",
//   email: "hong@example.com",
//   joinType: "KAKAO"
// }
