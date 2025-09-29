import { useEffect } from 'react';
import { useClothesStore } from '@/lib/stores/useClothesStore';
import { useProfileStore } from '@/lib/stores/useProfileStore';
import { useAuthStore } from '@/lib/stores/useAuthStore';
import { useInfiniteScroll } from '@/lib/hooks/useInfiniteScroll';
import ClothesItem from '@/components/closet/ClothesItem';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import type { ClothesType } from "@/lib/api";

interface UserClosetProps {
  userId: string;
}

export default function UserCloset({ userId }: UserClosetProps) {
  const { data: clothes, loading, fetch, fetchMore, params, updateParams } = useClothesStore();
  const { data: profileData, fetch: fetchProfile, updateParams: updateProfileParams } = useProfileStore();
  const currentUser = useAuthStore((state) => state.data);
  
  const isOwnProfile = userId === currentUser?.userDto?.id;
  const displayName = isOwnProfile ? currentUser?.userDto?.name : profileData?.name;

  // 무한 스크롤 설정
  const { ref } = useInfiniteScroll({
    onLoadMore: () => fetchMore()
  });

  useEffect(() => {
    // 옷장 데이터 가져오기
    updateParams({ ownerId: userId });
    fetch();
    
    // 프로필 데이터 가져오기 (사용자 이름을 위해)
    if (!isOwnProfile && userId) {
      updateProfileParams({ userId });
      fetchProfile();
    }
  }, [userId, updateParams, fetch, isOwnProfile, updateProfileParams, fetchProfile]);

  // 카테고리 매핑
  const categoryMap: Record<string, ClothesType | undefined> = {
    '전체': undefined,
    '상의': 'TOP',
    '하의': 'BOTTOM',
    '원피스': 'DRESS',
    '아우터': 'OUTER',
    '속옷': 'UNDERWEAR',
    '액세서리': 'ACCESSORY',
    '신발': 'SHOES',
    '양말': 'SOCKS',
    '모자': 'HAT',
    '가방': 'BAG',
    '스카프': 'SCARF',
    '기타': 'ETC'
  };

  const reverseCategoryMap: Record<ClothesType | 'ALL', string> = {
    'ALL': '전체',
    'TOP': '상의',
    'BOTTOM': '하의',
    'DRESS': '원피스',
    'OUTER': '아우터',
    'UNDERWEAR': '속옷',
    'ACCESSORY': '액세서리',
    'SHOES': '신발',
    'SOCKS': '양말',
    'HAT': '모자',
    'BAG': '가방',
    'SCARF': '스카프',
    'ETC': '기타'
  };

  const handleCategoryChange = (category: string) => {
    const clothesType = categoryMap[category];
    updateParams({ typeEqual: clothesType });
    fetch();
  };

  if (loading && clothes.length === 0) {
    return (
      <div className="bg-neutral-50 w-[456px] h-full rounded-bl-[30px] rounded-tl-[30px] border border-[#e7e7e9] shadow-[-5px_0px_14px_0px_rgba(46,46,59,0.04)]">
        <div className="flex flex-col gap-5 p-[30px] pb-10 pt-5 h-full">
          {/* 헤더 스켈레톤 */}
          <div className="flex items-center justify-between">
            <div className="h-6 bg-gray-200 rounded w-32 animate-pulse"></div>
            <div className="h-[46px] bg-gray-200 rounded-[100px] w-[102px] animate-pulse"></div>
          </div>
          
          {/* 옷 그리드 스켈레톤 */}
          <div className="flex flex-col gap-10">
            {Array.from({ length: 3 }).map((_, rowIndex) => (
              <div key={rowIndex} className="flex gap-5">
                {Array.from({ length: 2 }).map((_, colIndex) => (
                  <div key={colIndex} className="basis-0 grow flex flex-col gap-3 min-h-px min-w-px">
                    <div className="aspect-square bg-gray-200 rounded-[16px] animate-pulse"></div>
                    <div className="h-4 bg-gray-200 rounded w-3/4 animate-pulse"></div>
                    <div className="flex gap-1.5">
                      <div className="h-6 bg-gray-200 rounded w-8 animate-pulse"></div>
                      <div className="h-6 bg-gray-200 rounded w-12 animate-pulse"></div>
                    </div>
                  </div>
                ))}
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-neutral-50 w-[456px] h-full rounded-bl-[30px] rounded-tl-[30px] border border-[#e7e7e9] shadow-[-5px_0px_14px_0px_rgba(46,46,59,0.04)]">
      <div className="flex flex-col gap-5 p-[30px] pb-10 pt-5 h-full">
        {/* 헤더 */}
        <div className="flex items-center justify-between w-full">
          <h2 className="font-['SUIT:Bold',_sans-serif] text-[20px] text-[#212126] tracking-[-0.5px] leading-[0] not-italic">
            {displayName || '사용자'} 님의 옷장
          </h2>
          
          {/* 카테고리 필터 */}
          <Select
            value={params.typeEqual ? reverseCategoryMap[params.typeEqual] : '전체'}
            onValueChange={handleCategoryChange}
          >
            <SelectTrigger className="bg-white h-[46px] w-[140px] rounded-[100px] border border-[#d4d4d9] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)] px-[22px] py-3.5 font-['SUIT:SemiBold',_sans-serif] text-[16px] text-[#575765] tracking-[-0.4px]">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="전체">전체</SelectItem>
              <SelectItem value="상의">상의</SelectItem>
              <SelectItem value="하의">하의</SelectItem>
              <SelectItem value="원피스">원피스</SelectItem>
              <SelectItem value="아우터">아우터</SelectItem>
              <SelectItem value="속옷">속옷</SelectItem>
              <SelectItem value="액세서리">액세서리</SelectItem>
              <SelectItem value="신발">신발</SelectItem>
              <SelectItem value="양말">양말</SelectItem>
              <SelectItem value="모자">모자</SelectItem>
              <SelectItem value="가방">가방</SelectItem>
              <SelectItem value="스카프">스카프</SelectItem>
              <SelectItem value="기타">기타</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* 옷 목록 */}
        <div className="flex-1 overflow-auto">
          {clothes.length === 0 ? (
            <div className="flex items-center justify-center h-full">
              <div className="text-center text-gray-500">
                <p className="text-lg">옷이 없습니다.</p>
                <p className="text-sm mt-2">다른 카테고리를 선택해보세요.</p>
              </div>
            </div>
          ) : (
            <div className="flex flex-col gap-10">
              {/* 2열 그리드로 옷들을 표시 */}
              {Array.from({ length: Math.ceil(clothes.length / 2) }).map((_, rowIndex) => (
                <div key={rowIndex} className="flex gap-5">
                  {clothes.slice(rowIndex * 2, rowIndex * 2 + 2).map((item) => (
                    <div key={item.id} className="basis-0 grow min-h-px min-w-px">
                      <ClothesItem
                        clothes={item}
                        isOwner={isOwnProfile}
                      />
                    </div>
                  ))}
                  {/* 홀수 개수일 때 빈 공간 채우기 */}
                  {clothes.slice(rowIndex * 2, rowIndex * 2 + 2).length === 1 && (
                    <div className="basis-0 grow"></div>
                  )}
                </div>
              ))}

              {/* 무한 스크롤 로딩 중인 경우 하단에 스켈레톤 추가 */}
              {loading && clothes.length > 0 && (
                <div className="flex gap-5">
                  {Array.from({ length: 2 }).map((_, colIndex) => (
                    <div key={colIndex} className="basis-0 grow flex flex-col gap-3 min-h-px min-w-px">
                      <div className="aspect-square bg-gray-200 rounded-[16px] animate-pulse"></div>
                      <div className="h-4 bg-gray-200 rounded w-3/4 animate-pulse"></div>
                      <div className="flex gap-1.5">
                        <div className="h-6 bg-gray-200 rounded w-8 animate-pulse"></div>
                        <div className="h-6 bg-gray-200 rounded w-12 animate-pulse"></div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
          
          {/* 무한 스크롤 트리거 영역 */}
          <div ref={ref} className="w-full h-1 mt-8" />
        </div>
      </div>
    </div>
  );
}