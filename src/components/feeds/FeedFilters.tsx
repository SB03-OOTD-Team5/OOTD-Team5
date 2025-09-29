import {useState} from 'react';
import {useFeedStore} from '@/lib/stores/useFeedStore';
import {Input} from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import searchIcon from '@/assets/icons/ic_search.svg';
import type {PrecipitationType, SkyStatus} from "@/lib/api";

export default function FeedFilters() {
  const { params, updateParams } = useFeedStore();
  const [searchValue, setSearchValue] = useState('');
  const [searchTimeout, setSearchTimeout] = useState<NodeJS.Timeout | null>(null);

  const handleSearchChange = (value: string) => {
    setSearchValue(value);
    
    // 이전 타이머 클리어
    if (searchTimeout) {
      clearTimeout(searchTimeout);
    }
    
    // 300ms 디바운싱 후 검색 실행
    const timeout = setTimeout(() => {
      updateParams({ keywordLike: value.trim() });
    }, 300);
    
    setSearchTimeout(timeout);
  };

  const weatherOptions = [
    { value: '전체', label: '날씨' },
    { value: 'CLEAR', label: '맑음' },
    { value: 'MOSTLY_CLOUDY', label: '구름많음' },
    { value: 'CLOUDY', label: '흐림' }
  ];

  const precipitationOptions = [
    { value: '전체', label: '강수' },
    { value: 'NONE', label: '없음' },
    { value: 'RAIN', label: '비' },
    { value: 'RAIN_SNOW', label: '비/눈' },
    { value: 'SNOW', label: '눈' },
    { value: 'SHOWER', label: '소나기' }
  ];

  const sortOptions: {value: string, label: string}[] = [
    { value: 'createdAt', label: '최신순' },
    { value: 'likeCount', label: '좋아요순' }
  ];

  const handleWeatherChange = (value: string) => {
    const weatherValue = value === '전체' ? undefined : value;
    updateParams({ skyStatusEqual: weatherValue as SkyStatus | undefined });
  };

  const handlePrecipitationChange = (value: string) => {
    const precipitationValue = value === '전체' ? undefined : value;
    updateParams({ precipitationTypeEqual: precipitationValue as PrecipitationType | undefined });
  };

  const handleSortChange = (value: string) => {
    updateParams({ 
      sortBy: value as "createdAt" | "likeCount" | undefined,
      sortDirection: 'DESCENDING'
    });
  };

  return (
    <div className="flex gap-3.5 items-center justify-between px-0 py-2.5">
      {/* 왼쪽 필터들 */}
      <div className="flex gap-3.5 items-center">
        {/* 검색바 */}
        <div className="relative w-[280px]">
          <Input
            type="text"
            placeholder="피드 내 검색하기"
            value={searchValue}
            icon={
              <img
                src={searchIcon}
                alt="검색"
                className="absolute left-[22px] top-1/2 transform -translate-y-1/2 size-5"
              />
            }
            onChange={(e) => handleSearchChange(e.target.value)}
            className="w-full h-[36px] pl-[54px] pr-[22px] py-3.5 bg-white border border-[#d4d4d9] rounded-[100px] text-[16px] font-['SUIT:SemiBold',_sans-serif] text-[#575765] placeholder:text-[#a9a9b1] focus:border-[#d4d4d9] focus:ring-0 shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]"
          />
        </div>

        {/* 날씨 필터 */}
        <Select value={params.skyStatusEqual || '전체'} onValueChange={handleWeatherChange}>
          <SelectTrigger className="w-[102px] h-[46px] px-[22px] pr-[18px] bg-white border border-[#d4d4d9] rounded-[100px] text-[16px] font-['SUIT:SemiBold',_sans-serif] text-[#575765] focus:border-[#d4d4d9] focus:ring-0 shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]">
            <SelectValue placeholder="날씨" />
          </SelectTrigger>
          <SelectContent>
            {weatherOptions.map((option) => (
              <SelectItem key={option.value} value={option.value}>
                {option.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        {/* 강수 필터 */}
        <Select value={params.precipitationTypeEqual || '전체'} onValueChange={handlePrecipitationChange}>
          <SelectTrigger className="w-[102px] h-[46px] px-[22px] pr-[18px] bg-white border border-[#d4d4d9] rounded-[100px] text-[16px] font-['SUIT:SemiBold',_sans-serif] text-[#575765] focus:border-[#d4d4d9] focus:ring-0 shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]">
            <SelectValue placeholder="강수" />
          </SelectTrigger>
          <SelectContent>
            {precipitationOptions.map((option) => (
              <SelectItem key={option.value} value={option.value}>
                {option.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {/* 정렬 옵션 */}
      <Select value={params.sortBy} onValueChange={handleSortChange}>
        <SelectTrigger className="w-auto h-[46px] px-[22px] pr-[18px] bg-white border border-[#d4d4d9] rounded-[100px] text-[16px] font-['SUIT:SemiBold',_sans-serif] text-[#575765] focus:border-[#d4d4d9] focus:ring-0 shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]">
          <SelectValue placeholder="최신순" />
        </SelectTrigger>
        <SelectContent>
          {sortOptions.map((option) => (
            <SelectItem key={option.value} value={option.value}>
              {option.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}