import {create} from 'zustand';
import {type FollowSummaryDto} from '@/lib/api/types';
import {getFollowSummary} from '@/lib/api/follows';
import {type BaseStore} from './types';
import {createBaseStoreActions} from "@/lib/stores/actions.ts";

interface FollowSummaryStore extends BaseStore<FollowSummaryDto, {userId: string}> {}

export const useFollowSummaryStore = create<FollowSummaryStore>((set, get) => ({
  ...createBaseStoreActions({
    set, get,
    fetchApi: getFollowSummary
  })
}));