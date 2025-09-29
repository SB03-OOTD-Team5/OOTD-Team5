import {create} from 'zustand';
import type {DirectMessageDto, DirectMessageParams} from '@/lib/api/types';
import {getDms} from '@/lib/api/messages';
import {type PaginatedStore} from './types';
import {createPaginatedStoreActions} from "@/lib/stores/actions.ts";

interface DirectMessageStore extends PaginatedStore<DirectMessageDto, DirectMessageParams> {}

export const useDirectMessageStore = create<DirectMessageStore>((set, get) => ({
  ...createPaginatedStoreActions({
    set, get,
    fetchApi: getDms,
    initialData: {
      params: {
        cursor: undefined,
        idAfter: undefined,
        limit: 20,
        userId: '',
      }
    }
  }),
}));