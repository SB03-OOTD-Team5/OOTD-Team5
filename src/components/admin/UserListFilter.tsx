import { useState } from "react";
import { useUserStore } from "@/lib/stores/useUserStore";
import { type Role } from "@/lib/api/types";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import searchIcon from "@/assets/icons/ic_search.svg";

export default function UserListFilter() {
  const { updateParams } = useUserStore();
  const [searchEmail, setSearchEmail] = useState("");
  const [roleFilter, setRoleFilter] = useState<string>("전체");
  const [statusFilter, setStatusFilter] = useState<string>("잠금 상태");
  const [searchTimeout, setSearchTimeout] = useState<NodeJS.Timeout | null>(null);

  const handleSearch = (email: string) => {
    setSearchEmail(email);
    
    // 이전 타이머 클리어
    if (searchTimeout) {
      clearTimeout(searchTimeout);
    }
    
    // 500ms 디바운싱 후 검색 실행
    const timeout = setTimeout(() => {
      updateParams({ emailLike: email || undefined });
    }, 500);
    
    setSearchTimeout(timeout);
  };

  const handleRoleFilter = (role: string) => {
    setRoleFilter(role);
    const roleParam = role === "전체" ? undefined : role as Role;
    updateParams({ roleEqual: roleParam });
  };

  const handleStatusFilter = (status: string) => {
    setStatusFilter(status);
    const lockedParam = status === "잠금 상태" ? undefined : 
                       status === "활성" ? false : true;
    updateParams({ locked: lockedParam });
  };

  return (
    <div className="flex gap-3.5 items-center px-5 py-2.5">
      {/* 검색창 */}

      <div className="relative w-[280px]">

        <Input
          type="text"
          placeholder="이메일 검색"
          value={searchEmail}
          icon={
            <img
                src={searchIcon}
                alt="검색"
                className="absolute left-[22px] top-1/2 transform -translate-y-1/2 size-5"
            />
          }
          onChange={(e) => handleSearch(e.target.value)}
          className="w-full h-[36px] pl-[54px] pr-[22px] py-3.5 bg-white border border-[#d4d4d9] rounded-[100px] text-[16px] font-['SUIT:SemiBold',_sans-serif] text-[#575765] placeholder:text-[#a9a9b1] focus:border-[#d4d4d9] focus:ring-0 shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]"
        />
      </div>

      {/* 권한 필터 */}
      <Select value={roleFilter} onValueChange={handleRoleFilter}>
        <SelectTrigger className="w-auto h-[36px] px-[22px] pr-[18px] bg-white border border-[#d4d4d9] rounded-[100px] text-[16px] font-['SUIT:SemiBold',_sans-serif] text-[#575765] focus:border-[#d4d4d9] focus:ring-0 shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]">
          <SelectValue placeholder="모든 권한" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="전체">모든 권한</SelectItem>
          <SelectItem value="USER">USER</SelectItem>
          <SelectItem value="ADMIN">ADMIN</SelectItem>
        </SelectContent>
      </Select>

      {/* 상태 필터 */}
      <Select value={statusFilter} onValueChange={handleStatusFilter}>
        <SelectTrigger className="w-auto h-[36px] px-[22px] pr-[18px] bg-white border border-[#d4d4d9] rounded-[100px] text-[16px] font-['SUIT:SemiBold',_sans-serif] text-[#575765] focus:border-[#d4d4d9] focus:ring-0 shadow-[0px_2px_4px_0px_rgba(55,55,64,0.03)]">
          <SelectValue placeholder="모든 상태" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="잠금 상태">모든 상태</SelectItem>
          <SelectItem value="활성">활성</SelectItem>
          <SelectItem value="잠금">잠금</SelectItem>
        </SelectContent>
      </Select>
    </div>
  );
}