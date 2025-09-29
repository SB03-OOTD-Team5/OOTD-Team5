import { useState } from 'react';
import type { FeedDto } from '@/lib/api/types';
import leftArrowIcon from '@/assets/icons/ic_left.svg';
import rightArrowIcon from '@/assets/icons/ic_right.svg';
import emptyImageIcon from '@/assets/icons/empty image.svg';

interface FeedDetailLeftSectionProps {
  feed: FeedDto;
}

export default function FeedDetailLeftSection({ feed }: FeedDetailLeftSectionProps) {
  const [currentIndex, setCurrentIndex] = useState(0);
  
  // 현재 선택된 OOTD
  const currentOotd = feed.ootds[currentIndex];

  const handlePrevious = () => {
    setCurrentIndex(prev => prev === 0 ? feed.ootds.length - 1 : prev - 1);
  };

  const handleNext = () => {
    setCurrentIndex(prev => prev === feed.ootds.length - 1 ? 0 : prev + 1);
  };

  const handleThumbnailClick = (index: number) => {
    setCurrentIndex(index);
  };

  return (
    <div className="w-[531px] relative shrink-0">
      <div className="content-stretch flex flex-col items-start justify-start overflow-clip relative w-full">
        {/* 메인 이미지 영역 - 600x600 정사각형 */}
        {currentOotd?.imageUrl ? (
          <div className="aspect-[600/600] bg-center bg-cover bg-no-repeat shrink-0 w-full relative" 
               style={{ backgroundImage: `url('${currentOotd.imageUrl}')` }}>
            
            {/* 네비게이션 화살표 - OOTD가 2개 이상일 때만 표시 */}
            {feed.ootds.length > 1 && (
              <>
                {/* 왼쪽 화살표 */}
                <button
                  onClick={handlePrevious}
                  className="absolute bg-[rgba(255,255,255,0.8)] overflow-clip left-5 rounded-[100px] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.3)] size-10 top-[251px]"
                >
                  <div className="absolute overflow-clip size-6 top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2">
                    <img src={leftArrowIcon} alt="이전" className="block max-w-none size-full" />
                  </div>
                </button>

                {/* 오른쪽 화살표 */}
                <button
                  onClick={handleNext}
                  className="absolute bg-[rgba(255,255,255,0.8)] overflow-clip right-5 rounded-[100px] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.3)] size-10 top-[251px]"
                >
                  <div className="absolute overflow-clip size-6 top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2">
                    <img src={rightArrowIcon} alt="다음" className="block max-w-none size-full" />
                  </div>
                </button>
              </>
            )}
          </div>
        ) : (
          <div className="aspect-[600/600] bg-[#f7f7f8] shrink-0 w-full flex items-center justify-center relative">
            <img src={emptyImageIcon} alt="이미지 없음" className="w-16 h-16" />
            
            {/* 이미지가 없을 때도 화살표 표시 */}
            {feed.ootds.length > 1 && (
              <>
                {/* 왼쪽 화살표 */}
                <button
                  onClick={handlePrevious}
                  className="absolute bg-[rgba(255,255,255,0.8)] overflow-clip left-5 rounded-[100px] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.3)] size-10 top-[251px]"
                >
                  <div className="absolute overflow-clip size-6 top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2">
                    <img src={leftArrowIcon} alt="이전" className="block max-w-none size-full" />
                  </div>
                </button>

                {/* 오른쪽 화살표 */}
                <button
                  onClick={handleNext}
                  className="absolute bg-[rgba(255,255,255,0.8)] overflow-clip right-5 rounded-[100px] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.3)] size-10 top-[251px]"
                >
                  <div className="absolute overflow-clip size-6 top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2">
                    <img src={rightArrowIcon} alt="다음" className="block max-w-none size-full" />
                  </div>
                </button>
              </>
            )}
          </div>
        )}

        {/* OOTD 아이템 정보 - 고정 높이 */}
        <div className="box-border content-stretch flex flex-col gap-3 items-start justify-start pb-3 pt-4 px-4 relative shrink-0 w-full h-[106px]">
          {currentOotd ? (
            <>
              <div className="font-['SUIT:Bold',_sans-serif] leading-[0] not-italic overflow-ellipsis overflow-hidden relative shrink-0 text-[24px] text-black text-nowrap tracking-[-0.6px] w-full">
                <p className="leading-[normal] overflow-ellipsis overflow-hidden truncate">{currentOotd.name}</p>
              </div>
              <div className="relative shrink-0 w-full">
                <div className="content-stretch flex gap-1.5 items-center justify-start overflow-x-auto min-h-[26px]">
                  {currentOotd.attributes?.map((attribute, index) => (
                    <div key={index} className="bg-white box-border content-stretch flex gap-1 items-center justify-center px-1.5 py-1 relative rounded-[7px] shrink-0">
                      <div aria-hidden="true" className="absolute border border-[#d4d4d9] border-solid inset-0 pointer-events-none rounded-[7px]" />
                      <div className="font-['SUIT:SemiBold',_sans-serif] leading-[0] not-italic relative shrink-0 text-[#808089] text-[14px] text-center text-nowrap tracking-[-0.35px]">
                        <p className="leading-[normal] whitespace-pre">{attribute.value}</p>
                      </div>
                    </div>
                  ))}
                  {/* 페이드 효과를 위한 스페이서 */}
                  <div className="shrink-0 w-8 h-1" />
                </div>
                {/* 오른쪽 페이드 효과 */}
                <div className="absolute right-0 top-0 h-full w-8 bg-gradient-to-l from-white to-transparent pointer-events-none" />
              </div>
            </>
          ) : (
            <div className="flex items-center justify-center w-full h-full">
              <span className="text-[#a9a9b1] text-[14px]">의상 정보가 없습니다</span>
            </div>
          )}
        </div>

        {/* OOTD 썸네일들 - 100x100 원형 */}
        <div className="box-border content-stretch flex gap-2.5 items-start justify-start px-4 py-3 relative shrink-0">
          {feed.ootds.map((ootd, index) => {
            const isSelected = currentIndex === index;
            const hasImage = !!ootd.imageUrl;
            
            return (
              <button
                key={ootd.clothesId}
                onClick={() => handleThumbnailClick(index)}
                className="relative rounded-[16px] shrink-0 size-[100px] cursor-pointer hover:opacity-80 transition-opacity"
              >
                {hasImage ? (
                  <>
                    <div 
                      className="bg-center bg-cover bg-no-repeat rounded-[16px] size-full" 
                      style={{ backgroundImage: `url('${ootd.imageUrl}')` }}
                    />
                    {isSelected && (
                      <div aria-hidden="true" className="absolute border-4 border-[#1e89f4] border-solid inset-0 pointer-events-none rounded-[16px]" />
                    )}
                  </>
                ) : (
                  <div className={`rounded-[16px] size-full flex items-center justify-center relative ${
                    isSelected ? 'bg-[#e3f2fd]' : 'bg-[#f7f7f8]'
                  }`}>
                    <img src={emptyImageIcon} alt="이미지 없음" className="w-8 h-8" />
                    {isSelected && (
                      <div aria-hidden="true" className="absolute border-4 border-[#1e89f4] border-solid inset-0 pointer-events-none rounded-[16px]" />
                    )}
                  </div>
                )}
              </button>
            );
          })}
        </div>
      </div>
      {/* 구분선 */}
      <div aria-hidden="true" className="absolute border-[0px_1px_0px_0px] border-[rgba(34,34,55,0.06)] border-solid inset-0 pointer-events-none" />
    </div>
  );
}