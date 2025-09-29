import {create} from 'zustand';
import type {CommentDto, FeedCommentParams} from '@/lib/api/types';
import {getFeedComments} from '@/lib/api/feeds';
import {type PaginatedStore} from './types';
import {createPaginatedStoreActions} from "@/lib/stores/actions.ts";


interface FeedCommentStore extends PaginatedStore<CommentDto, FeedCommentParams> {}

export const useFeedCommentStore = create<FeedCommentStore>((set, get) => ({
  ...createPaginatedStoreActions({
    set, get,
    fetchApi: getFeedComments,
    initialData: {
      params: {
        cursor: undefined,
        idAfter: undefined,
        limit: 20,
        feedId: '',
      },
    }
  }),
}));