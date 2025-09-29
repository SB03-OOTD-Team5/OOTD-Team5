import {useState, useRef, useEffect, useCallback} from 'react';
import { Dialog, DialogContent, DialogOverlay, DialogTitle, DialogClose } from '@/components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useImageUpload } from '@/hooks/useImageUpload';
import { useAuthStore } from '@/lib/stores/useAuthStore';
import { useClothesStore } from '@/lib/stores/useClothesStore';
import { useClothesAttributeDefStore } from '@/lib/stores/useClothesAttributeDefStore';
import { createClothes, extractByUrl } from '@/lib/api/clothes';
import { toast } from 'sonner';
import type { ClothesType, ClothesAttributeDto } from '@/lib/api/types';

import closeIcon from '@/assets/icons/ic_X.svg'
import emptyImageIcon from '@/assets/icons/empty image.svg'
import leftArrowIcon from '@/assets/icons/ic_left.svg'

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

type ModalMode = 'form' | 'url';

interface AddClothesModalProps {
  open: boolean;
  onClose: () => void;
}

export default function AddClothesModal({ open, onClose }: AddClothesModalProps) {
  const { data: auth } = useAuthStore();
  const { add } = useClothesStore();
  const { data: attributeDefs, fetch: fetchAttributes } = useClothesAttributeDefStore();
  const [mode, setMode] = useState<ModalMode>('form');
  const [loading, setLoading] = useState(false);
  const { selectedImage, setSelectedImage, imagePreview, handleImageChange, clearImage } = useImageUpload();
  
  const [formData, setFormData] = useState({
    name: '',
    type: '' as ClothesType,
    attributes: [] as ClothesAttributeDto[]
  });
  const [selectedAttributes, setSelectedAttributes] = useState<Record<string, string>>({});
  const [url, setUrl] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  // 의상 속성 정의 로드
  useEffect(() => {
    if (open) {
      fetchAttributes();
    }
  }, [open, fetchAttributes]);

  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name || !formData.type) return;
    
    if (!auth?.userDto.id) {
      toast.error('로그인이 필요합니다.');
      return;
    }

    setLoading(true);
    try {
      const attributes = convertSelectedAttributesToDto();
      const newClothes = await createClothes({
        ownerId: auth.userDto.id,
        name: formData.name,
        type: formData.type,
        attributes: attributes
      }, selectedImage || undefined);
      
      add(newClothes);
      toast.success('옷장에 성공적으로 등록되었습니다.');
      handleClose();
    } catch (error) {
      console.error('옷 등록 실패:', error);
      toast.error('옷 등록에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleUrlSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!url.trim()) return;
    
    setLoading(true);
    try {
      const extracted = await extractByUrl(url.trim());
      setFormData({
        name: extracted.name || formData.name,
        type: extracted.type || formData.type,
        attributes: extracted.attributes || formData.attributes
      });
      if (extracted.imageUrl) {
        fetch(extracted.imageUrl)
        .then((res) => res.blob())
        .then((blob) => {
          const filename = extracted.imageUrl?.split('/').pop() || 'image';
          setSelectedImage(new File([blob], filename));
        });
      }
      // URL로 불러온 데이터를 폼에 채워주는 로직은 추후 구현
      toast.success('URL에서 의상 정보를 불러왔습니다.');
      setUrl('');
      setMode('form');
    } catch (error) {
      console.error('URL 불러오기 실패:', error);
      toast.error('URL에서 의상 정보를 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setMode('form');
    setFormData({ name: '', type: '' as ClothesType, attributes: [] });
    setSelectedAttributes({});
    clearImage();
    setUrl('');
    onClose();
  };

  // 선택된 속성들을 ClothesAttributeDto 배열로 변환
  const convertSelectedAttributesToDto = useCallback((): ClothesAttributeDto[] => {
    if (!selectedAttributes) return [];

    return Object.entries(selectedAttributes)
    .map(([definitionId, value]) => ({definitionId, value}));
  }, [selectedAttributes]);

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogOverlay className="bg-black/50" />
      <DialogContent className="max-w-[550px] p-0 bg-transparent border-none" showCloseButton={false}>
        {mode === 'form' ? (
          // 옷 추가 폼
          <div className="bg-white box-border content-stretch flex flex-col gap-6 items-center justify-start p-[30px] relative rounded-[20px] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)] w-full max-h-[90vh] overflow-y-auto">
            {/* 헤더 */}
            <div className="content-stretch flex items-center justify-between relative shrink-0 w-full">
              <div className="w-[30px]" />
              <DialogTitle className="font-bold leading-none not-italic relative shrink-0 text-gray-800 text-[22px] text-nowrap tracking-[-0.55px]">
                옷 추가
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

            <form onSubmit={handleFormSubmit} className="w-full flex flex-col gap-6">
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
              <div className="box-border content-stretch flex items-center justify-between pb-0 pt-1.5 px-0 relative shrink-0 w-full">
                <button
                  type="button"
                  onClick={() => setMode('url')}
                  className="bg-white hover:bg-gray-50 box-border content-stretch flex gap-1.5 h-[46px] items-center justify-start px-[18px] py-2.5 relative rounded-[12px] shrink-0 border border-gray-300 transition-colors"
                >
                  <span className="font-semibold text-gray-600 text-[16px]">
                    링크로 의상 불러오기
                  </span>
                </button>
                <div className="content-stretch flex gap-3 items-center justify-start relative shrink-0">
                  <button
                    type="button"
                    onClick={handleClose}
                    className="bg-gray-100 hover:bg-gray-200 box-border content-stretch flex gap-1.5 h-[46px] items-center justify-center px-[18px] py-2.5 relative rounded-[12px] shrink-0 transition-colors"
                  >
                    <span className="font-bold text-gray-700 text-[18px]">취소</span>
                  </button>
                  <button
                    type="submit"
                    disabled={loading || !formData.name || !formData.type}
                    className="bg-blue-500 hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed box-border content-stretch flex gap-1.5 h-[46px] items-center justify-center px-[18px] py-2.5 relative rounded-[12px] shrink-0 transition-colors"
                  >
                    <span className="font-bold text-white text-[18px]">
                      {loading ? '저장 중...' : '저장'}
                    </span>
                  </button>
                </div>
              </div>
            </form>
          </div>
        ) : (
          // URL 불러오기 폼
          <div className="bg-white box-border content-stretch flex flex-col gap-6 items-center justify-start p-[30px] relative rounded-[20px] shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)] w-full">
            {/* 헤더 */}
            <div className="content-stretch flex items-center relative shrink-0 w-full">
              <button onClick={() => setMode('form')} className="content-stretch flex gap-2 items-center justify-start relative shrink-0 size-[30px] hover:bg-gray-100 rounded transition-colors">
                <div className="overflow-clip relative shrink-0 size-[30px]">
                  <div className="absolute inset-[20.83%_33.33%]">
                    <img alt="뒤로가기" className="block max-w-none size-full" src={leftArrowIcon} />
                  </div>
                </div>
              </button>
              <DialogTitle className="font-bold leading-none not-italic absolute left-1/2 transform -translate-x-1/2 text-gray-800 text-[22px] text-nowrap tracking-[-0.55px]">
                링크로 의상 불러오기
              </DialogTitle>
              <DialogClose asChild>
                <button className="overflow-clip relative shrink-0 size-[30px] hover:bg-gray-100 rounded transition-colors ml-auto">
                  <div className="absolute inset-[20.834%]">
                    <img alt="닫기" className="block max-w-none size-full" src={closeIcon} />
                  </div>
                </button>
              </DialogClose>
            </div>

            <form onSubmit={handleUrlSubmit} className="w-full flex flex-col gap-6">
              {/* URL 입력 */}
              <div className="content-stretch flex flex-col gap-2.5 items-start justify-start relative shrink-0 w-full">
                <div className="font-bold leading-none not-italic relative shrink-0 text-gray-500 text-[14px] tracking-[-0.35px] w-full">
                  <p className="leading-normal">URL</p>
                </div>
                <input
                  type="url"
                  value={url}
                  onChange={(e) => setUrl(e.target.value)}
                  placeholder="URL을 입력해주세요"
                  className="bg-white box-border content-stretch flex h-[46px] items-center justify-between px-5 py-3.5 relative rounded-[12px] shrink-0 w-full border border-gray-200 shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)] focus:outline-none focus:border-blue-500"
                  required
                />
              </div>

              {/* 하단 버튼 */}
              <div className="box-border content-stretch flex gap-3 items-center justify-end pb-0 pt-1.5 px-0 relative shrink-0 w-full">
                <button
                  type="button"
                  onClick={() => setMode('form')}
                  className="bg-gray-100 hover:bg-gray-200 box-border content-stretch flex gap-1.5 h-[46px] items-center justify-center px-[18px] py-2.5 relative rounded-[12px] shrink-0 transition-colors"
                >
                  <span className="font-bold text-gray-700 text-[18px]">취소</span>
                </button>
                <button
                  type="submit"
                  disabled={loading || !url.trim()}
                  className="bg-blue-500 hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed box-border content-stretch flex gap-1.5 h-[46px] items-center justify-center px-[18px] py-2.5 relative rounded-[12px] shrink-0 transition-colors"
                >
                  <span className="font-bold text-white text-[18px]">
                    {loading ? '불러오는 중...' : '불러오기'}
                  </span>
                </button>
              </div>
            </form>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}