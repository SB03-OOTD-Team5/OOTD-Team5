import {create} from 'zustand';
import {type ProfileDto} from '@/lib/api/types';
import {getProfile} from '@/lib/api/users';
import {type BaseStore} from './types';
import {createBaseStoreActions} from "@/lib/stores/actions.ts";

interface ProfileStore extends BaseStore<ProfileDto, {userId: string}> {}

export const useProfileStore = create<ProfileStore>((set, get) => ({
  ...createBaseStoreActions({
    set, get,
    fetchApi: getProfile
  })
}));