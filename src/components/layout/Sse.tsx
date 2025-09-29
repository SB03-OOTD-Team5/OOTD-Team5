import { useSseStore } from "@/lib/stores/sseStore";
import {useAuthStore} from "@/lib/stores/useAuthStore.ts";
import {useEffect} from "react";

export default function Sse() {
  const { connect, disconnect } = useSseStore();
  const { isAuthenticated, getAccessToken } = useAuthStore();

  useEffect(() => {
    if (isAuthenticated()) {
      connect(getAccessToken() as string);
    }
    return () => {
      disconnect();
    };
  }, [isAuthenticated, getAccessToken, connect, disconnect]);

  return null;
}