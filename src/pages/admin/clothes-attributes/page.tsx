import { useState } from "react";
import { type ClothesAttributeDefDto } from "@/lib/api/types";
import ClothesAttributeFilter from "@/components/admin/ClothesAttributeFilter";
import ClothesAttributeList from "@/components/admin/ClothesAttributeList";
import AddClothesAttributeModal from "@/components/admin/AddClothesAttributeModal";
import EditClothesAttributeModal from "@/components/admin/EditClothesAttributeModal";

export default function ClothesAttributeManagementPage() {
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingAttribute, setEditingAttribute] = useState<ClothesAttributeDefDto | null>(null);

  const handleAddClick = () => {
    setIsAddModalOpen(true);
  };

  const handleItemClick = (attribute: ClothesAttributeDefDto) => {
    setEditingAttribute(attribute);
    setIsEditModalOpen(true);
  };

  const handleCloseAddModal = () => {
    setIsAddModalOpen(false);
  };

  const handleCloseEditModal = () => {
    setIsEditModalOpen(false);
    setEditingAttribute(null);
  };

  return (
    <div className="w-full h-[calc(100vh-80px)] flex flex-col">
      {/* 검색 및 필터 섹션 */}
      <div className="flex-shrink-0">
        <ClothesAttributeFilter onAddClick={handleAddClick} />
      </div>
      
      {/* 의상 속성 목록 테이블 */}
      <div className="flex-1 min-h-0">
        <ClothesAttributeList onItemClick={handleItemClick} />
      </div>

      {/* 속성 추가 모달 */}
      <AddClothesAttributeModal
        isOpen={isAddModalOpen}
        onClose={handleCloseAddModal}
      />

      {/* 속성 수정 모달 */}
      <EditClothesAttributeModal
        isOpen={isEditModalOpen}
        attribute={editingAttribute}
        onClose={handleCloseEditModal}
      />
    </div>
  );
}