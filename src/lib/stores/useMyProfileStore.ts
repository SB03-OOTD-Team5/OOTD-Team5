import {create} from 'zustand';
import {type ProfileDto} from '@/lib/api/types';
import {getProfile} from '@/lib/api/users';
import {type BaseStore} from './types';
import {createBaseStoreActions} from "@/lib/stores/actions.ts";

interface MyProfileStore extends BaseStore<ProfileDto, {userId: string}> {}

export const useMyProfileStore = create<MyProfileStore>((set, get) => ({
  ...createBaseStoreActions({
    set, get,
    fetchApi: getProfile
  })
}));