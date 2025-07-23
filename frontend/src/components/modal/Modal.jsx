import { useRef, useEffect } from "react";
import { useModalStore } from "../../store/useModalStore.js";
import { Backdrop } from "./Backdrop.jsx";
import { createPortal } from "react-dom";

const portalElement = document.getElementById("modal");

export const Modal = () => {
  // 전역 모달 상태를 가져옴
  const { content, isOpen, close, position } = useModalStore();

  // 모달의 backdrop을 가리키는 ref
  const modalRef = useRef(null);

  // 모달이 열리면 스크롤 비활성화 (포지셔닝 모달은 스크롤 허용)
  useEffect(() => {
    if (isOpen && !position) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "unset";
    }
    return () => {
      document.body.style.overflow = "unset";
    };
  }, [isOpen, position]);

  // 모달 backdrop을 클릭한 경우 모달을 닫음
  const handleClickOutside = (e) => {
    if (modalRef.current === e.target) {
      close();
    }
  };

  // 모달이 열려있지 않거나 서버에서 실행되는 경우를 차단
  if (!isOpen || typeof window === "undefined") return null;

  // 버튼 기준 위치에 모달을 띄우는 경우 (position이 있으면)
  if (position) {
    return createPortal(
      <>
        <Backdrop onClick={close} />
        <div
          ref={modalRef}
          onClick={handleClickOutside}
          style={{
            position: "absolute",
            top: position.top,
            left: position.left,
            zIndex: 10000,
          }}>
          <div onClick={(e) => e.stopPropagation()}>{content}</div>
        </div>
      </>,
      portalElement
    );
  }

  // 기존 중앙 모달 (백드랍)
  return createPortal(
    <div ref={modalRef} onClick={handleClickOutside} className="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black">
      <div onClick={(e) => e.stopPropagation()}>{content}</div>
    </div>,
    portalElement
  );
};
