import { useState, useRef, useEffect } from 'react';
import { Dialog, DialogContent, DialogOverlay, DialogTitle, DialogClose } from '@/components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { useImageUpload } from '@/hooks/useImageUpload';
import { useClothesStore } from '@/lib/stores/useClothesStore';
import { useClothesAttributeDefStore } from '@/lib/stores/useClothesAttributeDefStore';
import { updateClothes } from '@/lib/api/clothes';
import { toast } from 'sonner';
import type { ClothesDto, ClothesType, ClothesAttributeDto } from '@/lib/api/types';

import closeIcon from '@/assets/icons/ic_X.svg'
import emptyImageIcon from '@/assets/icons/empty image.svg'

const CLOTHES_TYPES = [
  { label: '상의', value: 'TOP' as ClothesType },
  { label: '하의', value: 'BOTTOM' as ClothesType },
  { label: '원피스', value: 'DRESS' as ClothesType },
  { label: '아우터', value: 'OUTER' as ClothesType },
  { label: '속옷', value: 'UNDERWEAR' as ClothesType },
  { label: '신발', value: 'SHOES' as ClothesType },
  { label: '악세서리', value: 'ACCESSORY' as ClothesType },
  { label: '양말', value: 'SOCKS' as ClothesType },
  { label: '모자', value: 'HAT' as ClothesType },
  { label: '가방', value: 'BAG' as ClothesType },
  { label: '스카프', value: 'SCARF' as ClothesType },
  { label: '기타', value: 'ETC' as ClothesType },
];

interface EditClothesModalProps {
  open: boolean;
  onClose: () => void;
  clothes: ClothesDto | null;
}

export default function EditClothesModal({ open, onClose, clothes }: EditClothesModalProps) {
  const { update } = useClothesStore();
  const { data: attributeDefs, fetch: fetchAttributes } = useClothesAttributeDefStore();
  const [loading, setLoading] = useState(false);
  const { selectedImage, imagePreview, handleImageChange, clearImage } = useImageUpload();
  
  const [formData, setFormData] = useState({
    name: '',
    type: '' as ClothesType,
    attributes: [] as ClothesAttributeDto[]
  });
  const [selectedAttributes, setSelectedAttributes] = useState<Record<string, string>>({});
  const fileInputRef = useRef<HTMLInputElement>(null);

  // 의상 속성 정의 로드
  useEffect(() => {
    if (open) {
      fetchAttributes();
    }
  }, [open, fetchAttributes]);

  // 모달이 열릴 때 폼 데이터 초기화
  useEffect(() => {
    if (open && clothes) {
      setFormData({
        name: clothes.name,
        type: clothes.type,
        attributes: clothes.attributes
      });
      
      // 기존 속성들을 selectedAttributes로 변환
      const existingAttributes: Record<string, string> = {};
      clothes.attributes.forEach(attr => {
        if (attr.definitionId) {
          existingAttributes[attr.definitionId] = attr.value;
        }
      });
      setSelectedAttributes(existingAttributes);
      
      clearImage();
    }
  }, [open, clothes, clearImage]);

  // 선택된 속성들을 ClothesAttributeDto 배열로 변환
  const convertSelectedAttributesToDto = (): ClothesAttributeDto[] => {
    if (!selectedAttributes) return [];

    return Object.entries(selectedAttributes)
      .filter(([, value]) => value && value.trim())
      .map(([definitionId, value]) => ({definitionId, value}));
  };

  // 수정 저장
  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!clothes || !formData.name || !formData.type) return;

    setLoading(true);
    try {
      const attributes = convertSelectedAttributesToDto();
      const updatedClothes = await updateClothes(clothes.id, {
        name: formData.name,
        type: formData.type,
        attributes: attributes
      }, selectedImage || undefined);
      
      update(updatedClothes.id, updatedClothes);
      toast.success('옷이 성공적으로 수정되었습니다.');
      handleClose();
    } catch (error) {
      console.error('옷 수정 실패:', error);
      toast.error('옷 수정에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    clearImage();
    setFormData({ name: '', type: '' as ClothesType, attributes: [] });
    setSelectedAttributes({});
    onClose();
  };

  if (!clothes) return null;

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogOverlay className="bg-black/50" />
      <DialogContent className="max-w-[550px] p-0 bg-transparent border-none" showCloseButton={false}>
        <div className="bg-white box-border content-stretch flex flex-col gap-6 items-center justify-start p-[30px] relative rounded-[20px] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)] w-full max-h-[90vh] overflow-y-auto">
            {/* 헤더 */}
            <div className="content-stretch flex items-center justify-between relative shrink-0 w-full">
              <div className="w-[30px]" />
              <DialogTitle className="font-bold leading-none not-italic relative shrink-0 text-gray-800 text-[22px] text-nowrap tracking-[-0.55px]">
                옷 수정
              </DialogTitle>
              <DialogClose asChild>
                <button className="overflow-clip relative shrink-0 size-[30px] hover:bg-gray-100 rounded transition-colors">
                  <div className="absolute inset-[20.834%]">
                    <img alt="닫기" className="block max-w-none size-full" src={closeIcon} />
                  </div>
                </button>
              </DialogClose>
            </div>

            {/* 이미지 업로드 */}
            <div className="box-border content-stretch flex flex-col items-end justify-start pb-[26px] pt-0 px-0 relative shrink-0 w-[100px] self-center">
              <div className="aspect-square bg-gray-300 mb-[-26px] relative rounded-[100px] shrink-0 w-full overflow-hidden cursor-pointer" onClick={() => fileInputRef.current?.click()}>
                {imagePreview ? (
                  <img src={imagePreview} alt="미리보기" className="w-full h-full object-cover" />
                ) : clothes.imageUrl ? (
                  <img src={clothes.imageUrl} alt={clothes.name} className="w-full h-full object-cover" />
                ) : (
                  <div className="aspect-square overflow-clip relative size-full flex items-center justify-center">
                    <div className="absolute inset-[29.167%] overflow-clip">
                      <div className="absolute inset-[8.333%]">
                        <img alt="사진 업로드" className="block max-w-none size-full" src={emptyImageIcon} />
                      </div>
                    </div>
                  </div>
                )}
              </div>
              <button 
                onClick={() => fileInputRef.current?.click()}
                className="bg-blue-500 hover:bg-blue-600 box-border content-stretch flex flex-col gap-2 items-center justify-center mb-[-26px] overflow-clip px-3 py-1.5 relative rounded-[100px] shrink-0 transition-colors"
              >
                <div className="flex flex-col font-bold justify-center leading-none not-italic relative shrink-0 text-[16px] text-white tracking-[-0.4px] w-full">
                  <p className="leading-normal">변경</p>
                </div>
              </button>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleImageChange}
                className="hidden"
              />
            </div>

            <form onSubmit={handleSave} className="w-full flex flex-col gap-6">
              {/* 이름 */}
              <div className="content-stretch flex flex-col gap-2.5 items-start justify-start relative shrink-0 w-full">
                <div className="font-bold leading-none not-italic relative shrink-0 text-gray-500 text-[14px] tracking-[-0.35px] w-full">
                  <p className="leading-normal">이름</p>
                </div>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                  placeholder="이름을 입력해주세요"
                  className="bg-white box-border content-stretch flex h-[46px] items-center justify-between px-5 py-3.5 relative rounded-[12px] shrink-0 w-full border border-gray-200 shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)] focus:outline-none focus:border-blue-500"
                  required
                />
              </div>

              {/* 종류 */}
              <div className="content-stretch flex flex-col gap-2.5 items-start justify-start relative shrink-0 w-full">
                <div className="font-bold leading-none not-italic relative shrink-0 text-gray-500 text-[14px] tracking-[-0.35px] w-full">
                  <p className="leading-normal">종류</p>
                </div>
                <Select value={formData.type} onValueChange={(value) => setFormData(prev => ({ ...prev, type: value as ClothesType }))} required>
                  <SelectTrigger className="bg-white box-border content-stretch flex h-[46px] items-center justify-between px-5 py-3.5 relative rounded-[12px] shrink-0 w-full border border-gray-200 shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]">
                    <SelectValue placeholder="종류를 선택해주세요" />
                  </SelectTrigger>
                  <SelectContent>
                    {CLOTHES_TYPES.map((type) => (
                      <SelectItem key={type.value} value={type.value} className="cursor-pointer hover:bg-gray-50 hover:shadow-sm transition-colors">
                        {type.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              {/* 의상 속성 Select들 */}
              {attributeDefs && attributeDefs.length > 0 && (
                <div className="content-stretch flex flex-col gap-5 items-start justify-start relative shrink-0 w-full">
                  {attributeDefs.map((attrDef) => (
                    <div key={attrDef.id} className="content-stretch flex flex-col gap-2.5 items-start justify-start relative shrink-0 w-full">
                      <div className="font-bold leading-none not-italic relative shrink-0 text-gray-500 text-[14px] tracking-[-0.35px] w-full">
                        <p className="leading-normal truncate">{attrDef.name}</p>
                      </div>
                      <Select 
                        value={selectedAttributes[attrDef.id] || ""} 
                        onValueChange={(value) => 
                          setSelectedAttributes(prev => ({ ...prev, [attrDef.id]: value }))
                        }
                      >
                        <SelectTrigger className="bg-white box-border content-stretch flex h-[46px] items-center justify-between px-5 py-3.5 relative rounded-[12px] shrink-0 w-full border border-gray-200 shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]">
                          <SelectValue placeholder={`${attrDef.name}를 선택해주세요`} />
                        </SelectTrigger>
                        <SelectContent>
                          {attrDef.selectableValues.map((value) => (
                            <SelectItem key={value} value={value} className="cursor-pointer hover:bg-gray-50 hover:shadow-sm transition-colors">
                              {value}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                  ))}
                </div>
              )}

              {/* 하단 버튼 */}
              <div className="box-border content-stretch flex gap-3 items-center justify-end pb-0 pt-1.5 px-0 relative shrink-0 w-full">
                <Button
                  type="button"
                  onClick={handleClose}
                  variant="secondary"
                >
                  취소
                </Button>
                <Button
                  type="submit"
                  disabled={loading || !formData.name || !formData.type}
                  variant="primary"
                >
                  {loading ? '저장 중...' : '저장'}
                </Button>
              </div>
            </form>
          </div>
      </DialogContent>
    </Dialog>
  );
}