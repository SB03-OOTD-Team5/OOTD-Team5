import {create} from 'zustand';
import {type WeatherDto, type WeatherParams} from '@/lib/api/types';
import {getWeather} from '@/lib/api/weather';
import {type ListStore} from './types';
import {createListStoreActions} from "@/lib/stores/actions.ts";

interface WeatherStore extends ListStore<WeatherDto, WeatherParams> {
  selectedWeather?: WeatherDto;
  selectWeather: (weather: WeatherDto) => void;
  clearSelection: () => void;
}


export const useWeatherStore = create<WeatherStore>((set, get) => ({
  ...createListStoreActions({
    set, get,
    fetchApi: getWeather,
    initialData: {
      params: { longitude: 0, latitude: 0 },
    }
  }),

  selectedWeather: undefined,
  selectWeather: (weather: WeatherDto) => set({ selectedWeather: weather }),
  clearSelection: () => set({ selectedWeather: undefined })
}));