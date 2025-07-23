import { create } from "zustand";

export const useModalStore = create((set) => ({
  content: null,
  isOpen: false,
  position: null,
  open: (content, ref, position = null) => {
    let finalPosition = null;
    if (ref?.current) {
      const rect = ref.current.getBoundingClientRect();
      finalPosition = {
        top: rect.top + window.scrollY + rect.height + (position?.top ?? 0),
        left: rect.left + window.scrollX + (position?.left ?? 0),
      };
    } else if (position) {
      finalPosition = position;
    }
    set({ isOpen: true, content, position: finalPosition });
  },
  close: () => set({ isOpen: false, content: null, position: null }),
}));
