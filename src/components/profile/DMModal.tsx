import {useCallback, useEffect, useRef, useState} from 'react';
import {Dialog, DialogContent} from '@/components/ui/dialog';
import profileIcon from '@/assets/icons/profile.svg';

import sendIcon from '@/assets/icons/ic_send.svg';
import {useWebSocketStore} from "@/lib/stores/websocketStore.ts";
import {useAuthStore} from "@/lib/stores/useAuthStore.ts";
import {useDirectMessageStore} from "@/lib/stores/useDirectMessageStore.ts";
import {useInfiniteScroll} from "@/lib/hooks/useInfiniteScroll.ts";

interface DMModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  targetUser: {
    id: string;
    name: string;
    profileImageUrl?: string;
  } | null;
}

export default function DMModal({ open, onOpenChange, targetUser }: DMModalProps) {
  const { send, isConnected, subscribe } = useWebSocketStore();
  const { data: auth } = useAuthStore();
  const { data: messages, add, updateParams, clearData: clearMessages, fetchMore, loading } = useDirectMessageStore();

  const [content, setContent] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const messagesContainerRef = useRef<HTMLDivElement>(null);


  const {ref} = useInfiniteScroll({
    onLoadMore: () => fetchMore(),
    rootMargin: '10px',
    threshold: 1
  });

  const isFirstMessage = messages.length === 0;

  // 스크롤을 맨 하단으로 이동하는 함수
  const scrollToBottom = useCallback((smooth = true) => {
    messagesEndRef.current?.scrollIntoView({ 
      behavior: smooth ? 'smooth' : 'instant',
      block: 'end'
    });
  }, []);

  useEffect(() => {
    if (open && targetUser?.id) {
      updateParams({userId: targetUser.id});
    }
  }, [targetUser?.id, updateParams, open]);

  useEffect(() => {
    if (!auth || !targetUser) return;
    const destination = resolveDestination(auth.userDto.id, targetUser.id);
    subscribe(destination, (message) => {
      add(message);
    });
  }, [subscribe, add, auth, targetUser]);

  const resolveDestination = useCallback((senderId: string, receiverId: string) => {
    let dest = '/sub/direct-messages_';
    if (senderId.localeCompare(receiverId) < 0) {
      dest = dest.concat(senderId).concat('_').concat(receiverId);
    } else {
      dest = dest.concat(receiverId).concat('_').concat(senderId);
    }
    return dest;
  }, []);

  const sendMessage = useCallback(
      async () => {
        if (!isConnected || !auth || !targetUser || !content.trim()) return;
        const message = {
          senderId: auth.userDto.id,
          receiverId: targetUser.id,
          content: content.trim(),
        };
        send('/pub/direct-messages_send', message);
        setContent(''); // 메시지 전송 후 입력 필드 초기화
      },
      [isConnected, auth, targetUser, content, send],
  );

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  // 모달이 열릴 때 스크롤을 하단으로
  useEffect(() => {
    if (open && messages.length > 0) {
      scrollToBottom(false)
    }
  }, [open, scrollToBottom, messages.length]);

  // 모달이 닫힐 때 데이터 정리
  useEffect(() => {
    if (!open) {
      setContent('');
      clearMessages();
    }
  }, [open, clearMessages]);

  const formatTimeAgo = (createdAt: string) => {
    const now = new Date();
    const created = new Date(createdAt);
    const diffMs = now.getTime() - created.getTime();
    const diffMinutes = Math.floor(diffMs / (1000 * 60));

    if (diffMinutes < 1) return '방금 전';
    if (diffMinutes < 60) return `${diffMinutes}분 전`;

    const diffHours = Math.floor(diffMinutes / 60);
    if (diffHours < 24) return `${diffHours}시간 전`;

    const diffDays = Math.floor(diffHours / 24);
    if (diffDays < 7) return `${diffDays}일 전`;

    return created.toLocaleDateString('ko-KR');
  };

  if (!targetUser) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent 
        className="bg-white w-[600px] h-[510px] max-w-[min(600px,90vw)] max-h-[min(510px,85vh)] p-0 gap-0 rounded-[30px] border-0 shadow-lg flex overflow-hidden"
        showCloseButton={false}
      >
        <div className="flex flex-col h-full w-full">
          {/* 헤더 */}
          <div className="flex gap-2 items-center px-5 py-3 border-b border-[#e7e7e9]">
            <div className="bg-[#a9a9b1] relative rounded-[100px] shrink-0 size-[30px] overflow-hidden">
              {targetUser.profileImageUrl ? (
                <img 
                  src={targetUser.profileImageUrl} 
                  alt={targetUser.name} 
                  className="w-full h-full object-cover rounded-[100px]"
                />
              ) : (
                <img 
                  src={profileIcon} 
                  alt={targetUser.name} 
                  className="w-full h-full object-cover rounded-[100px]"
                />
              )}
              <div aria-hidden="true" className="absolute border-[#a9a9b1] border-[1.5px] border-solid inset-0 pointer-events-none rounded-[100px] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]" />
            </div>
            <div className="font-['SUIT:SemiBold',_sans-serif] text-[#34343d] text-[18px] tracking-[-0.45px] leading-[0] not-italic">
              <p className="leading-[normal] whitespace-pre">{targetUser.name}</p>
            </div>
          </div>

          {/* 메시지 영역 */}
          <div className="flex-1 overflow-hidden px-5">
            {(loading && messages.length === 0) || isFirstMessage ? (
              /* 첫 메시지 상태 또는 로딩 상태 */
              <div className="flex items-center justify-center h-full">
                <div className="font-['SUIT:Bold',_sans-serif] text-[#a9a9b1] text-[24px] text-center tracking-[-0.6px] leading-[1.6] not-italic">
                  <p>{targetUser.name} 님과의 대화를 시작해보세요</p>
                </div>
              </div>
            ) : (
              /* 기존 메시지들이 있는 상태 */
              <div ref={messagesContainerRef} className="h-full overflow-y-auto" id="messages-container">

                <div ref={ref} className="w-full h-1"/>
                
                {/* 로딩 스켈레톤 (무한 스크롤 중) */}
                {loading && messages.length > 0 && (
                  <div className="flex justify-center py-4">
                    <div className="flex gap-3 items-start w-full max-w-md">
                      <div className="bg-gray-200 rounded-[100px] size-[30px] animate-pulse" />
                      <div className="flex-1">
                        <div className="bg-gray-200 rounded-[16px] h-12 w-3/4 animate-pulse" />
                      </div>
                    </div>
                  </div>
                )}
                
                <div className="flex flex-col gap-6 py-4">
                  {/* 날짜 표시 */}
                  <div className="font-['SUIT:SemiBold',_sans-serif] text-[#808089] text-[14px] text-center tracking-[-0.35px] leading-[0] not-italic">
                    <p className="leading-[normal]">
                      {new Date().toLocaleDateString('ko-KR', { 
                        year: '2-digit', 
                        month: 'long', 
                        day: 'numeric' 
                      })}
                    </p>
                  </div>

                  {/* 메시지 목록 - 최신 메시지가 아래로 */}
                  <div className="flex flex-col gap-[18px]">
                    {messages.slice().sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()).map((msg) => (
                      <div key={msg.id}>
                        {msg.sender.userId === auth?.userDto.id ? (
                          /* 내가 보낸 메시지 */
                          <div className="flex gap-3 items-end justify-end">
                            <div className="flex gap-2 items-center px-0 py-1.5">
                              <div className="font-['SUIT:SemiBold',_sans-serif] text-[#808089] text-[14px] tracking-[-0.35px] leading-[0] not-italic">
                                <p className="leading-[normal] whitespace-pre">{formatTimeAgo(msg.createdAt)}</p>
                              </div>
                            </div>
                            <div className="bg-[#1e89f4] px-[19px] py-3.5 rounded-[16px]">
                              <div className="font-['SUIT:SemiBold',_sans-serif] text-white text-[18px] tracking-[-0.45px] leading-[0] not-italic">
                                <p className="leading-[normal] whitespace-pre">{msg.content}</p>
                              </div>
                            </div>
                          </div>
                        ) : (
                          /* 상대방이 보낸 메시지 */
                          <div className="flex gap-3 items-start">
                            <div className="flex gap-2 items-center px-0 py-1">
                              <div className="bg-[#a9a9b1] relative rounded-[100px] shrink-0 size-[30px] overflow-hidden">
                                {targetUser.profileImageUrl ? (
                                  <img 
                                    src={targetUser.profileImageUrl} 
                                    alt={targetUser.name} 
                                    className="w-full h-full object-cover rounded-[100px]"
                                  />
                                ) : (
                                  <img 
                                    src={profileIcon} 
                                    alt={targetUser.name} 
                                    className="w-full h-full object-cover rounded-[100px]"
                                  />
                                )}
                                <div aria-hidden="true" className="absolute border-[#a9a9b1] border-[1.5px] border-solid inset-0 pointer-events-none rounded-[100px] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]" />
                              </div>
                            </div>
                            <div className="flex gap-3 items-end">
                              <div className="bg-[#f2f2f3] px-[18px] py-3.5 rounded-[16px] inline-block w-fit">
                                <div className="font-['SUIT:SemiBold',_sans-serif] text-[#212126] text-[18px] tracking-[-0.35px] leading-[0] not-italic">
                                  <p className="leading-[normal] whitespace-pre">{msg.content}</p>
                                </div>
                              </div>
                              <div className="flex gap-2 items-center px-0 py-1.5">
                                <div className="font-['SUIT:SemiBold',_sans-serif] text-[#808089] text-[14px] tracking-[-0.35px] leading-[0] not-italic">
                                  <p className="leading-[normal] whitespace-pre">{formatTimeAgo(msg.createdAt)}</p>
                                </div>
                              </div>
                            </div>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                  
                  {/* 스크롤 하단 마커 */}
                  <div ref={messagesEndRef} className="h-1" />
                </div>
              </div>
            )}
          </div>

          {/* 메시지 입력 영역 */}
          <div className="flex flex-col gap-2 items-center px-5 py-3">
            <div className="bg-white h-[54px] relative rounded-[100px] w-full border-[#e7e7e9] border-[1.5px]">
              <div className="flex items-center justify-between h-full pl-5 pr-3 py-3.5">
                <input
                  type="text"
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  onKeyPress={handleKeyPress}
                  placeholder="메시지 입력..."
                  className="flex-1 font-['SUIT:SemiBold',_sans-serif] text-[18px] text-[#212126] tracking-[-0.45px] bg-transparent border-none outline-none placeholder:text-[#a9a9b1]"
                />
                <button
                  onClick={sendMessage}
                  disabled={!content.trim()}
                  className="flex gap-2 items-center justify-center p-[10px] rounded-[100px] hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <div className="overflow-clip relative size-6">
                    <img 
                      src={sendIcon} 
                      alt="메시지 보내기" 
                      className="block max-w-none size-full" 
                    />
                  </div>
                </button>
              </div>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}