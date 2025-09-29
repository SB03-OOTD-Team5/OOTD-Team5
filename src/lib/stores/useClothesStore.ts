import {create} from 'zustand';
import type {ClothesDto, ClothesListParams} from '@/lib/api/types';
import {getClothes} from '@/lib/api/clothes';
import {type PaginatedStore} from './types';
import {createPaginatedStoreActions} from "@/lib/stores/actions.ts";

interface ClothesStore extends PaginatedStore<ClothesDto, ClothesListParams> {
  isEmpty: () => boolean;
}

export const useClothesStore = create<ClothesStore>((set, get) => ({
  ...createPaginatedStoreActions({
    set, get,
    fetchApi: getClothes,
    initialData: {
      params: {
        ownerId: '',
        typeEqual: undefined,
        limit: 20
      }
    }
  }),

  isEmpty: () => {
    const {data, params} = get();
    return params.typeEqual == undefined && data.length === 0;
  },
}));