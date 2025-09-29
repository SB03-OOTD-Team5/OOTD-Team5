import { create } from 'zustand';
import type { JwtDto } from '@/lib/api/types';
import {getCsrfToken, refreshToken, signIn, signOut} from '@/lib/api/auth';
import type { BaseStore } from './types';
import {execute} from "@/lib/stores/utils";
import {createBaseStoreActions} from "@/lib/stores/actions.ts";

interface AuthStore extends BaseStore<JwtDto, unknown> {
  signIn: (username: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
  isAuthenticated: () => boolean;
  getAccessToken: () => string | null;
}

export const useAuthStore = create<AuthStore>((set, get) => ({
  ...createBaseStoreActions({
    set, get,
    fetchApi: refreshToken,
  }),
  signIn: async (username: string, password: string) => {
    await execute(
        set, get,
        () => signIn({ username, password }),
        {
          shouldThrow: true
        }
    )
  },

  signOut: async () => {
    await execute(
        set, get,
        signOut,
        {
          onSuccess: (_result, _set, get) => {
            get().clear();
            getCsrfToken();
          },
        }
    )
  },

  isAuthenticated: () => {
    const { data } = get();
    return data?.accessToken != null;
  },

  getAccessToken: () => {
    const { data } = get();
    return data?.accessToken || null;
  },
}));