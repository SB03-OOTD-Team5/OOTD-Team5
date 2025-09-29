import { useEffect, useRef } from 'react';

interface UseInfiniteScrollOptions {
  onLoadMore: () => void;
  rootMargin?: string;
  threshold?: number;
}

export function useInfiniteScroll({
  onLoadMore,
  rootMargin = '100px',
  threshold = 0.1
}: UseInfiniteScrollOptions) {
  const targetRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const target = targetRef.current;
    if (!target) return;

    const observer = new IntersectionObserver(
      (entries) => {
        const intersected = entries.some(entry => entry.isIntersecting);
        if (intersected) {
          onLoadMore();
        }
      },
      {
        rootMargin,
        threshold
      }
    );

    observer.observe(target);

    return () => {
      observer.unobserve(target);
    };
  }, [onLoadMore, rootMargin, threshold, targetRef]);

  return { ref: targetRef };
}