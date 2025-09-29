import {create} from 'zustand';
import type {FeedDto, FeedListParams} from '@/lib/api/types';
import {getFeedList} from '@/lib/api/feeds';
import {type PaginatedStore} from './types';
import {createPaginatedStoreActions} from "@/lib/stores/actions.ts";

interface FeedStore extends PaginatedStore<FeedDto, FeedListParams> {}

export const useFeedStore = create<FeedStore>((set, get) => ({
  ...createPaginatedStoreActions({
    set, get,
    fetchApi: getFeedList,
    initialData: {
      params: {
        cursor: undefined,
        idAfter: undefined,
        limit: 10,
        sortBy: 'createdAt',
        sortDirection: 'DESCENDING',
      }
    }
  }),
}));