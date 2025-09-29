import {useState} from 'react';
import {Dialog, DialogContent, DialogOverlay} from '@/components/ui/dialog';
import {useAuthStore} from '@/lib/stores/useAuthStore';
import {useWeatherStore} from '@/lib/stores/useWeatherStore';
import {useRecommendationStore} from '@/lib/stores/useRecommendationStore';
import {createFeed} from '@/lib/api/feeds';
import {toast} from 'sonner';
import type {FeedDto} from "@/lib/api";

// Figma assets
import closeIcon from '@/assets/icons/ic_X.svg';

interface AddFeedModalProps {
  open: boolean;
  onClose: () => void;
  onCreated: (feed: FeedDto) => void;
}

export default function AddFeedModal({ open, onClose, onCreated }: AddFeedModalProps) {
  const { data: auth } = useAuthStore();
  const { selectedWeather } = useWeatherStore();
  const { data: recommendation } = useRecommendationStore();

  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!auth?.userDto.id || !selectedWeather || !recommendation?.clothes) {
      toast.error('필요한 정보가 없습니다.');
      return;
    }

    if (!content.trim()) {
      toast.error('OOTD에 대한 설명을 입력해주세요.');
      return;
    }

    setLoading(true);
    
    try {
      const clothesIds = recommendation.clothes.map(ootd => ootd.clothesId);
      
      const created = await createFeed({
        authorId: auth.userDto.id,
        weatherId: selectedWeather.id,
        clothesIds,
        content: content.trim()
      });

      // 성공 toast에 버튼 추가
      toast.success('OOTD 등록이 완료되었습니다.', {
        action: {
          label: '등록된 피드 확인',
          onClick: () => {
            onCreated(created);
          }
        }
      });
      
      // 모달 닫고 초기화
      setContent('');
      onClose();
    } catch (error) {
      console.error('Feed creation failed:', error);
      toast.error('OOTD 등록을 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    onClose();
  };

  return (
    <Dialog open={open} onOpenChange={handleCancel}>
      <DialogOverlay className="bg-[rgba(19,19,22,0.5)]" />
      <DialogContent className="bg-white box-border content-stretch flex flex-col gap-6 items-center justify-center overflow-clip p-[30px] rounded-[30px] w-[733px] max-w-none" showCloseButton={false}>
        {/* 헤더 */}
        <div className="content-stretch flex items-center justify-between relative shrink-0 w-full">
          <div className="content-stretch flex gap-2 items-center justify-start shrink-0" />
          <div className="font-bold leading-[0] not-italic relative shrink-0 text-[#212126] text-[22px] text-nowrap tracking-[-0.55px]">
            <p className="leading-[normal] whitespace-pre">OOTD 등록하기</p>
          </div>
          <button
            onClick={handleCancel}
            className="overflow-clip relative shrink-0 size-[30px] flex items-center justify-center hover:bg-gray-100 rounded transition-colors"
          >
            <img src={closeIcon} alt="닫기" className="size-6" />
          </button>
        </div>

        {/* 텍스트 에리어 */}
        <div className="bg-white box-border content-stretch flex gap-2 h-[120px] items-start justify-start px-5 py-3.5 relative rounded-[12px] shrink-0 w-full border border-[#e7e7e9] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]">
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="OOTD에 대해 설명해주세요"
            className="w-full h-full resize-none border-none outline-none bg-transparent font-semibold text-[16px] tracking-[-0.4px] text-[#212126] placeholder-[#a9a9b1]"
          />
        </div>

        {/* 버튼들 */}
        <div className="content-stretch flex gap-3 items-center justify-end relative shrink-0 w-full">
          <button
            onClick={handleCancel}
            className="bg-[#f7f7f8] box-border content-stretch flex gap-1.5 h-[46px] items-center justify-center px-[18px] py-2.5 relative rounded-[12px] shrink-0 hover:bg-gray-200 transition-colors"
          >
            <div className="font-bold leading-[0] not-italic relative shrink-0 text-[#575765] text-[18px] text-nowrap tracking-[-0.45px]">
              <p className="leading-[normal] whitespace-pre">취소</p>
            </div>
          </button>
          <button
            onClick={handleSubmit}
            disabled={loading || !content.trim()}
            className="bg-[#1e89f4] box-border content-stretch flex gap-1.5 h-[46px] items-center justify-center px-[18px] py-2.5 relative rounded-[12px] shrink-0 hover:bg-[#1e89f4]/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <div className="font-bold leading-[0] not-italic relative shrink-0 text-[18px] text-nowrap text-white tracking-[-0.45px]">
              <p className="leading-[normal] whitespace-pre">{loading ? '등록 중...' : '등록'}</p>
            </div>
          </button>
        </div>
      </DialogContent>
    </Dialog>

  );
}