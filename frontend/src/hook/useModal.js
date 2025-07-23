import { useModalStore } from "../store/useModalStore.js";

export function useModal() {
  const { open, close } = useModalStore();
  return {
    open,
    close,
  };
}
