import { type OotdDto } from '@/lib/api/types';

interface RecommendationItemProps {
  item: OotdDto;
  onClick?: () => void;
}

export default function RecommendationItem({ item, onClick }: RecommendationItemProps) {
  return (
    <div 
      className="content-stretch flex flex-col gap-3 items-start justify-start relative w-full"
      onClick={onClick}
    >
      {/* 이미지 섹션 */}
      <div className="aspect-square bg-gray-200 rounded-[16px] shrink-0 w-full overflow-hidden relative">
        {item.imageUrl ? (
          <img 
            src={item.imageUrl} 
            alt={item.name}
            className="w-full h-full object-cover"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gray-300">
            <div className="text-gray-500 text-sm">이미지 없음</div>
          </div>
        )}
      </div>

      {/* 제목 */}
      <div className="font-bold leading-none not-italic overflow-ellipsis overflow-hidden relative shrink-0 text-[#212126] text-[18px] text-nowrap tracking-[-0.45px] w-full">
        <p className="text-overflow-inherit text-wrap-mode-inherit white-space-collapse-inherit leading-normal overflow-inherit truncate">
          {item.name}
        </p>
      </div>

      {/* 속성 태그들 */}
      <div className="relative shrink-0 w-full">
        <div className="content-stretch flex gap-1.5 items-center justify-start overflow-x-auto">
          {item.attributes.map((attribute, index) => (
              <div
                  key={index}
                  className="box-border content-stretch flex gap-1 items-center justify-center px-1.5 py-1 relative rounded-[7px] shrink-0 border border-gray-300"
              >
                <div className="font-semibold leading-none not-italic relative shrink-0 text-gray-500 text-[14px] tracking-[-0.35px] w-full">
                  <p className="leading-normal">{attribute.value}</p>
                </div>
              </div>
          ))}
          {/* 페이드 효과를 위한 스페이서 */}
          <div className="shrink-0 w-8 h-1" />
        </div>
        {/* 오른쪽 페이드 효과 */}
        <div className="absolute right-0 top-0 h-full w-8 bg-gradient-to-l from-white to-transparent pointer-events-none" />
      </div>
    </div>
  );
}