import { useCallback } from 'react';
import type { OAuthProvider } from '@/lib/api/types';

export default function useOAuth2() {
  const BASE_URL = import.meta.env.VITE_PUBLIC_PATH || '';

  const oauth2 = useCallback(
      (provider: OAuthProvider) => {
        const url = `${BASE_URL}/oauth2/authorization/${provider}`;
        console.log({ url });
        window.location.href = url;
      },
      [BASE_URL],
  );

  return { oauth2 };
}
