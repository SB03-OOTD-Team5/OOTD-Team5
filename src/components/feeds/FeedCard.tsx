import type {FeedDto, SkyStatus} from '@/lib/api/types';
import { likeFeed, unlikeFeed } from '@/lib/api/feeds';
import { useFeedStore } from '@/lib/stores/useFeedStore';
import { toast } from 'sonner';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';

import sunnyIcon from '@/assets/illust_logos/il_Sunny.svg';
import overcastIcon from '@/assets/illust_logos/il_Overcast.svg';
import cloudyIcon from '@/assets/illust_logos/il_cloudy.svg';
import profileIcon from '@/assets/icons/profile.svg';
import heartIcon from '@/assets/icons/ic_heart.svg';
import heartFilledIcon from '@/assets/icons/ic_heart_filled.svg';
import commentIcon from '@/assets/icons/ic_comment.svg';
import emptyImageIcon from '@/assets/icons/empty image.svg';
import {useNavigate} from "react-router-dom";

function WeatherIcon({ skyStatus }: { skyStatus: SkyStatus }) {
  switch (skyStatus) {
    case 'CLEAR':
      return <img alt="맑음" className="block max-w-none size-full" src={sunnyIcon} />;
    case 'MOSTLY_CLOUDY':
      return <img alt="구름많음" className="block max-w-none size-full" src={cloudyIcon} />;
    case 'CLOUDY':
      return <img alt="흐림" className="block max-w-none size-full" src={overcastIcon} />;
    default:
      return <img alt="맑음" className="block max-w-none size-full" src={sunnyIcon} />;
  }
}

// Action icons imported above

interface FeedCardProps {
  feed: FeedDto;
  onClick?: () => void;
}

function ImageLayout({ ootds }: { ootds: FeedDto['ootds'] }) {
  const images = ootds.filter(ootd => ootd.imageUrl);



  if (images.length === 0) {
    return (
      <div className="aspect-[307.5/206] content-stretch flex items-center justify-center overflow-clip relative shrink-0 w-full bg-gray-100 rounded">
        <img src={emptyImageIcon} alt="이미지 없음" className="w-16 h-16" />
      </div>
    );
  }

  if (images.length === 1) {
    // 1개 이미지: 전체 영역
    return (
      <div className="aspect-[307.5/206] content-stretch flex items-start justify-start overflow-clip relative shrink-0 w-full">
        <div className="h-full relative w-full">
          <div 
            className="absolute bg-center bg-cover bg-no-repeat inset-0 rounded" 
            style={{ backgroundImage: `url('${images[0].imageUrl}')` }} 
          />
        </div>
      </div>
    );
  }

  if (images.length === 2) {
    // 2개 이미지: 좌우 분할
    return (
      <div className="aspect-[307.5/206] content-stretch flex gap-0.5 items-start justify-start overflow-clip relative shrink-0 w-full">
        <div className="h-full relative shrink-0 w-[182px]">
          <div 
            className="absolute bg-center bg-cover bg-no-repeat inset-0 rounded" 
            style={{ backgroundImage: `url('${images[0].imageUrl}')` }} 
          />
        </div>
        <div className="basis-0 grow h-full relative shrink-0">
          <div 
            className="absolute bg-center bg-cover bg-no-repeat inset-0 rounded" 
            style={{ backgroundImage: `url('${images[1].imageUrl}')` }} 
          />
        </div>
      </div>
    );
  }

  // 3개 이상: 왼쪽 1개, 오른쪽 상하 2개 + 오버레이
  return (
    <div className="aspect-[307.5/206] content-stretch flex gap-0.5 items-start justify-start overflow-clip relative shrink-0 w-full">
      <div className="h-full relative shrink-0 w-[182px]">
        <div 
          className="absolute bg-center bg-cover bg-no-repeat inset-0 rounded" 
          style={{ backgroundImage: `url('${images[0].imageUrl}')` }} 
        />
      </div>
      <div className="basis-0 content-stretch flex flex-col gap-0.5 grow h-full items-start justify-center min-h-px min-w-px relative shrink-0">
        <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-full">
          <div 
            className="absolute bg-center bg-cover bg-no-repeat inset-0 rounded" 
            style={{ backgroundImage: `url('${images[1].imageUrl}')` }} 
          />
        </div>
        <div className="basis-0 grow min-h-px min-w-px relative shrink-0 w-full">
          <div 
            className="absolute bg-center bg-cover bg-no-repeat inset-0 rounded" 
            style={{ backgroundImage: `url('${images[2]?.imageUrl || images[1].imageUrl}')` }} 
          />
          {images.length > 3 && (
            <div className="absolute bg-[rgba(33,33,38,0.9)] box-border content-stretch flex gap-2 items-center justify-center px-2 py-1 right-2 rounded-[100px] top-2">
              <div className="font-[var(--font-weight-semibold)] leading-[0] not-italic relative shrink-0 text-[14px] text-nowrap text-white tracking-[-0.35px]">
                <p className="leading-[normal] whitespace-pre">+{images.length - 3}</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default function FeedCard({ feed, onClick }: FeedCardProps) {
  const { update } = useFeedStore();
  const navigate = useNavigate();

  const handleLike = async () => {
    try {
      if (feed.likedByMe) {
        await unlikeFeed(feed.id);
        update(feed.id, { 
          likedByMe: false, 
          likeCount: feed.likeCount - 1 
        });
      } else {
        await likeFeed(feed.id);
        update(feed.id, { 
          likedByMe: true, 
          likeCount: feed.likeCount + 1 
        });
      }
    } catch {
      toast.error('좋아요 처리 중 오류가 발생했습니다.');
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return {
      date: `${month}월 ${day}일`,
      time: `${hours}:${minutes}`
    };
  };

  const { date, time } = formatDate(feed.createdAt);

  function getSkyStatusText(skyStatus: SkyStatus): string {
    switch (skyStatus) {
      case 'CLEAR':
        return '맑음';
      case 'MOSTLY_CLOUDY':
        return '구름많음';
      case 'CLOUDY':
        return '흐림';
      default:
        return '맑음';
    }
  }
  const displayTemp = (temp?: number) => temp ? `${Math.round(temp)}°` : '-';


  return (
    <div 
      className="bg-white box-border content-stretch flex flex-col gap-4 items-start justify-start px-3 py-4 relative rounded-[20px] w-full cursor-pointer hover:shadow-lg transition-shadow"
      onClick={onClick}
    >
      <div aria-hidden="true" className="absolute border border-solid border-zinc-200 inset-0 pointer-events-none rounded-[20px]" />
      
      {/* 헤더 */}
      <div className="box-border content-stretch flex items-center justify-between px-1.5 py-0 relative shrink-0 w-full">
        <div className="content-stretch flex gap-1.5 items-start justify-start relative shrink-0">
          <div className="box-border content-stretch flex gap-2 items-center justify-start px-0 py-[3px] relative shrink-0">
            <div className="bg-[var(--color-gray-400)] relative rounded-[100px] shrink-0 size-5">
              {feed.author.profileImageUrl ? (
                <img 
                  src={feed.author.profileImageUrl} 
                  alt={feed.author.name} 
                  className="w-full h-full rounded-[100px] object-cover"
                />
              ) : (
                <img 
                  src={profileIcon} 
                  alt={feed.author.name} 
                  className="w-full h-full rounded-[100px] object-cover"
                />
              )}
            </div>
          </div>
          <div className="content-stretch flex flex-col gap-1 items-start justify-start relative shrink-0 w-[94px]">
            <div
                className="font-[var(--font-weight-bold)] leading-[0] not-italic relative shrink-0 text-[var(--color-gray-800)] text-[16px] tracking-[-0.4px] w-full cursor-pointer hover:underline"
                onClick={(e) => {
                  e.stopPropagation();
                  navigate(`/profiles?userId=${feed.author.userId}`);
                }}
            >
              <p className="leading-[normal] truncate">{feed.author.name}</p>
            </div>
            <div className="content-stretch flex font-[var(--font-weight-semibold)] gap-1 items-center justify-start leading-[0] not-italic relative shrink-0 text-[var(--color-gray-500)] text-[14px] text-nowrap tracking-[-0.35px] w-full">
              <div className="relative shrink-0">
                <p className="leading-[normal] text-nowrap whitespace-pre">{date}</p>
              </div>
              <div className="relative shrink-0">
                <p className="leading-[normal] text-nowrap whitespace-pre">{time}</p>
              </div>
            </div>
          </div>
        </div>
        <TooltipProvider>
          <Tooltip key={feed.id}>
            <TooltipTrigger asChild>
            <div className="bg-[var(--color-gray-100)] box-border content-stretch flex gap-[3px] items-center justify-start pl-2.5 pr-3.5 py-1.5 relative rounded-[10px] shrink-0">
              <div className="overflow-clip relative shrink-0 size-6">
                <WeatherIcon skyStatus={feed.weather.skyStatus} />
              </div>
              <div className="font-[var(--font-weight-bold)] leading-[0] not-italic relative shrink-0 text-[var(--color-gray-800)] text-[14px] text-center text-nowrap tracking-[-0.35px]">
                <p className="leading-[normal] whitespace-pre">
                  {displayTemp(feed.weather.temperature.current)}
                </p>
              </div>
            </div>
            </TooltipTrigger>
            <TooltipContent
                side="right"
                sideOffset={-10}
                align="start"
                alignOffset={25}

                className="bg-[rgba(12,12,13,0.74)] text-[#f7f7f8] font-semibold text-[14px] tracking-[-0.35px] px-3.5 py-3 rounded-[10px] flex flex-col gap-2 leading-none border-0"
            >
              <div className="whitespace-pre">날씨: {getSkyStatusText(feed.weather.skyStatus)}</div>
              <div className="whitespace-pre">평균: {displayTemp(feed.weather.temperature.current)}</div>
              <div className="whitespace-pre">최저: {displayTemp(feed.weather.temperature.min)}</div>
              <div className="whitespace-pre">최고: {displayTemp(feed.weather.temperature.max)}</div>
            </TooltipContent>
          </Tooltip>
        </TooltipProvider>

      </div>

      {/* 이미지 영역 */}
      <ImageLayout ootds={feed.ootds} />

      {/* 내용 */}
      <div className="box-border content-stretch flex gap-2 items-center justify-start px-0.5 py-0 relative shrink-0 w-full">
        <div className="font-[var(--font-weight-semibold)] leading-[0] not-italic relative text-[var(--color-gray-700)] text-[16px] tracking-[-0.4px] w-full">
          <p className="leading-[normal] line-clamp-3">{feed.content}</p>
        </div>
      </div>

      {/* 하단 액션 */}
      <div className="box-border content-stretch flex gap-3.5 items-center justify-start pb-2 pt-1 px-0.5 relative shrink-0 w-full">
        <button
          onClick={(e) => {
            e.stopPropagation();
            handleLike();
          }}
          className="content-stretch flex gap-0.5 items-end justify-start relative shrink-0 hover:opacity-70 transition-opacity"
        >
          <div className="overflow-clip relative shrink-0 size-4">
            <img 
              src={feed.likedByMe ? heartFilledIcon : heartIcon} 
              alt="좋아요" 
              className="block max-w-none size-full" 
            />
          </div>
          <div className="font-[var(--font-weight-semibold)] leading-[0] not-italic relative shrink-0 text-[var(--color-gray-400)] text-[14px] text-nowrap tracking-[-0.35px]">
            <p className="leading-[normal] whitespace-pre">좋아요</p>
          </div>
          <div className="font-[var(--font-weight-semibold)] leading-[0] not-italic relative shrink-0 text-[var(--color-gray-400)] text-[14px] text-nowrap tracking-[-0.35px]">
            <p className="leading-[normal] whitespace-pre">{feed.likeCount}</p>
          </div>
        </button>
        
        <div className="content-stretch flex gap-0.5 items-center justify-start relative shrink-0">
          <div className="overflow-clip relative shrink-0 size-4">
            <img src={commentIcon} alt="댓글" className="block max-w-none size-full" />
          </div>
          <div className="font-[var(--font-weight-semibold)] leading-[0] not-italic relative shrink-0 text-[var(--color-gray-400)] text-[14px] text-nowrap tracking-[-0.35px]">
            <p className="leading-[normal] whitespace-pre">댓글</p>
          </div>
          <div className="font-[var(--font-weight-semibold)] leading-[0] not-italic relative shrink-0 text-[var(--color-gray-400)] text-[14px] text-nowrap tracking-[-0.35px]">
            <p className="leading-[normal] whitespace-pre">{feed.commentCount}</p>
          </div>
        </div>
      </div>
    </div>
  );
}