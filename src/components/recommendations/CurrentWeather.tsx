import imageLocationIcon from '@/assets/icons/ic_local.svg';
import {useWeatherStore} from "@/lib/stores/useWeatherStore.ts";

interface CurrentWeatherProps {
  fetchLocation: () => Promise<void>;
}

export default function CurrentWeather({ fetchLocation }: CurrentWeatherProps) {
  const {selectedWeather: weather} = useWeatherStore();

  const temperature = weather?.temperature?.current;
  const skyStatus = weather?.skyStatus || "CLEAR";
  const tempMin = weather?.temperature?.min;
  const tempMax = weather?.temperature?.max;
  const tempDiff = weather?.temperature?.comparedToDayBefore;
  const humidity = weather?.humidity?.current;
  const humidityDiff = weather?.humidity?.comparedToDayBefore;
  const precipitation = weather?.precipitation || { type: 'NONE', amount: 0, probability: 0 };
  const windSpeed = weather?.windSpeed || { speed: 0, asWord: 'WEAK' };

  const getSkyStatusText = (status: string) => {
    switch (status) {
      case 'CLEAR': return '맑음';
      case 'CLOUDY': return '흐림';
      case 'MOSTLY_CLOUDY': return '구름많음';
      default: return '';
    }
  };

  const getPrecipitationText = (type: string) => {
    switch (type) {
      case 'NONE': return '없음';
      case 'RAIN': return '비';
      case 'SNOW': return '눈';
      case 'RAIN_SNOW': return '비/눈';
      case 'SHOWER': return '소나기';
      default: return '없음';
    }
  };

  const getWindText = (strength: string) => {
    switch (strength) {
      case 'WEAK': return '약한 바람';
      case 'MODERATE': return '보통 바람';
      case 'STRONG': return '강한 바람';
      default: return '약한 바람';
    }
  };

  return (
    <div className="box-border content-stretch flex flex-col gap-1.5 items-start justify-start px-5 py-0 relative shrink-0 w-full">
      {/* 위치 정보 */}
      <div className="content-stretch flex gap-1.5 items-center justify-center relative rounded-full shrink-0">
        <div className="font-bold leading-none not-italic relative shrink-0 text-gray-600 text-base text-center text-nowrap tracking-[-0.4px]">
          <p className="leading-normal whitespace-pre">
            {
              weather?.location?.locationNames?.reduce((prev, current) => prev.concat(' ').concat(current))
                || '위치 정보가 없습니다.'
            }
          </p>
        </div>
        <div className="bg-white relative rounded-full shrink-0 size-6 cursor-pointer" onClick={fetchLocation}>
          <div className="box-border content-stretch flex gap-2 items-center justify-center overflow-clip p-[2px] relative size-6">
            <div className="overflow-clip relative shrink-0 size-[18px]">
              <div className="absolute inset-[11.806%]">
                <img alt="위치" className="block max-w-none size-full" src={imageLocationIcon} />
              </div>
            </div>
          </div>
          <div className="absolute border border-gray-200 border-solid inset-0 pointer-events-none rounded-full" />
        </div>
      </div>

      {/* 메인 날씨 정보 */}
      {weather ? (
        <div className="content-stretch flex items-center justify-between relative shrink-0 w-full">
          {/* 온도 및 상태 */}
          <div className="basis-0 box-border content-stretch flex gap-4 grow items-center justify-start min-h-px min-w-px pl-0 pr-5 py-0 relative shrink-0">
            <div className="font-extrabold leading-none not-italic relative shrink-0 text-gray-900 text-[50px] text-center text-nowrap tracking-[-1.25px]">
              <p className="leading-normal whitespace-pre">{temperature != null ? `${Math.round(temperature)}°` : '-'}</p>
            </div>
            <div className="basis-0 content-stretch flex flex-col gap-[3px] grow items-start justify-start leading-none min-h-px min-w-px not-italic relative shrink-0">
              <div className="content-stretch flex items-center justify-between relative shrink-0 text-center w-[146px]">
                <div className="font-bold relative shrink-0 text-gray-900 text-xl text-nowrap tracking-[-0.5px]">
                  <p className="leading-normal whitespace-pre">{getSkyStatusText(skyStatus)}</p>
                </div>
                <div className="font-extrabold relative shrink-0 text-gray-700 text-[18px] tracking-[-0.45px] w-[105px]">
                  <p className="leading-normal not-italic">
                    {
                        tempMin != null && tempMax != null ? (
                          <>
                            <span>{`${Math.round(tempMin)}°`} / </span>
                            <span className="text-gray-700">{`${Math.round(tempMax)}°`} </span>
                          </>
                        ) : ''
                    }
                  </p>
                </div>
              </div>
              <div className="font-bold relative shrink-0 text-gray-500 text-sm tracking-[-0.35px] w-[105px]">
                <p className="leading-normal">
                  {tempDiff != null ? '어제보다 ' + `${(tempDiff > 0 ? '+' : '')}${Math.round(tempDiff)}° ` : '-'}
                </p>
              </div>
            </div>
          </div>

          {/* 상세 날씨 정보 */}
          <div className="box-border content-stretch flex gap-5 items-center justify-start pl-5 pr-0 py-0 relative shrink-0">
            <div className="absolute border-gray-300 border-l-2 border-solid inset-0 pointer-events-none" />

            {/* 습도 */}
            <div className="content-stretch flex gap-2.5 items-start justify-start relative shrink-0">
              <div className="font-bold leading-none not-italic relative shrink-0 text-gray-600 text-base text-center text-nowrap tracking-[-0.4px]">
                <p className="leading-normal whitespace-pre">습도</p>
              </div>
              <div className="content-stretch flex flex-col gap-1 items-start justify-start leading-none not-italic relative shrink-0 w-[97px]">
                <div className="font-bold relative shrink-0 text-gray-800 text-base tracking-[-0.4px] w-full">
                  <p className="leading-normal">{humidity != null ? Math.round(humidity) + '%' : '-'}</p>
                </div>
                <div className="font-semibold relative shrink-0 text-gray-500 text-sm tracking-[-0.35px] w-full">
                  <p className="leading-normal">
                    {humidityDiff != null ? '어제보다 ' +(humidityDiff > 0 ? '+' : '') + Math.round(humidityDiff) + '%' : '-'}
                  </p>
                </div>
              </div>
            </div>

            {/* 강수 */}
            <div className="content-stretch flex gap-2.5 items-start justify-start relative shrink-0">
              <div className="font-bold leading-none not-italic relative shrink-0 text-gray-600 text-base text-center text-nowrap tracking-[-0.4px]">
                <p className="leading-normal whitespace-pre">강수</p>
              </div>
              <div className="content-stretch flex flex-col gap-1 items-start justify-start leading-none not-italic relative shrink-0 w-[89px]">
                <div className="font-bold relative shrink-0 text-gray-800 text-base tracking-[-0.4px] w-full">
                  <p className="leading-normal">{getPrecipitationText(precipitation.type)}</p>
                </div>
                <div className="font-semibold relative shrink-0 text-gray-500 text-sm tracking-[-0.35px] w-full">
                  <p className="leading-normal">{Math.round(precipitation.amount)}mm / {Math.round(precipitation.probability)}%</p>
                </div>
              </div>
            </div>

            {/* 바람 */}
            <div className="content-stretch flex gap-2.5 items-start justify-start relative shrink-0">
              <div className="font-bold leading-none not-italic relative shrink-0 text-gray-600 text-base text-center text-nowrap tracking-[-0.4px]">
                <p className="leading-normal whitespace-pre">바람</p>
              </div>
              <div className="content-stretch flex flex-col gap-1 items-start justify-start leading-none not-italic relative shrink-0 w-[51px]">
                <div className="font-bold relative shrink-0 text-gray-800 text-base tracking-[-0.4px] w-full">
                  <p className="leading-normal">{windSpeed.speed != null ? Math.round(windSpeed.speed) + 'm/s' : '-'}</p>
                </div>
                <div className="font-semibold relative shrink-0 text-gray-500 text-sm tracking-[-0.35px] w-full">
                  <p className="leading-normal">{getWindText(windSpeed.asWord)}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      ) : (
        <div className="content-stretch flex items-center justify-between relative shrink-0 w-full">
          {/* Skeleton for temperature and status */}
          <div className="basis-0 box-border content-stretch flex gap-4 grow items-center justify-start min-h-px min-w-px pl-0 pr-5 py-0 relative shrink-0">
            <div className="h-[60px] w-[120px] bg-gray-200 rounded animate-pulse" />
            <div className="basis-0 content-stretch flex flex-col gap-[3px] grow items-start justify-start leading-none min-h-px min-w-px not-italic relative shrink-0">
              <div className="content-stretch flex items-center justify-between relative shrink-0 text-center w-[146px]">
                <div className="h-6 w-16 bg-gray-200 rounded animate-pulse" />
                <div className="h-5 w-20 bg-gray-200 rounded animate-pulse" />
              </div>
              <div className="h-4 w-24 bg-gray-200 rounded animate-pulse" />
            </div>
          </div>

          {/* Skeleton for weather details */}
          <div className="box-border content-stretch flex gap-5 items-center justify-start pl-5 pr-0 py-0 relative shrink-0">
            <div className="absolute border-gray-300 border-l-2 border-solid inset-0 pointer-events-none" />

            {/* Skeleton for humidity */}
            <div className="content-stretch flex gap-2.5 items-start justify-start relative shrink-0">
              <div className="h-4 w-8 bg-gray-200 rounded animate-pulse" />
              <div className="content-stretch flex flex-col gap-1 items-start justify-start leading-none not-italic relative shrink-0 w-[97px]">
                <div className="h-4 w-12 bg-gray-200 rounded animate-pulse" />
                <div className="h-3 w-16 bg-gray-200 rounded animate-pulse" />
              </div>
            </div>

            {/* Skeleton for precipitation */}
            <div className="content-stretch flex gap-2.5 items-start justify-start relative shrink-0">
              <div className="h-4 w-8 bg-gray-200 rounded animate-pulse" />
              <div className="content-stretch flex flex-col gap-1 items-start justify-start leading-none not-italic relative shrink-0 w-[89px]">
                <div className="h-4 w-10 bg-gray-200 rounded animate-pulse" />
                <div className="h-3 w-16 bg-gray-200 rounded animate-pulse" />
              </div>
            </div>

            {/* Skeleton for wind */}
            <div className="content-stretch flex gap-2.5 items-start justify-start relative shrink-0">
              <div className="h-4 w-8 bg-gray-200 rounded animate-pulse" />
              <div className="content-stretch flex flex-col gap-1 items-start justify-start leading-none not-italic relative shrink-0 w-[51px]">
                <div className="h-4 w-12 bg-gray-200 rounded animate-pulse" />
                <div className="h-3 w-14 bg-gray-200 rounded animate-pulse" />
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}