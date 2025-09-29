import {create} from 'zustand';
import {type FollowDto, type FollowerListParam} from '@/lib/api/types';
import {getFollowers} from '@/lib/api/follows';
import {type PaginatedStore} from './types';
import {createPaginatedStoreActions} from "@/lib/stores/actions.ts";

interface FollowerStore extends PaginatedStore<FollowDto, FollowerListParam> {}

export const useFollowerStore = create<FollowerStore>((set, get) => ({
  ...createPaginatedStoreActions({
    set, get,
    fetchApi: getFollowers
  })
}));