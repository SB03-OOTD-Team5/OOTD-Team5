import {useState} from "react";
import {useClothesAttributeDefStore} from "@/lib/stores/useClothesAttributeDefStore";
import {createClothesAttributeDef} from "@/lib/api/clothes-attributes";
import {Dialog, DialogContent} from "@/components/ui/dialog";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {toast} from "sonner";
import ClothesAttributeTag from "./ClothesAttributeTag";
import closeIcon from '@/assets/icons/ic_X.svg'

interface AddClothesAttributeModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function AddClothesAttributeModal({ isOpen, onClose }: AddClothesAttributeModalProps) {
  const { add } = useClothesAttributeDefStore();
  const [name, setName] = useState("");
  const [optionValue, setOptionValue] = useState("");
  const [selectableValues, setSelectableValues] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);

  const handleClose = () => {
    setName("");
    setOptionValue("");
    setSelectableValues([]);
    onClose();
  };

  const handleAddOption = (value: string) => {
    const trimmedValue = value.trim();
    if (!trimmedValue) return;

    // 중복 검사 (대소문자 구분 없음)
    if (selectableValues.some(v => v.toLowerCase() === trimmedValue.toLowerCase())) {
      toast.error("이미 존재하는 옵션입니다.");
      setOptionValue("");
      return;
    }

    setSelectableValues(prev => [...prev, trimmedValue]);
    setOptionValue("");
  };

  const handleOptionKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      // IME 입력 전 상태에서는 실행하지 않도록 처리
      if (e.nativeEvent.isComposing) {
        return;
      }
      handleAddOption(optionValue);
    }
  };

  const handleRemoveOption = (index: number) => {
    setSelectableValues(prev => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async () => {
    if (!name.trim()) {
      toast.error("속성명을 입력해주세요.");
      return;
    }

    if (selectableValues.length === 0) {
      toast.error("최소 하나의 옵션 값을 추가해주세요.");
      return;
    }

    setLoading(true);

    try {
      const newAttribute = await createClothesAttributeDef({
        name: name.trim(),
        selectableValues
      });

      // 스토어에 추가
      add(newAttribute);

      // 성공 토스트
      toast.success("속성이 성공적으로 추가되었습니다.");

      handleClose();
    } catch (error) {
      console.error('Add attribute failed:', error);
      toast.error("속성 추가에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="w-[550px] p-[30px] gap-[23px]" showCloseButton={false}>
        {/* 헤더 */}
        <div className="flex items-center justify-between">
          <div />
          <h2 className="text-[22px] font-['SUIT:Bold',_sans-serif] text-[#34343d] tracking-[-0.55px]">
            속성 추가
          </h2>
          <button
            onClick={handleClose}
            className="overflow-clip relative shrink-0 size-[30px] hover:bg-gray-100 rounded transition-colors"
          >
            <div className="absolute inset-[20.834%]">
              <img alt="닫기" className="block max-w-none size-full" src={closeIcon} />
            </div>
          </button>
        </div>

        {/* 속성명 입력 */}
        <div className="flex flex-col gap-2.5">
          <label className="text-[14px] font-['SUIT:Bold',_sans-serif] text-[#808089] tracking-[-0.35px]">
            속성명
          </label>
          <Input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="속성명을 입력해주세요"
            className="h-[46px] px-5 py-3.5 bg-white border border-[#e7e7e9] rounded-[12px] text-[16px] font-['SUIT:SemiBold',_sans-serif] text-[#696975] placeholder:text-[#a9a9b1] focus:border-[#e7e7e9] focus:ring-0 shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]"
          />
        </div>

        {/* 옵션 값 입력 및 태그 목록 */}
        <div className="flex flex-col gap-3">
          <div className="flex flex-col gap-2.5">
            <label className="text-[14px] font-['SUIT:Bold',_sans-serif] text-[#808089] tracking-[-0.35px]">
              옵션 값
            </label>
            <Input
              value={optionValue}
              onChange={(e) => setOptionValue(e.target.value)}
              onKeyDown={handleOptionKeyDown}
              placeholder="옵션을 입력해주세요"
              className="h-[46px] px-5 py-3.5 bg-white border border-[#e7e7e9] rounded-[12px] text-[16px] font-['SUIT:SemiBold',_sans-serif] text-[#696975] placeholder:text-[#a9a9b1] focus:border-[#e7e7e9] focus:ring-0 shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]"
            />
          </div>

          {/* 추가된 태그들 */}
          {selectableValues.length > 0 && (
            <div className="flex flex-wrap gap-2.5 px-1">
              {selectableValues.map((value, index) => (
                <ClothesAttributeTag
                  key={index}
                  label={value}
                  variant="selected"
                  showDelete
                  onDelete={() => handleRemoveOption(index)}
                />
              ))}
            </div>
          )}
        </div>

        {/* 버튼들 */}
        <div className="flex gap-3 justify-end">
          <Button
            onClick={handleClose}
            variant="outline"
            className="h-[46px] px-[18px] py-2.5 bg-white border border-[#d4d4d9] text-[#696975] font-['SUIT:SemiBold',_sans-serif] text-[16px] tracking-[-0.4px] rounded-[12px] hover:bg-gray-50"
          >
            취소
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={loading}
            className="h-[46px] px-[18px] py-2.5 bg-[#1e89f4] hover:bg-[#1e89f4]/90 text-white font-['SUIT:Bold',_sans-serif] text-[18px] tracking-[-0.45px] rounded-[12px]"
          >
            {loading ? "처리 중..." : "완료"}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}