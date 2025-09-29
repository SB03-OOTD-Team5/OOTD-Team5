import {useClothesStore} from '@/lib/stores/useClothesStore';
import type {ClothesDto} from '@/lib/api/types';
import ClothesItem from './ClothesItem';
import EmptyCloset from './EmptyCloset';
import {useInfiniteScroll} from "@/lib/hooks/useInfiniteScroll.ts";

interface ClothesGridProps {
  onAddClick?: () => void;
  isOwner?: boolean;
  onEditClothes?: (clothes: ClothesDto) => void;
  onDeleteClothes?: (clothes: ClothesDto) => void;
}

export default function ClothesGrid({ onAddClick, isOwner, onEditClothes, onDeleteClothes }: ClothesGridProps) {
  const { data: clothes, loading, isEmpty, fetchMore } = useClothesStore();
  const { ref: scrollRef } = useInfiniteScroll({
    onLoadMore: () => fetchMore()
  });

  return (
    <div className="h-full overflow-y-auto">
      {clothes.length === 0 ? (
        isEmpty() ? (
          <EmptyCloset onClickRegister={onAddClick} />
        ) : (
          <div className="flex items-center justify-center py-20">
            <div className="text-gray-500 text-center">
              <p className="text-lg">검색 결과가 없습니다.</p>
              <p className="text-sm mt-2">다른 카테고리를 선택해보세요.</p>
            </div>
          </div>
        )
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6 p-4">
          {clothes.map((item) => (
            <ClothesItem
              key={item.id}
              clothes={item}
              isOwner={isOwner}
              onEdit={onEditClothes}
              onDelete={onDeleteClothes}
            />
          ))}
          <div ref={scrollRef} className="h-1" />
        </div>
      )}
      {
        loading && (
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6 p-4">
              {Array.from({ length: 10 }).map((_, index) => (
                  <div key={index} className="flex flex-col gap-3">
                    <div className="aspect-square bg-gray-200 rounded-[16px] animate-pulse" />
                    <div className="h-5 bg-gray-200 rounded w-3/4 animate-pulse" />
                    <div className="flex gap-1.5">
                      <div className="h-6 bg-gray-200 rounded w-12 animate-pulse" />
                      <div className="h-6 bg-gray-200 rounded w-16 animate-pulse" />
                      <div className="h-6 bg-gray-200 rounded w-14 animate-pulse" />
                    </div>
                  </div>
              ))}
            </div>
        )
      }
    </div>
  );
}