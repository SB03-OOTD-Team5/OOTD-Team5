package com.sprint.ootd5team.base.sse.repository.emitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 사용자별 SseEmitter 컬렉션을 관리하는 인메모리 저장소
 * <p>
 * 사용자별로 다수의 SSE 연결(탭/디바이스 등)을 보유할 수 잇음
 * 연결 수명 관리는 상위 서비스 계층에서 수행,
 * 이 저장소는 단순한 보관/조회/삭제 기능 제공
 */
@Slf4j
@Repository
public class SseEmitterRepository {

    // 사용자별 SseEmitter 목록을 보관하는 스레드 안전한 맵
    private final ConcurrentMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

    /**
     * 지정한 사용자 컬렉션에 SseEmitter를 추가
     *
     * @param userId     사용자 ID
     * @param sseEmitter 추가할 Emitter
     */
    public void add(UUID userId, SseEmitter sseEmitter) {
        List<SseEmitter> list = data.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
        list.add(sseEmitter);
        log.info(
            "[SseEmitterRepository] SSE Emitter 등록: userId={}, totalForUser={}, emitterHash={}",
            userId, list.size(), Integer.toHexString(System.identityHashCode(sseEmitter)));

    }

    /**
     * 사용자에 대한 Emitter 목록을 조회
     * 존재하지 않으면 빈 목록을 반환
     *
     * @param userId 사용자 ID
     * @return 사용자에 등록된 Emitter 목록(수정 불가 뷰가 아님에 유의)
     */
    public List<SseEmitter> get(UUID userId) {
        List<SseEmitter> list = data.get(userId);
        int count = list == null ? 0 : list.size();
        log.debug("[SseEmitterRepository] SSE Emitter 조회: userId={}, count={}", userId, count);
        return list == null ? List.of() : List.copyOf(list);

    }

    /**
     * 사용자 컬렉션에서 특정 SseEmitter를 제거
     * 사용자의 목록이 없으면 동작하지 않음
     *
     * @param userId     사용자 ID
     * @param sseEmitter 제거할 Emitter
     */
    public void remove(UUID userId, SseEmitter sseEmitter) {
        List<SseEmitter> sseEmitters = data.get(userId);
        if (sseEmitters != null) {
            boolean removed = sseEmitters.remove(sseEmitter);
            log.info(
                "[SseEmitterRepository] SSE Emitter 제거: userId={}, removed={}, remainingForUser={}, emitterHash={}",
                userId, removed, sseEmitters.size(),
                Integer.toHexString(System.identityHashCode(sseEmitter)));
            //컬렉션이 비었다면 키 정리
            if (sseEmitters.isEmpty()) {
                data.remove(userId, sseEmitters);
                log.debug("[SseEmitterRepository] SSE 사용자 키 정리: userId={}", userId);
            }
        } else {
            log.debug("[SseEmitterRepository] SSE Emitter 제거 요청: 사용자 목록 없음 userId={}", userId);
        }
    }

    /**
     * 전체 사용자-Emitter 매핑을 반환
     * 주로 모니터링/디버깅 용도
     */
    public Map<UUID, List<SseEmitter>> findAll() {
        log.debug("[SseEmitterRepository] SSE 저장소 전체 조회: userCount={}", data.size());
        return data.entrySet().stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                e -> List.copyOf(e.getValue())
            ));
    }

}
