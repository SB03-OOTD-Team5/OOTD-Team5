import {useCallback, useState} from 'react';

export interface GeoLocation {
  longitude: number;
  latitude: number;
}

export default function useGeoLocation() {
  const [location, setLocation] = useState<GeoLocation>();

  const refetchLocation = useCallback(async () => {
    const geoLocation = navigator.geolocation;

    if (!geoLocation) {
      let longitude: number = 0;
      let latitude: number = 0;
      longitude = Number(prompt('Geolocation API를 사용할 수 없습니다.\n경도를 직접 입력해주세요. \n(예: 126.988)'));
      if (!longitude) {
        return;
      }
      latitude = Number(prompt('위도를 직접 입력해주세요. \n(예: 37.567)'));
      if (!latitude) {
        return;
      }
      if (longitude && latitude) {
        setLocation({longitude, latitude});
      }
    } else {
      geoLocation.getCurrentPosition(async (position) => {
        setLocation(
            {
              longitude: position.coords.longitude,
              latitude: position.coords.latitude,
            }
        );
      });
    }
  }, []);

  return { location, refetchLocation, setLocation };
}
