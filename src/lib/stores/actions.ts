import type {BaseStore, CursorState, ListStore, PaginatedStore} from "@/lib/stores/types.ts";
import type {CursorParams, CursorResponse} from "@/lib/api";

type SetType<S> = {
  (partial: S | Partial<S> | ((state: S) => S | Partial<S>), replace?: false | undefined): void;
}

export function createBaseStoreActions<T, P>(
    {set, get, fetchApi, initialData} : {
      set: SetType<BaseStore<T, P>>,
      get: () => BaseStore<T, P>,
      fetchApi: (p: P) => Promise<T>,
      initialData?: Partial<{
        data: T | null;
        params: P;
      }>
    }
): BaseStore<T, P> {
  const _initialData = {data: null, params: {} as P, ...initialData};

  return ({
    data: _initialData.data,
    update: newData => {
      set((state) => ({
        data: state.data ? { ...state.data, ...newData } : null,
      }))
    },

    params: _initialData.params,
    updateParams: (newParams, options) => {
      const _options = {autoFetch: true, ...options};
      set((state) => ({
        params: { ...state.params, ...newParams },
      }))
      if (_options.autoFetch) {
        get().fetch();
      }
    },

    fetch: async (options) => {
      const {loading} = get();
      if (loading && options?.ignoreLoading !== true) {
        console.warn(`로딩 중이므로 요청이 무시되었습니다.`);
        return;
      }

      try {
        set({loading: true, error: undefined});

        const {params} = get();
        const result = await fetchApi(params);

        set({data: result});

      } catch (error) {
        console.error(error);
        set({error: (error as Error).message || '알 수 없는 오류가 발생했습니다.'});

        if (options?.throwError) {
          throw error;
        }
      } finally {
        set({loading: false});
      }
    },
    clearData: () => {
      set({
        ..._initialData,
        loading: false,
      })
    },

    loading: false,

    error: undefined,
    clearError: () => {set({error: undefined})},

    clear: () => {
      const { clearData, clearError } = get();
      clearData();
      clearError();
    }
  })
}
export function createListStoreActions<T, P>(
    {set, get, fetchApi, initialData, keyExtractor = e => (e as never)['id'] as string} : {
      set: SetType<ListStore<T, P>>,
      get: () => ListStore<T, P>,
      fetchApi: (p: P) => Promise<T[]>,
      initialData?: {
        data?: T[];
        params?: P;
      },
      keyExtractor?: (e: T) => string
    }
): ListStore<T, P> {
  const _initialData = {data: [], params: {} as P, ...initialData};
  return ({
    data: _initialData.data,
    add: newItem => {
      set(state => {
        const data = state.data;
        const newItemKey = keyExtractor(newItem);
        const isDuplicate = data.some(item => keyExtractor(item) === newItemKey);
        if (isDuplicate) {
          console.warn(`중복된 아이템이 감지되었습니다: ${newItemKey}`);
          return {data};
        }

        const params = state.params;
        const sortBy = (params as any)['sortBy'];
        const sortDirection = (params as any)['sortDirection'];

        // 정렬 기준이 없으면 맨 앞에 추가
        if (!sortBy || !sortDirection) {
          return {data: [newItem, ...data]};
        }

        // 정렬된 위치 찾기
        const newItems = [...data, newItem];
        const sortedData = newItems.sort((a, b) => {
          const aValue = (a as any)[sortBy];
          const bValue = (b as any)[sortBy];

          if (sortDirection === 'ASCENDING') {
            return aValue > bValue ? 1 : -1;
          } else {
            return aValue < bValue ? 1 : -1;
          }
        });
        return {data: sortedData};
      })
    },
    update: (id, newData) => {
      set((state) => ({
        data: state.data.map((item) =>
            keyExtractor(item) === id ? { ...item, ...newData } : item
        ),
      }))
    },
    delete: (id) => {
      set((state) => ({
        data: state.data.filter((item) => keyExtractor(item) !== id),
      }))
    },
    count: () => get().data.length,

    params: _initialData.params,
    updateParams: (newParams, options) => {
      const _options = {autoFetch: true, ...options};
      set((state) => ({
        params: { ...state.params, ...newParams },
      }))
      if (_options.autoFetch) {
        get().fetch();
      }
    },

    fetch: async (options) => {
      const {loading} = get();
      if (loading && options?.ignoreLoading !== true) {
        console.warn(`로딩 중이므로 요청이 무시되었습니다.`);
        return;
      }

      try {
        set({loading: true, error: undefined, data: []});

        const {params} = get();
        const result = await fetchApi(params);

        set({data: result});

      } catch (error) {
        console.error(error);
        set({error: (error as Error).message || '알 수 없는 오류가 발생했습니다.'});

        if (options?.throwError) {
          throw error;
        }
      } finally {
        set({loading: false});
      }
    },
    clearData: () => {
      set({
        ..._initialData,
        loading: false,
      })
    },

    loading: false,

    error: undefined,
    clearError: () => {set({error: undefined})},

    clear: () => {
      const { clearData, clearError } = get();
      clearData();
      clearError();
    }
  })
}

export function createPaginatedStoreActions<T, P extends CursorParams>(
    {set, get, fetchApi, initialData, keyExtractor = e => (e as never)['id'] as string} : {
      set: SetType<PaginatedStore<T, P>>,
      get: () => PaginatedStore<T, P>,
      fetchApi: (p: P) => Promise<CursorResponse<T>>,
      initialData?: Partial<{
        data: T[];
        params: P;
        cursorState: CursorState;
      }>,
      keyExtractor?: (e: T) => string
    }
): PaginatedStore<T, P> {
  const _initialData = {data: [], params: {limit: 20} as P, cursorState: {hasNext: false, totalCount: 0}, ...initialData};
  return ({
    data: _initialData.data,
    add: newItem => {
      set(state => {
        const data = state.data;
        const newItemKey = keyExtractor(newItem);
        const isDuplicate = data.some(item => keyExtractor(item) === newItemKey);
        if (isDuplicate) {
          console.warn(`중복된 아이템이 감지되었습니다: ${newItemKey}`);
          return {data};
        }

        const cursorState: CursorState = {...state.cursorState, totalCount: state.cursorState.totalCount+1}

        const params = state.params;
        const sortBy = (params as any)['sortBy'];
        const sortDirection = (params as any)['sortDirection'];

        // 정렬 기준이 없으면 맨 앞에 추가
        if (!sortBy || !sortDirection) {
          return {data: [newItem, ...data], cursorState};
        }

        // 정렬된 위치 찾기
        const newItems = [...data, newItem];
        const sortedData = newItems.sort((a, b) => {
          const aValue = (a as any)[sortBy];
          const bValue = (b as any)[sortBy];

          if (sortDirection === 'ASCENDING') {
            return aValue > bValue ? 1 : -1;
          } else {
            return aValue < bValue ? 1 : -1;
          }
        });
        return {data: sortedData, cursorState};
      })
    },
    update: (id, newData) => {
      set((state) => ({
        data: state.data.map((item) =>
            keyExtractor(item) === id ? { ...item, ...newData } : item
        ),
      }))
    },
    delete: (id) => {
      set((state) => ({
        data: state.data.filter((item) => keyExtractor(item) !== id),
        cursorState: {...state.cursorState, totalCount: state.cursorState.totalCount-1}
      }))
    },
    count: () => get().cursorState.totalCount,

    params: _initialData.params,
    updateParams: (newParams, options) => {
      const _options = {autoFetch: true, ...options};
      set((state) => ({
        params: { ...state.params, ...newParams },
      }))
      if (_options.autoFetch) {
        get().fetch();
      }
    },
    cursorState: _initialData.cursorState,
    hasNext: () => get().cursorState.hasNext,

    fetch: async (options) => {
      const {loading} = get();
      if (loading && options?.ignoreLoading !== true) {
        console.warn(`로딩 중이므로 요청이 무시되었습니다.`);
        return;
      }

      try {
        set({loading: true, error: undefined, data: []});

        const {params} = get();
        const result = await fetchApi({...params, cursor: undefined, idAfter: undefined} as P);

        set({
          data: result.data,
          cursorState: {
            nextCursor: result.nextCursor,
            nextIdAfter: result.nextIdAfter,
            hasNext: result.hasNext,
            totalCount: result.totalCount,
          },
        });

      } catch (error) {
        console.error(error);
        set({error: (error as Error).message || '알 수 없는 오류가 발생했습니다.'});

        if (options?.throwError) {
          throw error;
        }
      } finally {
        set({loading: false});
      }
    },
    fetchMore: async (options) => {
      const {loading, cursorState} = get();
      if (loading && options?.ignoreLoading !== true) {
        console.warn(`로딩 중이므로 요청이 무시되었습니다.`);
        return;
      }
      if (!cursorState.hasNext) {
        console.warn(`더 이상 데이터가 없으므로 요청이 무시되었습니다.`);
        return;
      }

      try {
        set({loading: true, error: undefined});

        const {params, data} = get();
        const result = await fetchApi({...params, cursor: cursorState.nextCursor, idAfter: cursorState.nextIdAfter} as P);

        set({
          data: [...data, ...result.data],
          cursorState: {
            nextCursor: result.nextCursor,
            nextIdAfter: result.nextIdAfter,
            hasNext: result.hasNext,
            totalCount: result.totalCount,
          },
        });

      } catch (error) {
        console.error(error);
        set({error: (error as Error).message || '알 수 없는 오류가 발생했습니다.'});

        if (options?.throwError) {
          throw error;
        }
      } finally {
        set({loading: false});
      }
    },
    clearData: () => {
      set({
        ..._initialData,
        loading: false,
      })
    },

    loading: false,

    error: undefined,
    clearError: () => {set({error: undefined})},

    clear: () => {
      const { clearData, clearError } = get();
      clearData();
      clearError();
    }
  })
}