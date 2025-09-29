import { useEffect, useState } from "react";
import { useUserStore } from "@/lib/stores/useUserStore";
import { useAuthStore } from "@/lib/stores/useAuthStore";
import { updateRole, updateUserLock } from "@/lib/api/users";
import { type UserDto } from "@/lib/api/types";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Switch } from "@/components/ui/switch";
import { toast } from "sonner";
import { ChevronUp, ChevronDown } from "lucide-react";
import { useInfiniteScroll } from "@/lib/hooks/useInfiniteScroll";

export default function UserList() {
  const { data: users, loading, fetch, update, params, updateParams, fetchMore } = useUserStore();
  const { data: authData } = useAuthStore();
  const [toggleLoading, setToggleLoading] = useState<{
    userId: string;
    type: 'lock' | 'role';
  } | null>(null);

  // 무한 스크롤 설정
  const { ref: scrollRef } = useInfiniteScroll({
    onLoadMore: () => fetchMore()
  });

  // params가 변경될 때마다 fetch 호출
  useEffect(() => {
    fetch();
  }, [params, fetch]);

  const handleSort = (field: 'email' | 'createdAt') => {
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

  const getSortIcon = (field: 'email' | 'createdAt') => {
    if (params.sortBy !== field) {
      return <ChevronDown className="w-4 h-4 text-gray-400 opacity-0 group-hover:opacity-100 transition-opacity" />;
    }
    
    return params.sortDirection === 'DESCENDING' 
      ? <ChevronDown className="w-4 h-4 text-blue-500" />
      : <ChevronUp className="w-4 h-4 text-blue-500" />;
  };

  const handleLockToggle = async (user: UserDto) => {
    if (toggleLoading?.userId === user.id) return;
    if (authData?.userDto?.id === user.id) return; // 자기 자신 계정 제외
    
    setToggleLoading({ userId: user.id, type: 'lock' });
    
    try {
      const newLockState = !user.locked;
      const updatedUser = await updateUserLock(user.id, { locked: newLockState });
      
      // 스토어 상태 업데이트
      update(user.id, { locked: updatedUser.locked });
      
      // Toast 알림
      toast.success(
        newLockState 
          ? `${user.email} 계정이 잠금되었습니다.`
          : `${user.email} 계정 잠금이 해제되었습니다.`
      );
    } catch (error) {
      console.error('Lock toggle failed:', error);
      toast.error('계정 상태 변경에 실패했습니다.');
    } finally {
      setToggleLoading(null);
    }
  };

  const handleRoleToggle = async (user: UserDto) => {
    if (toggleLoading?.userId === user.id) return;
    if (authData?.userDto?.id === user.id) return; // 자기 자신 계정 제외
    
    setToggleLoading({ userId: user.id, type: 'role' });
    
    try {
      const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
      const updatedUser = await updateRole(user.id, { role: newRole });
      
      // 스토어 상태 업데이트
      update(user.id, { role: updatedUser.role });
      
      // Toast 알림
      toast.success(`${user.email} 권한이 ${newRole}으로 변경되었습니다.`);
    } catch (error) {
      console.error('Role toggle failed:', error);
      toast.error('권한 변경에 실패했습니다.');
    } finally {
      setToggleLoading(null);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="h-full overflow-y-auto">
      <div className="px-5">
        <div className="rounded-lg overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow className="bg-[#f7f7f8] h-11 hover:bg-[#f7f7f8] border-none">
                <TableHead 
                  className={`group px-5 py-3 text-[16px] font-['SUIT:Bold',_sans-serif] tracking-[-0.4px] w-[300px] cursor-pointer select-none ${
                    params.sortBy === 'email' ? 'text-blue-500' : 'text-[#34343d] hover:text-blue-500'
                  }`}
                  onClick={() => handleSort('email')}
                >
                  <div className="flex items-center gap-1">
                    이메일
                    {getSortIcon('email')}
                  </div>
                </TableHead>
                <TableHead 
                  className={`group px-5 py-3 text-[16px] font-['SUIT:Bold',_sans-serif] tracking-[-0.4px] w-[320px] cursor-pointer select-none ${
                    params.sortBy === 'createdAt' ? 'text-blue-500' : 'text-[#34343d] hover:text-blue-500'
                  }`}
                  onClick={() => handleSort('createdAt')}
                >
                  <div className="flex items-center gap-1">
                    생성일
                    {getSortIcon('createdAt')}
                  </div>
                </TableHead>
                <TableHead className="px-5 py-3 text-[16px] font-['SUIT:Bold',_sans-serif] text-[#34343d] tracking-[-0.4px] w-[200px]">
                  계정 상태
                </TableHead>
                <TableHead className="px-5 py-3 text-[16px] font-['SUIT:Bold',_sans-serif] text-[#34343d] tracking-[-0.4px] w-[200px]">
                  ADMIN
                </TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {users.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={4} className="px-5">
                    <div className="flex items-center justify-center h-32 w-full">
                      <p className="text-[#575765] text-[16px] font-['SUIT:SemiBold',_sans-serif] text-center">등록된 사용자가 없습니다.</p>
                    </div>
                  </TableCell>
                </TableRow>
              ) : (
                users.map((user) => (
                  <TableRow key={user.id} className="h-14 hover:bg-gray-50 border-b border-[#e7e7e9] last:border-b-0">
                    {/* 이메일 */}
                    <TableCell className="px-5 py-0 text-[16px] font-['SUIT:SemiBold',_sans-serif] text-[#575765] tracking-[-0.4px]">
                      {user.email}
                    </TableCell>
                    
                    {/* 생성일 */}
                    <TableCell className="px-5 py-0 text-[16px] font-['SUIT:SemiBold',_sans-serif] text-[#575765] tracking-[-0.4px]">
                      {formatDate(user.createdAt)}
                    </TableCell>
                    
                    {/* 계정 상태 - 커스텀 버튼 유지 (Figma에 더 적합) */}
                    <TableCell className="px-5 py-0">
                      <div className="flex h-[34.568px] items-center justify-start relative w-[68.282px]">
                        <div className="flex-none rotate-[359.52deg]">
                          <div 
                            className={`box-border content-stretch flex gap-1 h-[34px] items-center justify-center px-3 py-2 relative rounded-[100px] w-[68px] transition-colors ${
                              authData?.userDto?.id === user.id 
                                ? 'cursor-not-allowed opacity-50' 
                                : 'cursor-pointer'
                            } ${
                              user.locked ? 'bg-[#34343d]' : 'bg-[#e7e7e9]'
                            }`}
                            onClick={() => handleLockToggle(user)}
                          >
                            <div className={`flex flex-col font-['SUIT:${user.locked ? 'Bold' : 'SemiBold'}',_sans-serif] justify-center leading-[0] not-italic relative shrink-0 text-[14px] text-nowrap tracking-[-0.35px] ${
                              user.locked ? 'text-white' : 'text-[#575765]'
                            }`}>
                              <p className="leading-[normal] whitespace-pre">{user.locked ? '비활성' : '활성'}</p>
                            </div>
                          </div>
                        </div>
                      </div>
                    </TableCell>
                    
                    {/* ADMIN - shadcn/ui Switch로 복원 */}
                    <TableCell className="px-5 py-0">
                      <div className="flex items-center justify-start">
                        <Switch
                          checked={user.role === 'ADMIN'}
                          onCheckedChange={() => handleRoleToggle(user)}
                          disabled={
                            (toggleLoading?.userId === user.id && toggleLoading?.type === 'role') ||
                            authData?.userDto?.id === user.id
                          }
                          className="w-[50px] h-[25px] data-[state=checked]:bg-[#0d99ff] data-[state=unchecked]:bg-[#d4d4d9]"
                          style={{
                            '--thumb-size': '18px',
                            '--thumb-translate-checked': '28px',
                            '--thumb-translate-unchecked': '4px',
                          } as React.CSSProperties}
                        />
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
              {
                loading && (
                    <TableRow>
                      <TableCell colSpan={4} className="px-5">
                        <div className="flex items-center justify-center h-32 w-full">
                          <p className="text-[#575765] text-[16px] font-['SUIT:SemiBold',_sans-serif]">로딩 중...</p>
                        </div>
                      </TableCell>
                    </TableRow>
                )
              }
            </TableBody>
          </Table>
          
          {/* 무한 스크롤 트리거 영역 */}
          <div ref={scrollRef} className="w-full h-1 mt-4" />
        </div>
      </div>
    </div>
  );
}