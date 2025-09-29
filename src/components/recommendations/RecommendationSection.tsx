import {useEffect} from 'react';
import RecommendationHeader from './RecommendationHeader';
import RecommendationGrid from './RecommendationGrid';
import EmptyRecommendation from './EmptyRecommendation';
import {useRecommendationStore} from "@/lib/stores/useRecommendationStore.ts";
import {useWeatherStore} from "@/lib/stores/useWeatherStore.ts";

export default function RecommendationSection() {
  const { selectedWeather } = useWeatherStore();
  const { data: recommendations, updateParams} = useRecommendationStore();

  useEffect(() => {
    if (selectedWeather?.id) {
      updateParams({ weatherId: selectedWeather.id });
    }
  }, [selectedWeather?.id, updateParams]);

  // selectedWeather가 없으면 추천 섹션을 렌더링하지 않음
  if (!selectedWeather) {
    return null;
  }

  const hasClothes = recommendations && recommendations.clothes.length > 0;

  return (
    <div className="relative w-full px-[100px] h-full">
      <div className="bg-white rounded-[20px] box-border content-stretch flex flex-col gap-[34px] px-[40px] items-start justify-start py-8 relative w-full h-full shadow-[0px_-2px_10px_0px_rgba(0,0,0,0.05)]">
        {hasClothes ? (
            <>
              <RecommendationHeader/>
              <RecommendationGrid/>
            </>
        ) : (
          <EmptyRecommendation/>
        )}
      </div>
    </div>
  );
}