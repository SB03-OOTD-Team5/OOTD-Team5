import { useState, useEffect } from "react";
import { useClothesAttributeDefStore } from "@/lib/stores/useClothesAttributeDefStore";
import { updateClothesAttributeDef, deleteClothesAttributeDef } from "@/lib/api/clothes-attributes";
import { type ClothesAttributeDefDto } from "@/lib/api/types";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { X } from "lucide-react";
import { toast } from "sonner";
import ClothesAttributeTag from "./ClothesAttributeTag";

interface EditClothesAttributeModalProps {
  isOpen: boolean;
  attribute: ClothesAttributeDefDto | null;
  onClose: () => void;
}

export default function EditClothesAttributeModal({ isOpen, attribute, onClose }: EditClothesAttributeModalProps) {
  const { update, delete: deleteFromStore } = useClothesAttributeDefStore();
  const [name, setName] = useState("");
  const [optionValue, setOptionValue] = useState("");
  const [selectableValues, setSelectableValues] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [isDeleteAlertOpen, setIsDeleteAlertOpen] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);

  // 모달이 열릴 때 기존 데이터로 초기화
  useEffect(() => {
    if (isOpen && attribute) {
      setName(attribute.name);
      setSelectableValues([...attribute.selectableValues]);
      setOptionValue("");
    }
  }, [isOpen, attribute]);

  const handleClose = () => {
    setName("");
    setOptionValue("");
    setSelectableValues([]);
    setIsDeleteAlertOpen(false);
    onClose();
  };

  const handleAddOption = (value: string) => {
    const trimmedValue = value.trim();
    if (!trimmedValue) return;

    // 중복 검사 (대소문자 구분 없음)
    if (selectableValues.some(v => v.toLowerCase() === trimmedValue.toLowerCase())) {
      toast.error("이미 존재하는 옵션입니다.");
      return;
    }

    setSelectableValues(prev => [...prev, trimmedValue]);
    setOptionValue("");
  };

  const handleOptionKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleAddOption(optionValue);
    }
  };

  const handleRemoveOption = (index: number) => {
    setSelectableValues(prev => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async () => {
    if (!attribute) return;

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
      const updatedAttribute = await updateClothesAttributeDef(attribute.id, {
        name: name.trim(),
        selectableValues
      });

      // 스토어 업데이트
      update(attribute.id, updatedAttribute);

      // 성공 토스트
      toast.success("속성이 성공적으로 수정되었습니다.");

      handleClose();
    } catch (error) {
      console.error('Update attribute failed:', error);
      toast.error("속성 수정에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!attribute) return;
    
    setDeleteLoading(true);
    try {
      await deleteClothesAttributeDef(attribute.id);
      // 스토어에서 삭제
      deleteFromStore(attribute.id);
      // 성공 토스트
      toast.success("속성이 성공적으로 삭제되었습니다.");
      setIsDeleteAlertOpen(false);
      handleClose();
    } catch (error) {
      console.error('Delete attribute failed:', error);
      toast.error("속성 삭제에 실패했습니다.");
    } finally {
      setDeleteLoading(false);
    }
  };

  if (!attribute) return null;

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="w-[550px] p-[30px] gap-[23px]" showCloseButton={false}>
        {/* 헤더 */}
        <DialogHeader className="flex flex-row items-center justify-between space-y-0">
          <div />
          <DialogTitle className="text-[22px] font-['SUIT:Bold',_sans-serif] text-[#34343d] tracking-[-0.55px]">
            속성 수정 / 삭제
          </DialogTitle>
          <button
            onClick={handleClose}
            className="size-[30px] flex items-center justify-center hover:opacity-70"
          >
            <X className="size-6 text-[#34343d]" />
          </button>
        </DialogHeader>

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
        <div className="flex gap-3 justify-between">
          <Button
            onClick={() => setIsDeleteAlertOpen(true)}
            variant="outline"
            className="h-[46px] px-[18px] py-2.5 bg-white border border-[#f24346] text-[#f24346] font-['SUIT:SemiBold',_sans-serif] text-[16px] tracking-[-0.4px] rounded-[12px] hover:bg-[#f24346]/5"
          >
            삭제
          </Button>
          <div className="flex gap-3">
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
        </div>
      </DialogContent>
      
      {/* 삭제 확인 다이얼로그 */}
      <AlertDialog open={isDeleteAlertOpen} onOpenChange={setIsDeleteAlertOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>속성을 삭제하시겠습니까?</AlertDialogTitle>
            <AlertDialogDescription>
              이 작업은 되돌릴 수 없습니다. "{attribute?.name}" 속성 정보가 영구적으로 삭제됩니다.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>취소</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              disabled={deleteLoading}
            >
              {deleteLoading ? '삭제 중...' : '삭제'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </Dialog>
  );
}