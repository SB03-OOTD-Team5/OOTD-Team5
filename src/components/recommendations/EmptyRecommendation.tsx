import hangerIcon from '@/assets/icons/il_hanger.svg';
import {useNavigate} from "react-router-dom";

export default function EmptyRecommendation() {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col gap-3.5 items-center justify-center relative w-full min-h-[400px]">
      {/* 행거 아이콘 */}
      <div className="overflow-clip relative shrink-0 size-[60px]">
        <img alt="옷장이 비어있습니다" className="block max-w-none size-full" src={hangerIcon} />
      </div>
      
      {/* 안내 메시지 */}
      <div className="flex flex-col font-bold justify-center leading-none not-italic relative shrink-0 text-gray-400 text-2xl text-center text-nowrap tracking-[-0.6px]">
        <p className="leading-normal whitespace-pre">OOTD 추천을 위해 옷장에 옷을 등록해보세요</p>
      </div>
      
      {/* 옷 등록 버튼 */}
      <button
          onClick={() => navigate('/closet')}
          className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-3 px-6 rounded-xl transition-colors mt-4"
      >
        옷 등록하러 가기
      </button>
    </div>
  );
}