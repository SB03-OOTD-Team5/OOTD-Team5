import { useWebSocketStore } from "@/lib/stores/websocketStore";
import {useAuthStore} from "@/lib/stores/useAuthStore.ts";
import {useEffect} from "react";

interface WebSocketProps {
}

export default function WebSocket({ }: WebSocketProps) {
  const { connect, disconnect } = useWebSocketStore();
  const { isAuthenticated, getAccessToken } = useAuthStore();

  useEffect(() => {
    if (isAuthenticated()) {
      connect(getAccessToken() as string);
    }
    return () => {
      disconnect();
    };
  }, [connect, disconnect, isAuthenticated, getAccessToken]);

  return null;
}