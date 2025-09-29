interface ClosetToggleButtonProps {
  isOpen: boolean;
  onToggle: () => void;
}

export default function ClosetToggleButton({ isOpen, onToggle }: ClosetToggleButtonProps) {
  return (
    <button
      onClick={onToggle}
      className="bg-[#34343d] rounded-bl-[20px] rounded-tl-[20px] border-[1px_0px_1px_1px] border-zinc-200 shadow-[-2px_0px_10px_0px_rgba(244,244,245,0.04)] flex flex-col items-center justify-center relative hover:bg-[#2a2a30] hover:shadow-[-2px_0px_14px_0px_rgba(244,244,245,0.08)] transition-all duration-200 ease-out active:scale-95"
      style={{
        width: '44px',
        height: isOpen ? '88px' : '147px',
        gap: '12px',
        transition: 'height 300ms ease-out, background-color 200ms ease-out, box-shadow 200ms ease-out, transform 100ms ease-out'
      }}
    >
      {/* 세로 텍스트 */}
      <div className="flex items-center justify-center">
        <div className="rotate-[270deg] whitespace-nowrap transition-opacity duration-200">
          <span className="font-['SUIT:Bold',_sans-serif] text-[16px] text-white tracking-[-0.4px] not-italic">
            {isOpen ? '닫기' : '옷장 열기'}
          </span>
        </div>
      </div>

    </button>
  );
}