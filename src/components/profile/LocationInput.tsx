import useGeoLocation, {type GeoLocation} from '@/hooks/useGeoLocation';
import type {WeatherAPILocation} from '@/lib/api/types';
import imageLocationIcon from '@/assets/icons/ic_local.svg';
import {getWeatherLocation} from "@/lib/api/weather.ts";
import {useEffect} from "react";

interface LocationInputProps {
  location?: WeatherAPILocation
  onChange: (location: WeatherAPILocation | undefined) => void;
  className?: string;
}

export default function LocationInput({
  location,
  onChange,
  className = ""
}: LocationInputProps) {
  const { location: geoLocation, setLocation, refetchLocation } = useGeoLocation();

  useEffect(() => {
    if (geoLocation && !isSame(geoLocation, location)) {
      getWeatherLocation({longitude: geoLocation?.longitude, latitude: geoLocation?.latitude})
      .then(onChange)
      .then(() => setLocation(undefined));
    }
  }, [geoLocation, location, onChange]);

  const isSame = (geoLocation: GeoLocation, location?: WeatherAPILocation) => {
    return geoLocation.latitude === location?.latitude && geoLocation.longitude === location?.longitude;
  }

  return (
    <div className={`space-y-2.5 ${className}`}>
      <label className="block text-[var(--font-size-body-3)] font-[var(--font-weight-bold)] text-[var(--color-gray-500)] tracking-[-0.35px]">
        현재 위치
      </label>
      
      {/* CurrentWeather 스타일을 참고한 위치 표시 */}
      <div className="bg-white box-border flex items-center justify-between px-5 py-3.5 rounded-[12px] border border-[var(--color-gray-200)] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]">
        {/* 위치 표시 영역 */}
        <div className="flex gap-1.5 items-center flex-1">
          <input
            type="text"
            value={location?.locationNames.reduce((a,b) => a.concat(' ').concat(b)) || ""}
            readOnly
            className="flex-1 bg-transparent outline-none placeholder:text-[var(--color-gray-400)] text-[var(--color-gray-700)] text-[var(--font-size-body-2)] font-[var(--font-weight-semibold)] tracking-[-0.4px]"
          />
        </div>
        
        {/* 위치 아이콘 버튼 (CurrentWeather 스타일) */}
        <div 
          className="relative bg-white rounded-full shrink-0 size-6 cursor-pointer hover:bg-gray-50 transition-colors" 
          onClick={refetchLocation}
          title="현재 위치 가져오기"
        >
          <div className="box-border flex items-center justify-center overflow-clip p-[2px] relative size-6">
            <div className="overflow-clip relative shrink-0 size-[18px]">
              <div className="absolute inset-[11.806%]">
                <img alt="위치" className="block max-w-none size-full" src={imageLocationIcon} />
              </div>
            </div>
          </div>
          <div className="absolute border border-[var(--color-gray-200)] border-solid inset-0 pointer-events-none rounded-full" />
        </div>
      </div>
    </div>
  );
}