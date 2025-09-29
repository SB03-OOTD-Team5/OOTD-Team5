import { useState } from 'react';
import hangerIcon from '@/assets/icons/il_hanger.svg';
import refreshIcon from '@/assets/icons/ic_refresh.svg';
import {useRecommendationStore} from "@/lib/stores/useRecommendationStore.ts";
import AddFeedModal from './AddFeedModal';
import FeedDetailModal from "@/components/feeds/FeedDetailModal.tsx";
import type {FeedDto} from "@/lib/api";

export default function RecommendationHeader() {
  const {loading, fetch} = useRecommendationStore();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [createdFeed, setCreatedFeed] = useState<FeedDto | undefined>();

  const handleRegister = () => {
    setIsModalOpen(true);
  }

  const handleRefresh = () => {
    fetch();
  }

  return (
    <div className="content-stretch flex items-center justify-between relative w-full">
      {/* 헤더 섹션 */}
      <div className="content-stretch flex flex-col gap-1 items-start justify-center relative shrink-0">
        <div className="content-stretch flex gap-2 items-center justify-start relative shrink-0">
          <div className="overflow-clip relative shrink-0 size-6">
            <img alt="옷걸이" className="block max-w-none size-full" src={hangerIcon} />
          </div>
          <div className="font-extrabold leading-none not-italic relative shrink-0 text-[#212126] text-[24px] text-nowrap tracking-[-0.6px]">
            <p className="leading-normal whitespace-pre">#추천 OOTD</p>
          </div>
        </div>
        <div className="font-semibold leading-none not-italic relative shrink-0 text-[#808089] text-[18px] text-nowrap tracking-[-0.45px]">
          <p className="leading-normal whitespace-pre">오늘 날씨에 맞는 옷을 추천해드릴게요</p>
        </div>
      </div>

      {/* 버튼 섹션 */}
      <div className="content-stretch flex gap-3 items-center justify-start relative shrink-0">
        {/* 다른 옷 추천 버튼 */}
        <button
          className="bg-white box-border content-stretch flex gap-1.5 h-[46px] items-center justify-center px-[18px] py-2.5 relative rounded-[12px] shrink-0 hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed border border-[#d4d4d9] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]"
          onClick={handleRefresh}
          disabled={loading}
        >
          <div className="font-semibold leading-none not-italic relative shrink-0 text-[#696975] text-[16px] text-nowrap tracking-[-0.4px]">
            <p className="leading-normal whitespace-pre">
              {loading ? '추천 중...' : '다른 옷 추천'}
            </p>
          </div>
          <img alt="새로고침" className="size-5" src={refreshIcon} />
        </button>

        {/* OOTD 등록 버튼 */}
        <button
          className="bg-[#1e89f4] box-border content-stretch flex gap-1.5 h-[46px] items-center justify-center px-[18px] py-2.5 relative rounded-[12px] shrink-0 hover:bg-[#1e89f4]/90 transition-colors"
          onClick={handleRegister}
        >
          <div className="font-bold leading-none not-italic relative shrink-0 text-white text-[18px] text-nowrap tracking-[-0.45px]">
            <p className="leading-normal whitespace-pre">OOTD 등록</p>
          </div>
        </button>
      </div>

      {/* 피드 등록 모달 */}
      <AddFeedModal 
        open={isModalOpen} 
        onClose={() => setIsModalOpen(false)}
        onCreated={setCreatedFeed}
      />
      {/* 피드 상세 모달 */}
      {
        createdFeed &&
          <FeedDetailModal
              feed={createdFeed}
              open={true}
              onOpenChange={() => setCreatedFeed(undefined)}
          />
      }

    </div>
  );
}