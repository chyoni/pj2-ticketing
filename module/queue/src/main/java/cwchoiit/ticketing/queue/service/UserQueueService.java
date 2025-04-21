package cwchoiit.ticketing.queue.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.function.Predicate;

import static cwchoiit.ticketing.queue.exception.ErrorCode.QUEUE_ALREADY_REGISTERED_USER;

@Service
@RequiredArgsConstructor
public class UserQueueService {

    private static final String USER_QUEUE_WAIT_KEY = "user:queue:%s:wait";
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    /**
     * 대기열에 현재 요청한 유저를 추가한다.
     * 만약, 현재 요청한 유저가 이미 대기열에 추가된 상태라면, 에러를 반환한다.
     * 만약, 현재 요청한 유저가 대기열에 없다면 추가한 뒤, 순번에 대한 Long 값을 반환한다.
     *
     * @param userId 요청한 유저의 ID
     * @return 대기 순번
     */
    public Mono<Long> registerWaitQueue(final String queueName, final Long userId) {
        long unixTimestamp = Instant.now().getEpochSecond();

        return reactiveRedisTemplate.opsForZSet()
                .add(generateKey(queueName), userId.toString(), unixTimestamp)
                .filter(Predicate.isEqual(true))
                .switchIfEmpty(Mono.error(QUEUE_ALREADY_REGISTERED_USER.build()))
                .flatMap(success -> reactiveRedisTemplate.opsForZSet()
                        .rank(generateKey(queueName), userId.toString())
                        .map(rank -> rank + 1)
                );
    }

    private String generateKey(final String queueName) {
        return String.format(USER_QUEUE_WAIT_KEY, queueName);
    }
}
