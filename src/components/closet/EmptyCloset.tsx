import hangerIcon from '@/assets/icons/il_hanger.svg';

interface Props {
  onClickRegister?: () => void; // 선택적으로 만들어서 다른 사용자 옷장에서는 숨김
}

export default function EmptyCloset({onClickRegister}: Props) {
  return (
    <div className="flex flex-col gap-3.5 items-center justify-center relative w-full min-h-[400px]">
      {/* 행거 아이콘 */}
      <div className="overflow-clip relative shrink-0 size-[60px]">
        <img alt="옷장이 비어있습니다" className="block max-w-none size-full" src={hangerIcon} />
      </div>
      
      {/* 안내 메시지 */}
      <div className="flex flex-col font-bold justify-center leading-none not-italic relative shrink-0 text-gray-400 text-2xl text-center text-nowrap tracking-[-0.6px]">
        <p className="leading-normal whitespace-pre">옷장이 비어있습니다.</p>
      </div>
      
      {/* 옷 등록 버튼 - 자신의 옷장일 때만 표시 */}
      {onClickRegister && (
        <button
            onClick={onClickRegister}
            className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-3 px-6 rounded-xl transition-colors mt-4"
        >
          옷 추가하기
        </button>
      )}
    </div>
  );
}