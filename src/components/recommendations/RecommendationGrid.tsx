import RecommendationItem from './RecommendationItem';
import {useRecommendationStore} from "@/lib/stores/useRecommendationStore.ts";

export default function RecommendationGrid() {
  const {data: recommendations, loading} = useRecommendationStore();

  if (loading) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4 w-full">
        {[...Array(10)].map((_, index) => (
          <div 
            key={index}
            className="bg-white rounded-[20px] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)] border border-[#e7e7e9] overflow-hidden animate-pulse"
          >
            <div className="aspect-square bg-gray-200" />
            <div className="p-4 space-y-2">
              <div className="h-4 bg-gray-200 rounded w-3/4" />
              <div className="h-3 bg-gray-200 rounded w-1/2" />
              <div className="h-6 bg-gray-200 rounded w-1/3" />
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (!recommendations || !recommendations.clothes || recommendations.clothes.length === 0) {
    return (
      <div className="flex items-center justify-center w-full py-16">
        <p className="text-gray-500 text-lg">추천할 옷을 찾을 수 없습니다.</p>
      </div>
    );
  }

  // 자연스러운 그리드로 모든 옷 표시
  const clothes = recommendations.clothes;

  return (
    <div className="w-full flex-1 overflow-y-auto">
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4 p-1">
        {clothes.map((item, index) => (
          <RecommendationItem
            key={`${item.clothesId}-${index}`}
            item={item}
          />
        ))}
      </div>
    </div>
  );
}