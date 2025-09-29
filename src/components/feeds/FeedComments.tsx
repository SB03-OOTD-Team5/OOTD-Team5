import { useEffect, useState } from 'react';
import { createFeedComment } from '@/lib/api/feeds';
import { toast } from 'sonner';
import { useFeedCommentStore } from '@/lib/stores/useFeedCommentStore';
import { useAuthStore } from '@/lib/stores/useAuthStore';
import { useInfiniteScroll } from '@/lib/hooks/useInfiniteScroll';
import type { FeedDto } from '@/lib/api/types';
import profileIcon from '@/assets/icons/profile.svg';

interface FeedCommentsProps {
  feed: FeedDto;
}

export default function FeedComments({ feed }: FeedCommentsProps) {
  const { data: comments, loading, add, updateParams, fetchMore, hasNext } = useFeedCommentStore();
  const { data: auth } = useAuthStore();
  const [commentText, setCommentText] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    updateParams({ feedId: feed.id });
  }, [feed.id, updateParams]);

  // 무한 스크롤 설정
  const { ref: infiniteScrollRef } = useInfiniteScroll({
    onLoadMore: () => {
      fetchMore();
    },
    rootMargin: '50px',
    threshold: 0.1
  });

  // 댓글 작성
  const handleSubmitComment = async () => {
    if (!commentText.trim() || !auth) return;
    
    try {
      setSubmitting(true);
      const newComment = await createFeedComment(feed.id, {
        feedId: feed.id,
        authorId: auth.userDto.id,
        content: commentText.trim(),
      });
      
      add(newComment);
      setCommentText('');
      toast.success('댓글이 등록되었습니다.');
    } catch (error) {
      console.error(error);
      toast.error('댓글 등록에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  // Enter 키 처리
  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmitComment();
    }
  };

  // 날짜 포맷팅
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    return `${month}월 ${day}일`;
  };

  return (
    <div className="flex flex-col h-full w-full">
      {/* 좋아요 개수 */}
      <div className="content-stretch flex gap-3 items-start justify-start relative shrink-0">
        <div className="box-border content-stretch flex font-['SUIT:Bold',_sans-serif] gap-1 items-start justify-start leading-[0] not-italic pb-3.5 pt-1 px-0 relative shrink-0 text-[#34343d] text-[16px] text-nowrap tracking-[-0.4px]">
          <div className="relative shrink-0">
            <p className="leading-[normal] text-nowrap whitespace-pre">좋아요</p>
          </div>
          <div className="relative shrink-0">
            <p className="leading-[normal] text-nowrap whitespace-pre">{feed.likeCount}개</p>
          </div>
        </div>
      </div>

      {/* 댓글 목록 */}
      <div className="flex flex-col w-full overflow-y-auto min-h-[200px] mb-2.5" style={{maxHeight: 'calc(100% - 80px)'}}>
        {comments.length === 0 ? (
          <div className="flex items-center justify-center py-4">
            <span className="text-[#a9a9b1] text-[14px]">첫 댓글을 작성해보세요!</span>
          </div>
        ) : (
          <>
            {comments.map((comment) => (
              <div key={comment.id} className="box-border content-stretch flex items-center justify-between pb-5 pt-0 px-1.5 relative shrink-0 w-full">
                <div className="content-stretch flex gap-2.5 items-center justify-start relative shrink-0">
                  <div className="content-stretch flex gap-1.5 items-start justify-start relative shrink-0">
                    <div className="box-border content-stretch flex gap-2 items-center justify-start px-0 py-px relative shrink-0">
                      <div className="bg-[#a9a9b1] relative rounded-[100px] shrink-0 size-5">
                        {comment.author.profileImageUrl ? (
                          <img 
                            src={comment.author.profileImageUrl} 
                            alt={comment.author.name} 
                            className="w-full h-full rounded-[100px] object-cover"
                          />
                        ) : (
                          <img 
                            src={profileIcon} 
                            alt={comment.author.name} 
                            className="w-full h-full rounded-[100px] object-cover"
                          />
                        )}
                      </div>
                    </div>
                    <div className="content-stretch flex flex-col gap-2 items-start justify-start leading-[0] not-italic relative shrink-0 w-[280px]">
                      <div className="content-stretch flex gap-1 items-center justify-start relative shrink-0">
                        <div className="font-['SUIT:Bold',_sans-serif] relative shrink-0 text-[#575765] text-[16px] tracking-[-0.4px]">
                          <p className="leading-[normal] text-nowrap whitespace-pre">{comment.author.name}</p>
                        </div>
                        <div className="font-['SUIT:SemiBold',_sans-serif] relative shrink-0 text-[#808089] text-[14px] tracking-[-0.35px]">
                          <p className="leading-[normal] text-nowrap whitespace-pre">{formatDate(comment.createdAt)}</p>
                        </div>
                      </div>
                      <div className="font-['SUIT:SemiBold',_sans-serif] relative text-[#34343d] text-[16px] tracking-[-0.4px] break-words w-full">
                        <p className="leading-[1.4]">{comment.content}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
            
            {/* 무한 스크롤 트리거 - 항상 렌더링 */}
            <div ref={infiniteScrollRef} className="flex items-center justify-center py-4">
              {hasNext() ? (
                loading ? (
                  <span className="text-[#a9a9b1] text-[14px]">댓글을 더 불러오는 중...</span>
                ) : (
                  <div className="h-4 text-[#a9a9b1] text-[12px]">트리거 (hasNext: true)</div>
                )
              ) : (
                <div className="h-4 text-[#a9a9b1] text-[12px]">더 이상 댓글이 없습니다</div>
              )}
            </div>
          </>
        )}
      </div>

      {/* 댓글 입력 - 50px 고정 높이, 하단 고정 */}
      <div className="box-border content-stretch flex flex-col gap-2 items-start justify-center pb-5 pt-0 px-0 relative shrink-0 w-full mt-auto">
        <div className="bg-[#f7f7f8] h-[50px] relative rounded-[10px] shrink-0 w-full">
          <div className="box-border content-stretch flex gap-2 items-center justify-start overflow-clip px-5 py-[18px] relative size-full">
            <input
              type="text"
              value={commentText}
              onChange={(e) => setCommentText(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="댓글을 입력해주세요"
              disabled={submitting}
              className="flex-1 bg-transparent border-none outline-none font-['SUIT:SemiBold',_sans-serif] text-[16px] text-[#131316] placeholder:text-[#808089] tracking-[-0.4px] focus:ring-0 shadow-none"
            />
            {commentText.trim() && (
              <button
                onClick={handleSubmitComment}
                disabled={submitting}
                className="text-[#1e89f4] font-['SUIT:SemiBold',_sans-serif] text-[14px] hover:text-[#1570cc] disabled:opacity-50"
              >
                {submitting ? '등록 중...' : '등록'}
              </button>
            )}
          </div>
          <div aria-hidden="true" className="absolute border border-[#e7e7e9] border-solid inset-0 pointer-events-none rounded-[10px] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]" />
        </div>
      </div>
    </div>
  );
}