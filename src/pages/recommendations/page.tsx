import WeatherSection from '@/components/recommendations/WeatherSection';
import RecommendationSection from '@/components/recommendations/RecommendationSection';

export default function RecommendationsPage() {
  return (
    <div className="flex flex-col h-full">
      {/* 날씨 섹션 */}
      <div className="flex-shrink-0">
        <WeatherSection />
      </div>
      
      {/* 추천 섹션 */}
      <div className="flex-1 min-h-0">
        <RecommendationSection />
      </div>
    </div>
  );
}