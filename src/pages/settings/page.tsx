import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { useAuthStore } from '@/lib/stores/useAuthStore';
import { useMyProfileStore } from '@/lib/stores/useMyProfileStore';
import { updateProfile } from '@/lib/api/users';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import ProfileImageUpload from '@/components/profile/ProfileImageUpload';
import GenderRadioGroup from '@/components/profile/GenderRadioGroup';
import TemperatureSensitivitySlider from '@/components/profile/TemperatureSensitivitySlider';
import LocationInput from '@/components/profile/LocationInput';
import { type Gender, type ProfileUpdateRequest, type WeatherAPILocation } from '@/lib/api/types';

interface SettingsFormData {
  name: string;
  gender?: Gender;
  birthDate?: string;
  location?: WeatherAPILocation;
  temperatureSensitivity?: number;
  profileImageUrl?: string;
}

export default function MyProfileSettingsPage() {
  const { data: authData } = useAuthStore();
  const { data: profile, loading } = useMyProfileStore();
  const [selectedImage, setSelectedImage] = useState<File | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    reset,
    formState: { errors, isDirty }
  } = useForm<SettingsFormData>();

  const currentUserId = authData?.userDto?.id;

  // 프로필 데이터 로드
  useEffect(() => {
    if (currentUserId) {
      useMyProfileStore.getState().updateParams({ userId: currentUserId });
    }
  }, [currentUserId]);

  // 폼 데이터 초기화
  useEffect(() => {
    if (profile) {
      reset({
        name: profile.name || '',
        gender: profile.gender,
        birthDate: profile.birthDate || '',
        location: profile.location,
        temperatureSensitivity: profile.temperatureSensitivity,
        profileImageUrl: profile.profileImageUrl
      });
    }
  }, [profile, reset]);

  // 폼 상태 감시
  const watchedValues = watch();

  const handleImageSelect = (file: File | null) => {
    setSelectedImage(file);
    const profileImageUrl = file ? URL.createObjectURL(file) : profile?.profileImageUrl || '';
    setValue('profileImageUrl', profileImageUrl, { shouldDirty: true }); //
  };

  const handleGenderChange = (gender: Gender) => {
    setValue('gender', gender, { shouldDirty: true });
  };

  const handleTemperatureChange = (value: number) => {
    setValue('temperatureSensitivity', value, { shouldDirty: true });
  };

  const handleLocationChange = (location: WeatherAPILocation | undefined) => {
    setValue('location', location, { shouldDirty: true });
  };

  const handleReset = () => {
    if (profile) {
      reset({
        name: profile.name || '',
        gender: profile.gender,
        birthDate: profile.birthDate || '',
        location: profile.location,
        temperatureSensitivity: profile.temperatureSensitivity,
        profileImageUrl: profile.profileImageUrl
      });
      setSelectedImage(null);
    }
  };

  const onSubmit = async (data: SettingsFormData) => {
    if (!currentUserId) {
      toast.error('사용자 정보를 찾을 수 없습니다.');
      return;
    }

    setIsSubmitting(true);

    try {
      const updateRequest: ProfileUpdateRequest = {
        name: data.name || undefined,
        gender: data.gender,
        birthDate: data.birthDate || undefined,
        temperatureSensitivity: data.temperatureSensitivity
      };

      // 위치 정보 처리
      if (data.location) {
        updateRequest.location = data.location;
      }

      // API 직접 호출
      const updatedProfile = await updateProfile(
        currentUserId, 
        updateRequest, 
        selectedImage || undefined
      );

      // 스토어 데이터 동기화
      useMyProfileStore.getState().update(updatedProfile);

      toast.success('프로필이 성공적으로 업데이트되었습니다.');
      setSelectedImage(null);
    } catch (error) {
      console.error('프로필 업데이트 실패:', error);
      toast.error('프로필 업데이트에 실패했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-[var(--color-gray-500)]">로딩 중...</div>
      </div>
    );
  }

  return (
    <div className="space-y-6 px-8 ">

      <div className="bg-white p-8">
        <form onSubmit={handleSubmit(onSubmit)} className="max-w-[428px] mx-auto space-y-6">
          {/* 프로필 이미지 */}
          <ProfileImageUpload
            currentImageUrl={watchedValues.profileImageUrl}
            name={profile?.name}
            onImageSelect={handleImageSelect}
            className="mb-6"
          />

          {/* 닉네임 */}
          <div className="space-y-2.5">
            <label className="block text-[var(--font-size-body-3)] font-[var(--font-weight-bold)] text-[var(--color-gray-500)] tracking-[-0.35px]">
              닉네임
            </label>
            <Input
              {...register('name', { required: '닉네임을 입력해주세요' })}
              placeholder="닉네임을 입력해주세요"
              error={errors.name?.message}
            />
          </div>

          {/* 성별 */}
          <div className="space-y-2.5">
            <GenderRadioGroup
              value={watchedValues.gender}
              onValueChange={handleGenderChange}
            />
          </div>

          {/* 생년월일 */}
          <div className="space-y-2.5">
            <label className="block text-[var(--font-size-body-3)] font-[var(--font-weight-bold)] text-[var(--color-gray-500)] tracking-[-0.35px]">
              생년월일
            </label>
            <Input
              {...register('birthDate')}
              type="date"
              placeholder="0000-00-00"
            />
          </div>

          {/* 현재 위치 */}
          <LocationInput
            location={watchedValues.location}
            onChange={handleLocationChange}
          />

          {/* 온도 민감도 */}
          <TemperatureSensitivitySlider
            value={watchedValues.temperatureSensitivity}
            onValueChange={handleTemperatureChange}
          />

          {/* 액션 버튼 */}
            {
              isDirty && (
                  <div className="flex gap-3.5 items-center justify-end pt-3">
                    <Button
                        type="button"
                        variant="secondary"
                        onClick={handleReset}
                        disabled={isSubmitting}
                    >
                      되돌리기
                    </Button>
                    <Button
                        type="submit"
                        variant="primary"
                        disabled={isSubmitting}
                    >
                      {isSubmitting ? '저장 중...' : '저장'}
                    </Button>
                  </div>
                )
            }


        </form>
      </div>
    </div>
  );
}