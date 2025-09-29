import {create} from 'zustand';
import type {UserDto, UserListParams} from '@/lib/api/types';
import {getUserList} from '@/lib/api/users';
import type {PaginatedStore} from './types';
import {createPaginatedStoreActions} from "@/lib/stores/actions.ts";

interface UserStore extends PaginatedStore<UserDto, UserListParams> {}

export const useUserStore = create<UserStore>((set, get) => ({
  ...createPaginatedStoreActions({
    set, get,
    fetchApi: getUserList,
    initialData: {
      params: {
        cursor: undefined,
        idAfter: undefined,
        limit: 20,
        sortBy: 'createdAt',
        sortDirection: 'DESCENDING',
      }
    }
  })
}));