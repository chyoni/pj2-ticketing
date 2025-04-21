package cwchoiit.ticketing.queue.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Objects;
import java.util.function.Predicate;

import static cwchoiit.ticketing.queue.exception.ErrorCode.QUEUE_ALREADY_REGISTERED_USER;

@Service
@RequiredArgsConstructor
public class UserQueueService {

    private static final String USER_QUEUE_WAIT_KEY = "user:queue:%s:wait";
    private static final String USER_QUEUE_PROCEED_KEY = "user:queue:%s:proceed";
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    /**
     * 대기열에 현재 요청한 유저를 추가한다.
     * 만약, 현재 요청한 유저가 이미 대기열에 추가된 상태라면, 에러를 반환한다.
     * 만약, 현재 요청한 유저가 대기열에 없다면 추가한 뒤, 순번에 대한 Long 값을 반환한다.
     *
     * @param queueName 대기열 큐 이름 (없으면 default)
     * @param userId 요청한 유저의 ID
     * @return 대기 순번
     */
    public Mono<Long> registerWaitQueue(final String queueName, final Long userId) {
        long unixTimestamp = Instant.now().getEpochSecond();

        return reactiveRedisTemplate.opsForZSet()
                .add(generateKey(USER_QUEUE_WAIT_KEY, queueName), userId.toString(), unixTimestamp)
                .filter(Predicate.isEqual(true))
                .switchIfEmpty(Mono.error(QUEUE_ALREADY_REGISTERED_USER.build()))
                .flatMap(success -> reactiveRedisTemplate.opsForZSet()
                        .rank(generateKey(USER_QUEUE_WAIT_KEY, queueName), userId.toString())
                        .map(rank -> rank + 1)
                );
    }

    /**
     * 진입 가능 큐에 전달받은 개수{@code count}만큼 대기열에 있는 유저를 이동시킨다.
     * @param queueName 대기열 큐와 진입 가능 큐 이름 (없으면 default)
     * @param count 진입 가능 큐에 넣을 유저 수
     * @return 진입 가능 큐에 실제로 들어간 유저 수
     */
    public Mono<Long> allowUser(final String queueName, final Long count) {
        return reactiveRedisTemplate.opsForZSet()
                .popMin(generateKey(USER_QUEUE_WAIT_KEY, queueName), count)
                .flatMap(user -> reactiveRedisTemplate.opsForZSet()
                        .add(
                                generateKey(USER_QUEUE_PROCEED_KEY, queueName),
                                Objects.requireNonNull(user.getValue()),
                                Instant.now().getEpochSecond()
                        )
                )
                .count();
    }

    /**
     * 진입 가능 큐에 전달받은 유저가 속해있는지 확인한다.
     * @param queueName 진입 가능 큐 이름 (없으면 default)
     * @param userId 유저 ID
     * @return 진입 가능 큐에 유저가 들어와있다면 {@code true}, 그렇지 않으면 {@code false}
     */
    public Mono<Boolean> isAllowed(final String queueName, final Long userId) {
        return reactiveRedisTemplate.opsForZSet()
                .rank(generateKey(USER_QUEUE_PROCEED_KEY, queueName), userId.toString())
                .defaultIfEmpty(-1L)
                .map(rank -> rank >= 0);
    }

    private String generateKey(final String prefix, final String queueName) {
        return String.format(prefix, queueName);
    }
}
