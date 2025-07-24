import { create } from "zustand";

export const useUserInfoStore = create((set) => ({
  userInfo: null,
  setUserInfo: (info) => set({ userInfo: info }),
  clearUserInfo: () => set({ userInfo: null }),
}));

// Example userInfo object:
// {
//  "nickname": "홍길동",
//    "email": "example123@naver.com",
//    "role": "USER",
//    "profileImageUrl": "https://example.com/profile.jpg"
// }
