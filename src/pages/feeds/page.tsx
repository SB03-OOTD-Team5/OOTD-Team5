import FeedFilters from '@/components/feeds/FeedFilters';
import FeedList from '@/components/feeds/FeedList';
import {useSearchParams} from "react-router-dom";
import {useEffect} from "react";
import {useFeedStore} from "@/lib/stores/useFeedStore.ts";

export default function FeedsPage() {
  const [searchParams] = useSearchParams();
  const authorIdEqual = searchParams.get('authorIdEqual');

  const {updateParams} = useFeedStore();

  useEffect(() => {
    if (authorIdEqual) {
      updateParams({authorIdEqual})
    } else {
      updateParams({authorIdEqual: undefined})
    }
  }, [authorIdEqual, updateParams]);

  return (
    <div className="flex flex-col h-full px-8 py-6">
      {/* 필터 영역 */}
      <div className="flex-shrink-0 mb-6">
        <FeedFilters />
      </div>
      
      {/* 피드 목록 */}
      <div className="flex-1 min-h-0">
        <FeedList />
      </div>
    </div>
  );
}