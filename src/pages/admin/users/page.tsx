import UserListFilter from "@/components/admin/UserListFilter";
import UserList from "@/components/admin/UserList";

export default function UserManagementPage() {
  return (
    <div className="flex flex-col h-full">
      {/* 검색 및 필터 섹션 */}
      <div className="flex-shrink-0">
        <UserListFilter />
      </div>
      
      {/* 사용자 목록 테이블 */}
      <div className="flex-1 min-h-0">
        <UserList />
      </div>
    </div>
  );
}