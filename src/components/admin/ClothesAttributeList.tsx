import {useEffect} from "react";
import {useClothesAttributeDefStore} from "@/lib/stores/useClothesAttributeDefStore";
import {type ClothesAttributeDefDto} from "@/lib/api/types";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {ChevronDown, ChevronUp} from "lucide-react";
import ClothesAttributeTag from "./ClothesAttributeTag";

interface ClothesAttributeListProps {
  onItemClick: (attribute: ClothesAttributeDefDto) => void;
}

export default function ClothesAttributeList({ onItemClick }: ClothesAttributeListProps) {
  const { data: attributes, loading, fetch, params, updateParams } = useClothesAttributeDefStore();

  // params가 변경될 때마다 fetch 호출
  useEffect(() => {
    fetch();
  }, [params, fetch]);

  const handleSort = (field: 'name' | 'createdAt') => {
    const currentSortBy = params.sortBy;
    const currentDirection = params.sortDirection;

    if (currentSortBy === field) {
      // 같은 필드 클릭 시 방향 토글
      const newDirection = currentDirection === 'DESCENDING' ? 'ASCENDING' : 'DESCENDING';
      updateParams({ sortDirection: newDirection });
    } else {
      // 다른 필드 클릭 시 해당 필드로 내림차순
      updateParams({ 
        sortBy: field, 
        sortDirection: 'DESCENDING' 
      });
    }
  };

  const getSortIcon = (field: 'name' | 'createdAt') => {
    if (params.sortBy !== field) {
      return <ChevronDown className="w-4 h-4 text-gray-400 opacity-0 group-hover:opacity-100 transition-opacity" />;
    }
    
    return params.sortDirection === 'DESCENDING' 
      ? <ChevronDown className="w-4 h-4 text-blue-500" />
      : <ChevronUp className="w-4 h-4 text-blue-500" />;
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  };

  if (loading) {
    return (
      <div className="px-5">
        <div className="flex items-center justify-center h-32 w-full">
          <p className="text-[#575765] text-[16px] font-['SUIT:SemiBold',_sans-serif]">로딩 중...</p>
        </div>
      </div>
    );
  }

  if (attributes.length === 0) {
    return (
      <div className="px-5">
        {/* 테이블 헤더 */}
        <div className="bg-[#f7f7f8] rounded-[8px] px-5 py-3 flex gap-[30px] items-center mb-0">
          <div className="w-[200px] text-[16px] font-['SUIT:Bold',_sans-serif] text-[#34343d] tracking-[-0.4px]">
            속성명
          </div>
          <div className="w-[200px] text-[16px] font-['SUIT:Bold',_sans-serif] text-[#34343d] tracking-[-0.4px]">
            생성일
          </div>
          <div className="w-[400px] text-[16px] font-['SUIT:Bold',_sans-serif] text-[#34343d] tracking-[-0.4px]">
            선택 가능 값
          </div>
        </div>
        
        {/* 빈 상태 */}
        <div className="flex flex-col items-center justify-center h-64 gap-3.5">
          <p className="text-[#a9a9b1] text-[20px] font-['SUIT:Bold',_sans-serif] tracking-[-0.5px]">
            설정된 속성값이 없습니다.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="px-5 h-full">
      <div className="rounded-lg h-full overflow-y-auto">
        <Table className="table-fixed w-full">
          <TableHeader>
            <TableRow className="bg-[#f7f7f8] h-11 hover:bg-[#f7f7f8] border-none">
              <TableHead
                className={`group px-5 py-3 text-[16px] font-['SUIT:Bold',_sans-serif] tracking-[-0.4px] w-1/4 cursor-pointer select-none ${
                  params.sortBy === 'name' ? 'text-blue-500' : 'text-[#34343d] hover:text-blue-500'
                }`}
                onClick={() => handleSort('name')}
              >
                <div className="flex items-center gap-1">
                  속성명
                  {getSortIcon('name')}
                </div>
              </TableHead>
              <TableHead
                className={`group px-5 py-3 text-[16px] font-['SUIT:Bold',_sans-serif] tracking-[-0.4px] w-1/4 cursor-pointer select-none ${
                  params.sortBy === 'createdAt' ? 'text-blue-500' : 'text-[#34343d] hover:text-blue-500'
                }`}
                onClick={() => handleSort('createdAt')}
              >
                <div className="flex items-center gap-1">
                  생성일
                  {getSortIcon('createdAt')}
                </div>
              </TableHead>
              <TableHead className="px-5 py-3 text-[16px] font-['SUIT:Bold',_sans-serif] text-[#34343d] tracking-[-0.4px] w-1/2">
                선택 가능 값
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {attributes.map((attribute) => (
              <TableRow
                key={attribute.id}
                className="h-14 hover:bg-gray-50 border-b border-[#e7e7e9] last:border-b-0 cursor-pointer"
                onClick={() => onItemClick(attribute)}
              >
                {/* 속성명 */}
                <TableCell className="px-5 py-0 text-[16px] font-['SUIT:SemiBold',_sans-serif] text-[#575765] tracking-[-0.4px]">
                  <div className="truncate">{attribute.name}</div>
                </TableCell>

                {/* 생성일 */}
                <TableCell className="px-5 py-0 text-[16px] font-['SUIT:SemiBold',_sans-serif] text-[#575765] tracking-[-0.4px]">
                  {formatDate(attribute.createdAt || new Date().toISOString())}
                </TableCell>

                {/* 선택 가능 값 */}
                <TableCell className="px-5 py-0">
                  <div className="relative">
                    <div className="flex gap-2 items-center overflow-x-auto">
                      {attribute.selectableValues.map((value, index) => (
                        <ClothesAttributeTag
                          key={index}
                          label={value}
                          variant="normal"
                        />
                      ))}
                      {/* 페이드 효과를 위한 스페이서 */}
                      <div className="shrink-0 w-32 h-1" />
                    </div>
                    {/* 오른쪽 페이드 효과 (더 있을 때만) */}
                    <div className="absolute right-0 top-0 h-full w-32 bg-gradient-to-l from-white to-transparent pointer-events-none" />
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}