"use client";

import { Toaster } from "react-hot-toast";

export default function CustomToast() {
  return (
    <Toaster
      toastOptions={{
        duration: 3000, // 모든 토스트 공통 지속시간

        style: {
          borderRadius: "4px",
          padding: "13px 30px",
          whiteSpace: "nowrap",
          maxWidth: "none", // 텍스트 크기에 따라 너비 자동
        },

        success: {
          style: {
            background: "#2c336c", // --color-tertiary
            color: "white",
            whiteSpace: "nowrap",
          },
          iconTheme: {
            primary: "white",
            secondary: "#2c336c", // --color-tertiary
          },
        },

        error: {
          style: {
            background: "#e20c3f", // --color-error
            color: "white",
            whiteSpace: "nowrap",
          },
          iconTheme: {
            primary: "white",
            secondary: "#e20c3f", // --color-error
          },
        },

        loading: {
          style: {
            background: "#9b9b9b", // --color-text-tertiary
            color: "white",
            whiteSpace: "nowrap",
          },
          iconTheme: {
            primary: "white",
            secondary: "#9b9b9b", // --color-text-tertiary
          },
        },
      }}
    />
  );
}
