import type { FeedDto } from '@/lib/api/types';
import {
  Dialog,
  DialogContent,
  DialogTitle,
} from '@/components/ui/dialog';
import FeedDetailLeftSection from './FeedDetailLeftSection';
import FeedDetailRightSection from './FeedDetailRightSection';

interface FeedDetailModalProps {
  feed: FeedDto | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export default function FeedDetailModal({ feed, open, onOpenChange }: FeedDetailModalProps) {
  if (!feed) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogTitle/>
      <DialogContent 
        className="max-w-none sm:max-w-none w-[918px] h-[800px] p-0 bg-white rounded-[20px] border border-[#e7e7e9] overflow-hidden"
        showCloseButton={false}
      >
        <div className="box-border content-stretch flex gap-5 items-start justify-start overflow-clip pl-0 pr-5 py-0 relative w-[918px] h-[800px] bg-white">
          {/* 왼쪽 섹션 - OOTD 캐러셀 (531px) */}
          <FeedDetailLeftSection feed={feed} />
          {/* 오른쪽 섹션 - 피드 정보 & 댓글 (367px) */}
          <FeedDetailRightSection feed={feed} onDelete={() => onOpenChange(false)} />
        </div>
        <div aria-hidden="true" className="absolute border border-[#e7e7e9] border-solid inset-0 pointer-events-none rounded-[20px]" />
      </DialogContent>
    </Dialog>
  );
}