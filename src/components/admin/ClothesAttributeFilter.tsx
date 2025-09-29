import { useState } from "react";
import { useClothesAttributeDefStore } from "@/lib/stores/useClothesAttributeDefStore";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import searchIcon from "@/assets/icons/ic_search.svg";

interface ClothesAttributeFilterProps {
  onAddClick: () => void;
}

export default function ClothesAttributeFilter({ onAddClick }: ClothesAttributeFilterProps) {
  const { updateParams } = useClothesAttributeDefStore();
  const [searchKeyword, setSearchKeyword] = useState("");
  const [searchTimeout, setSearchTimeout] = useState<NodeJS.Timeout | null>(null);

  const handleSearch = (keyword: string) => {
    setSearchKeyword(keyword);
    
    // 이전 타이머 클리어
    if (searchTimeout) {
      clearTimeout(searchTimeout);
    }
    
    // 500ms 디바운싱 후 검색 실행
    const timeout = setTimeout(() => {
      updateParams({ keywordLike: keyword || undefined });
    }, 500);
    
    setSearchTimeout(timeout);
  };

  return (
    <div className="flex items-center justify-between px-5 py-2.5">
      {/* 검색창 */}
      <div className="relative w-[280px]">
        <div className="absolute left-[22px] top-1/2 transform -translate-y-1/2 size-5">
          <svg width="20" height="21" viewBox="0 0 20 21" fill="none" xmlns="http://www.w3.org/2000/svg">
            <g clipPath="url(#clip0_2479_9089)">
              <path d="M13.327 8.58691C13.2988 8.01428 13.1717 7.45025 12.9518 6.91943C12.7005 6.31281 12.3321 5.76182 11.8678 5.29753C11.4035 4.83323 10.8526 4.46482 10.2459 4.21354C9.71511 3.99368 9.15109 3.86656 8.57845 3.83838L8.33268 3.83268C7.67609 3.83268 7.02605 3.96228 6.41943 4.21354C5.81281 4.46482 5.26182 4.83323 4.79753 5.29753C4.33323 5.76182 3.96482 6.31281 3.71354 6.91943C3.46228 7.52605 3.33268 8.17609 3.33268 8.83268L3.33838 9.07845C3.36656 9.65109 3.49368 10.2151 3.71354 10.7459C3.96482 11.3526 4.33323 11.9035 4.79753 12.3678C5.26182 12.8321 5.81281 13.2005 6.41943 13.4518C7.02605 13.7031 7.67609 13.8327 8.33268 13.8327C8.98927 13.8327 9.63932 13.7031 10.2459 13.4518C10.8526 13.2005 11.4035 12.8321 11.8678 12.3678C12.3321 11.9035 12.7005 11.3526 12.9518 10.7459C13.2031 10.1393 13.3327 9.48927 13.3327 8.83268L13.327 8.58691ZM14.9912 9.16064C14.9536 9.92408 14.7847 10.6763 14.4915 11.384C14.1565 12.1927 13.6661 12.928 13.047 13.547C12.428 14.1661 11.6927 14.6565 10.884 14.9915C10.0751 15.3266 9.20816 15.4993 8.33268 15.4993C7.4572 15.4993 6.59025 15.3266 5.78141 14.9915C4.97264 14.6565 4.23734 14.1661 3.61833 13.547C2.99931 12.928 2.50886 12.1927 2.17383 11.384C1.8807 10.6763 1.71176 9.92408 1.67415 9.16064L1.66602 8.83268C1.66602 7.9572 1.8388 7.09025 2.17383 6.28141C2.50886 5.47264 2.99931 4.73734 3.61833 4.11833C4.23734 3.49931 4.97264 3.00886 5.78141 2.67383C6.59025 2.3388 7.4572 2.16602 8.33268 2.16602L8.66064 2.17415C9.42408 2.21176 10.1763 2.3807 10.884 2.67383C11.6927 3.00886 12.428 3.49931 13.047 4.11833C13.6661 4.73734 14.1565 5.47264 14.4915 6.28141C14.8266 7.09025 14.9993 7.9572 14.9993 8.83268L14.9912 9.16064Z" fill="#A9A9B1"/>
              <path d="M11.9101 12.4101C12.2355 12.0847 12.763 12.0847 13.0885 12.4101L18.0885 17.4101C18.4139 17.7355 18.4139 18.263 18.0885 18.5885C17.763 18.9139 17.2355 18.9139 16.9101 18.5885L11.9101 13.5885C11.5847 13.263 11.5847 12.7355 11.9101 12.4101Z" fill="#A9A9B1"/>
            </g>
            <defs>
              <clipPath id="clip0_2479_9089">
                <rect width="20" height="20" fill="white" transform="translate(0 0.5)"/>
              </clipPath>
            </defs>
          </svg>
        </div>
        <Input
          type="text"
          placeholder="검색"
          value={searchKeyword}
          icon={
            <img
                src={searchIcon}
                alt="검색"
                className="absolute left-[22px] top-1/2 transform -translate-y-1/2 size-5"
            />
          }
          onChange={(e) => handleSearch(e.target.value)}
          className="w-full h-[36px] pl-[54px] pr-[22px] py-3.5 bg-white border border-[#d4d4d9] rounded-[100px] text-[16px] font-['SUIT:SemiBold',_sans-serif] text-[#575765] placeholder:text-[#a9a9b1] focus:border-[#d4d4d9] focus:ring-0 shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]"
        />
      </div>

      {/* 속성 추가 버튼 */}
      <Button
        onClick={onAddClick}
        className="h-[36px] px-[18px] py-2.5 bg-[#1e89f4] hover:bg-[#1e89f4]/90 text-white font-['SUIT:Bold',_sans-serif] text-[18px] tracking-[-0.45px] rounded-[12px] gap-1.5"
      >
        속성 추가
      </Button>
    </div>
  );
}