import {EventSourcePolyfill} from 'event-source-polyfill';
import {create} from 'zustand';

interface SseState {
  eventSource: EventSource | null;
  isConnected: boolean;
  isConnecting: boolean;
  subscriptions: Map<string, (data: any) => void>;
  connect: (accessToken: string) => Promise<void>;
  disconnect: () => void;
  subscribe: (topic: string, callback: (data: any) => void) => void;
  unsubscribe: (topic: string) => void;
}

export const useSseStore = create<SseState>((set, get) => ({
  eventSource: null,
  isConnected: false,
  isConnecting: false,
  subscriptions: new Map(),

  connect: async (accessToken: string) => {
    const { isConnected, isConnecting } = get();
    if (isConnected || isConnecting) return;

    set({ isConnecting: true });

    try {
      console.log('[SSE] Try to connect');
      const BASE_URL = import.meta.env.VITE_PUBLIC_PATH || '';
      const eventSource = new EventSourcePolyfill(`${BASE_URL}/api/sse`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
        withCredentials: true,
      });

      eventSource.onopen = () => {
        set({ eventSource, isConnected: true, isConnecting: false });
        console.log('[SSE] Connected');
      };

      eventSource.onerror = (error) => {
        console.error('[SSE] Error:', { error, readyState: eventSource.readyState });
        set({
          isConnected: false,
          isConnecting: eventSource.readyState === EventSourcePolyfill.CONNECTING,
          eventSource: null,
        });
      };
      set({ eventSource, isConnected: true, isConnecting: false });
    } catch (error) {
      console.error('SSE 연결 시도 중 에러:', error);
      set({ isConnected: false, isConnecting: false, eventSource: null });
    }
  },

  disconnect: () => {
    const { eventSource, isConnected, subscriptions } = get();
    if (eventSource && isConnected) {
      // 모든 구독 해제
      subscriptions.forEach((callback, topic) => {
        eventSource.removeEventListener(topic, callback);
      });
      eventSource.close();
      set({ eventSource: null, isConnected: false });
    }
  },

  subscribe: (topic, callback) => {
    const { eventSource, isConnected, subscriptions } = get();

    if (subscriptions.has(topic)) {
      console.log('[SSE] Already subscribed', topic);
      return;
    }

    // 구독 정보 저장
    const wrappedCallback = (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data);
        callback(data);
      } catch (error) {
        console.error('[SSE] Message parsing error:', error);
        callback(event.data); // JSON 파싱 실패시 원본 데이터 전달
      }
    };

    if (eventSource && isConnected) {
      console.log('[SSE] Subscribed', topic);
      eventSource.addEventListener(topic, wrappedCallback);

      subscriptions.set(topic, wrappedCallback);
      set({ subscriptions });
    }
  },

  unsubscribe: (topic) => {
    const { eventSource, isConnected, subscriptions } = get();

    if (eventSource && isConnected) {
      const callback = subscriptions.get(topic);
      if (callback) {
        eventSource.removeEventListener(topic, callback);
      }
    }

    subscriptions.delete(topic);
    set({ subscriptions });
  },
}));
