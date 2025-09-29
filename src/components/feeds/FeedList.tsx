import {useState} from 'react';
import {useFeedStore} from '@/lib/stores/useFeedStore';
import {useInfiniteScroll} from '@/lib/hooks/useInfiniteScroll';
import FeedCard from './FeedCard';
import FeedCardSkeleton from './FeedCardSkeleton';
import FeedEmptyState from './FeedEmptyState';
import FeedDetailModal from './FeedDetailModal';
import type {FeedDto} from '@/lib/api/types';


export default function FeedList() {
  const { data: feeds, loading, fetchMore } = useFeedStore();
  const [selectedFeed, setSelectedFeed] = useState<FeedDto | null>(null);
  const [modalOpen, setModalOpen] = useState(false);

  const handleFeedClick = (feed: FeedDto) => {
    setSelectedFeed(feed);
    setModalOpen(true);
  };

  const handleModalClose = () => {
    setModalOpen(false);
    setTimeout(() => setSelectedFeed(null), 300); // 애니메이션 완료 후 상태 정리
  };

  // 무한 스크롤 설정
  const { ref } = useInfiniteScroll({
    onLoadMore: () => fetchMore()
  });

  return (
    <div className="h-full overflow-y-auto">
      {loading && feeds.length === 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 p-4">
          {Array.from({ length: 8 }).map((_, index) => (
            <FeedCardSkeleton key={`skeleton-${index}`} />
          ))}
        </div>
      ) : feeds.length === 0 ? (
        <FeedEmptyState />
      ) : (
        <div className="p-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 w-full">
            {feeds.map((feed) => (
              <FeedCard 
                key={feed.id} 
                feed={feed} 
                onClick={() => handleFeedClick(feed)}
              />
            ))}

            {/* 무한 스크롤 로딩 중인 경우 하단에 스켈레톤 추가 */}
            {loading && feeds.length > 0 && 
              Array.from({ length: 4 }).map((_, index) => (
                <FeedCardSkeleton key={`loading-skeleton-${index}`} />
              ))
            }
          </div>

          {/* 무한 스크롤 트리거 영역 */}
          <div ref={ref} className="w-full h-1 mt-8" />
        </div>
      )}

      {/* 피드 상세 모달 */}
      <FeedDetailModal 
        feed={selectedFeed}
        open={modalOpen}
        onOpenChange={handleModalClose}
      />
    </div>
  );
}