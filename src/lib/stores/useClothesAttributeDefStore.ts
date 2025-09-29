import {create} from 'zustand';
import type {ClothesAttributeDefDto, ClothesAttributeDefListParams} from '@/lib/api/types';
import {getClothesAttributeDef} from '@/lib/api/clothes-attributes';
import {type ListStore} from './types';
import {createListStoreActions} from "@/lib/stores/actions.ts";

interface ClothesAttributeDefStore extends ListStore<ClothesAttributeDefDto, ClothesAttributeDefListParams>{}

export const useClothesAttributeDefStore = create<ClothesAttributeDefStore>((set, get) => ({
  ...createListStoreActions({
    set, get,
    fetchApi: getClothesAttributeDef,
    initialData: {
      params: {
        sortBy: 'name',
        sortDirection: 'ASCENDING',
        keywordLike: undefined
      }
    }
  }),
}));