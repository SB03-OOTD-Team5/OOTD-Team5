import {type CursorParams} from "@/lib/api";

export interface BaseStore<T, P> {
  data: T | null;
  update: (newData: Partial<T>) => void;

  params: P;
  updateParams: (newParams: Partial<P>, options?: Partial<{ignoreFetch: boolean}>) => void;

  fetch: (options?: {
    throwError?: boolean;
    ignoreLoading?: boolean;
  }) => Promise<void>;
  clearData: () => void;

  loading: boolean;

  error?: string;
  clearError: () => void;

  clear: () => void;
}



export interface ListStore<T, P> {
  data: T[];
  add: (item: T) => void;
  update: (id: string, newData: Partial<T>) => void;
  delete: (id: string) => void;
  count: () => number;

  params: P;
  updateParams: (newParams: Partial<P>, options?: Partial<{ignoreFetch: boolean}>) => void;

  fetch: (options?: {
    throwError?: boolean;
    ignoreLoading?: boolean;
  }) => Promise<void>;
  clearData: () => void;

  loading: boolean;

  error?: string;
  clearError: () => void;

  clear: () => void;
}

export interface PaginatedStore<T, P extends CursorParams> {
  data: T[];
  add: (item: T) => void;
  update: (id: string, newData: Partial<T>) => void;
  delete: (id: string) => void;
  count: () => number;

  params: Omit<P, 'cursor' | 'idAfter'>;
  updateParams: (newParams: Partial<Omit<P, 'cursor' | 'idAfter'>>, options?: Partial<{ignoreFetch: boolean}>) => void;

  cursorState: CursorState;
  hasNext: () => boolean;

  fetch: (options?: {
    throwError?: boolean;
    ignoreLoading?: boolean;
  }) => Promise<void>;
  fetchMore: (options?: {
    throwError?: boolean;
    ignoreLoading?: boolean;
  }) => Promise<void>;
  clearData: () => void;

  loading: boolean;

  error?: string;
  clearError: () => void;

  clear: () => void;
}

export interface CursorState {
  nextCursor?: string;
  nextIdAfter?: string;
  hasNext: boolean;
  totalCount: number;
}