export default function FeedCardSkeleton() {
  return (
    <div className="bg-white box-border content-stretch flex flex-col gap-4 items-start justify-start px-3 py-4 relative rounded-[20px] w-full border border-solid border-zinc-200">
      {/* 헤더 스켈레톤 */}
      <div className="box-border content-stretch flex items-center justify-between px-1.5 py-0 relative shrink-0 w-full">
        <div className="content-stretch flex gap-1.5 items-start justify-start relative shrink-0">
          <div className="box-border content-stretch flex gap-2 items-center justify-start px-0 py-[3px] relative shrink-0">
            {/* 프로필 스켈레톤 */}
            <div className="bg-gray-200 relative rounded-[100px] shrink-0 size-5 animate-pulse" />
          </div>
          <div className="content-stretch flex flex-col gap-1 items-start justify-start relative shrink-0 w-[94px]">
            {/* 사용자명 스켈레톤 */}
            <div className="bg-gray-200 h-4 rounded w-16 animate-pulse" />
            {/* 날짜/시간 스켈레톤 */}
            <div className="bg-gray-200 h-3 rounded w-20 animate-pulse" />
          </div>
        </div>
        {/* 날씨 배지 스켈레톤 */}
        <div className="bg-gray-200 h-8 rounded-[10px] shrink-0 w-20 animate-pulse" />
      </div>

      {/* 이미지 영역 스켈레톤 */}
      <div className="aspect-[307.5/206] content-stretch flex gap-0.5 items-start justify-start overflow-clip relative shrink-0 w-full">
        <div className="h-full relative shrink-0 w-[182px]">
          <div className="bg-gray-200 absolute inset-0 animate-pulse rounded" />
        </div>
        <div className="basis-0 content-stretch flex flex-col gap-0.5 grow h-full items-start justify-center min-h-px min-w-px relative shrink-0">
          <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-full">
            <div className="bg-gray-200 absolute inset-0 animate-pulse rounded" />
          </div>
          <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-full">
            <div className="bg-gray-200 absolute inset-0 animate-pulse rounded" />
          </div>
        </div>
      </div>

      {/* 내용 스켈레톤 */}
      <div className="box-border content-stretch flex gap-2 items-center justify-start px-0.5 py-0 relative shrink-0 w-full">
        <div className="bg-gray-200 h-4 rounded w-32 animate-pulse" />
      </div>

      {/* 하단 액션 스켈레톤 */}
      <div className="box-border content-stretch flex gap-3.5 items-center justify-start pb-2 pt-1 px-0.5 relative shrink-0 w-full">
        <div className="content-stretch flex gap-0.5 items-end justify-start relative shrink-0">
          <div className="bg-gray-200 size-4 rounded animate-pulse" />
          <div className="bg-gray-200 h-3 rounded w-8 animate-pulse" />
          <div className="bg-gray-200 h-3 rounded w-4 animate-pulse" />
        </div>
        <div className="content-stretch flex gap-0.5 items-center justify-start relative shrink-0">
          <div className="bg-gray-200 size-4 rounded animate-pulse" />
          <div className="bg-gray-200 h-3 rounded w-6 animate-pulse" />
          <div className="bg-gray-200 h-3 rounded w-4 animate-pulse" />
        </div>
      </div>
    </div>
  );
}