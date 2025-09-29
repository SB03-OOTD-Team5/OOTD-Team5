import {create} from 'zustand';
import {type FollowDto, type FollowingListParam} from '@/lib/api/types';
import {getFollowings} from '@/lib/api/follows';
import {type PaginatedStore} from './types';
import {createPaginatedStoreActions} from "@/lib/stores/actions.ts";

interface FollowingStore extends PaginatedStore<FollowDto, FollowingListParam> {}

export const useFollowingStore = create<FollowingStore>((set, get) => ({
  ...createPaginatedStoreActions({
    set, get,
    fetchApi: getFollowings
  })
}));