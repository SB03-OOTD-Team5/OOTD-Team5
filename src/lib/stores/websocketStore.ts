import { Client, type StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { create } from 'zustand';

interface WebSocketState {
  stompClient: Client | null;
  isConnected: boolean;
  isConnecting: boolean;
  subscriptions: Map<string, StompSubscription>;
  connect: (accessToken: string) => Promise<void>;
  disconnect: () => void;
  subscribe: (destination: string, callback: (message: any) => void) => void;
  unsubscribe: (destination: string) => void;
  send: (destination: string, body: any) => void;
}

export const useWebSocketStore = create<WebSocketState>((set, get) => ({
  stompClient: null,
  isConnected: false,
  isConnecting: false,
  subscriptions: new Map(),

  connect: async (accessToken: string) => {
    const { isConnected, isConnecting } = get();
    if (isConnected || isConnecting) return;

    set({ isConnecting: true });

    const BASE_URL = import.meta.env.VITE_PUBLIC_PATH || '';
    const socket = new SockJS(`${BASE_URL}/ws`);
    const client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      debug: (str) => {
        console.log(str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      set({ stompClient: client, isConnected: true, isConnecting: false });
      console.log('WebSocket 연결 성공');
    };

    client.onStompError = (frame) => {
      console.error('STOMP 에러:', frame);
      set({ isConnected: false, stompClient: null, isConnecting: false });
    };

    client.onWebSocketClose = () => {
      set({ isConnected: false, stompClient: null, isConnecting: false });
    };

    client.activate();
  },

  disconnect: () => {
    const { stompClient, isConnected } = get();
    if (stompClient && isConnected) {
      stompClient.deactivate();
      set({ stompClient: null, isConnected: false, subscriptions: new Map() });
    }
  },

  subscribe: (destination: string, callback: (message: any) => void) => {
    const { stompClient, isConnected, subscriptions } = get();

    if (subscriptions.has(destination) || !isConnected || stompClient == null) {
      return;
    }

    const subscription: StompSubscription = stompClient.subscribe(destination, (message) => {
      const payload = JSON.parse(message.body);
      callback(payload);
    });

    subscriptions.set(destination, subscription);
  },

  unsubscribe: (destination: string) => {
    const { stompClient, isConnected, subscriptions } = get();

    if (!subscriptions.has(destination) || !isConnected || stompClient == null) {
      return;
    }

    const subscription = subscriptions.get(destination);
    if (subscription) {
      subscription.unsubscribe();
      subscriptions.delete(destination);
      set({ subscriptions });
    }
  },

  send: (destination: string, body: any) => {
    const { stompClient, isConnected } = get();

    if (stompClient && isConnected) {
      stompClient.publish({
        destination,
        body: JSON.stringify(body),
      });
    } else {
      console.error('WebSocket이 연결되어 있지 않습니다.');
    }
  },
}));
