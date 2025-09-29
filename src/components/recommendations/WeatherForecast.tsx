import {type SkyStatus, type WeatherDto} from '@/lib/api/types';
import {useWeatherStore} from '@/lib/stores/useWeatherStore';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';

import sunnyIcon from '@/assets/illust_logos/il_Sunny.svg';
import overcastIcon from '@/assets/illust_logos/il_Overcast.svg';
import cloudyIcon from '@/assets/illust_logos/il_cloudy.svg';
import {useEffect} from "react";

// 날씨 상태를 한국어로 변환하는 함수
function getSkyStatusText(skyStatus: SkyStatus): string {
  switch (skyStatus) {
    case 'CLEAR':
      return '맑음';
    case 'MOSTLY_CLOUDY':
      return '구름많음';
    case 'CLOUDY':
      return '흐림';
    default:
      return '맑음';
  }
}

function WeatherIcon({ skyStatus }: { skyStatus: SkyStatus }) {
  switch (skyStatus) {
    case 'CLEAR':
      return (
        <div className="overflow-clip relative shrink-0 size-10">
          <img alt="맑음" className="block max-w-none size-full" src={sunnyIcon} />
        </div>
      );
    case 'MOSTLY_CLOUDY':
      return (
        <div className="overflow-clip relative shrink-0 size-10">
          <img alt="구름많음" className="block max-w-none size-full" src={cloudyIcon} />
        </div>
      );
    case 'CLOUDY':
      return (
        <div className="overflow-clip relative shrink-0 size-10">
          <img alt="흐림" className="block max-w-none size-full" src={overcastIcon} />
        </div>
      );
    default:
      return (
        <div className="overflow-clip relative shrink-0 size-10">
          <img alt="맑음" className="block max-w-none size-full" src={sunnyIcon} />
        </div>
      );
  }
}
export default function WeatherForecast() {
  const { data: weathers, loading, selectedWeather, selectWeather } = useWeatherStore();

  useEffect(() => {
    if (weathers && weathers.length > 0) {
      selectWeather(weathers[0]);
    }
  }, [weathers, selectWeather])

  if (loading || !weathers || weathers.length === 0) {
    return (
      <div className="backdrop-blur-[15px] backdrop-filter bg-white/70 box-border content-stretch flex items-start justify-between px-[60px] py-5 relative rounded-[30px] shrink-0 w-full">
        <div className="absolute border border-gray-200 border-solid inset-0 pointer-events-none rounded-[30px]" />
        
        {/* Skeleton for 5 weather items */}
        {Array.from({ length: 5 }).map((_, index) => (
          <div key={index} className="content-stretch flex flex-col gap-1.5 items-center justify-center relative shrink-0 w-[120px]">
            {/* Date skeleton */}
            <div className="h-4 w-12 bg-gray-200 rounded animate-pulse" />
            {/* Icon skeleton */}
            <div className="size-10 bg-gray-200 rounded animate-pulse" />
            {/* Temperature skeleton */}
            <div className="h-4 w-8 bg-gray-200 rounded animate-pulse" />
          </div>
        ))}
      </div>
    );
  }
  const getForecastDate = (index: number) => {
    const today = new Date();
    const targetDate = new Date(today);
    targetDate.setDate(today.getDate() + index);

    switch (index) {
      case 0: return "오늘";
      case 1: return "내일";
      case 2: return "모레";
      default: return `${targetDate.getMonth() + 1}월 ${targetDate.getDate()}일`;
    }
  };

  const getSkyStatus = (weather?: WeatherDto) => {
    return weather?.skyStatus || 'CLEAR';
  };

  const displayTemp = (temp?: number) => temp ? `${Math.round(temp)}°` : '-';

  return (
    <TooltipProvider>
      <div className="backdrop-blur-[15px] backdrop-filter bg-white/70 box-border content-stretch flex items-start justify-between px-[60px] py-5 relative rounded-[30px] shrink-0 w-full">
        <div className="absolute border border-gray-200 border-solid inset-0 pointer-events-none rounded-[30px]" />

        {
          weathers.map((weather, dayOffset) => {
            const { temperature } = weather;
            const date = getForecastDate(dayOffset);
            const skyStatus = getSkyStatus(weather)
            const isSelected = selectedWeather?.id === weather.id;

            return (
              <Tooltip key={weather.id}>
                <div
                    className="content-stretch flex flex-col gap-1.5 items-center justify-center relative shrink-0 w-[120px] cursor-pointer hover:border-1 rounded-2xl"
                    onClick={() => selectWeather(weather)}
                >
                  <div className={`font-${isSelected ? 'extrabold' : 'bold'} leading-none min-w-full not-italic relative shrink-0 text-base text-center tracking-[-0.4px] ${isSelected ? 'text-blue-500' : 'text-gray-800'}`} style={{ width: "min-content" }}>
                    <p className="leading-normal">{date}</p>
                  </div>

                  <TooltipTrigger asChild>
                    <div>
                      <WeatherIcon skyStatus={skyStatus} />

                      <div className="font-semibold leading-none not-italic relative shrink-0 text-gray-500 text-base text-center text-nowrap tracking-[-0.4px]">
                        <p className="leading-normal whitespace-pre">{displayTemp(temperature.current)}</p>
                      </div>
                    </div>
                  </TooltipTrigger>
                </div>
                <TooltipContent
                  side="right"
                  // sideOffset={-50}
                  align="start"
                  alignOffset={25}

                  className="bg-[rgba(12,12,13,0.74)] text-[#f7f7f8] font-semibold text-[14px] tracking-[-0.35px] px-3.5 py-3 rounded-[10px] flex flex-col gap-2 leading-none border-0"
                >
                  <div className="whitespace-pre">날씨: {getSkyStatusText(skyStatus)}</div>
                  <div className="whitespace-pre">평균: {displayTemp(temperature.current)}</div>
                  <div className="whitespace-pre">최저: {displayTemp(temperature.min)}</div>
                  <div className="whitespace-pre">최고: {displayTemp(temperature.max)}</div>
                </TooltipContent>
              </Tooltip>
            )
          })
        }
      </div>
    </TooltipProvider>
  );
}