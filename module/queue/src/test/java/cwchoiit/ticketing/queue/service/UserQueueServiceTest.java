package cwchoiit.ticketing.queue.service;

import cwchoiit.ticketing.queue.EmbeddedRedis;
import cwchoiit.ticketing.queue.exception.QueueAppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
@Import(EmbeddedRedis.class)
@DisplayName("Service - UserQueueService")
class UserQueueServiceTest {

    @Autowired
    private UserQueueService userQueueService;

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @BeforeEach
    public void beforeEach() {
        ReactiveRedisConnection reactiveConnection = reactiveRedisTemplate.getConnectionFactory().getReactiveConnection();
        reactiveConnection.serverCommands().flushAll().subscribe();
    }

    @Test
    @DisplayName("대기열 큐에 유저 등록")
    void registerWaitQueue() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 1L))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(userQueueService.registerWaitQueue("default", 2L))
                .expectNext(2L)
                .verifyComplete();

        StepVerifier.create(userQueueService.registerWaitQueue("default", 3L))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    @DisplayName("대기열 큐에 이미 등록된 유저를 재등록시, 에러 발생")
    void alreadyRegisterWaitQueue() {
        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(userQueueService.registerWaitQueue("default", 100L))
                .expectError(QueueAppException.class)
                .verify();
    }

    @Test
    @DisplayName("진입 가능 큐에 유저를 지정한 수만큼 추가하려고 시도 - 대기열 큐에 유저가 한명도 없으면 0을 반환")
    void emptyAllowUser() {
        StepVerifier.create(userQueueService.allowUser("default", 3L))
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    @DisplayName("진입 가능 큐에 유저를 지정한 수만큼 추가하려고 시도 - 대기열 큐에 유저가 지정한 수 이상으로 존재하므로 지정한 수 반환")
    void allowUser() {
        StepVerifier.create(
                        userQueueService.registerWaitQueue("default", 1000L)
                                .then(userQueueService.registerWaitQueue("default", 1001L))
                                .then(userQueueService.registerWaitQueue("default", 1002L))
                                .then(userQueueService.registerWaitQueue("default", 1003L))
                                .then(userQueueService.allowUser("default", 3L))
                ).expectNext(3L)
                .verifyComplete();
    }

    @Test
    @DisplayName("진입 가능 큐에 유저를 지정한 수만큼 추가하려고 시도 - 대기열 큐에 유저가 지정한 수보다 적으므로 대기열 큐 전체 유저 수 반환")
    void allowUser2() {
        StepVerifier.create(
                        userQueueService.registerWaitQueue("default", 1000L)
                                .then(userQueueService.registerWaitQueue("default", 1001L))
                                .then(userQueueService.registerWaitQueue("default", 1002L))
                                .then(userQueueService.registerWaitQueue("default", 1003L))
                                .then(userQueueService.allowUser("default", 10L))
                ).expectNext(4L)
                .verifyComplete();
    }

    @Test
    @DisplayName("대기열 큐에 유저를 전부 비운 후 새로운 유저를 추가하면 순번은 1이 나와야 한다.")
    void allowUserAfterRegisterWaitQueue() {
        StepVerifier.create(
                        userQueueService.registerWaitQueue("default", 1000L)
                                .then(userQueueService.registerWaitQueue("default", 1001L))
                                .then(userQueueService.registerWaitQueue("default", 1002L))
                                .then(userQueueService.registerWaitQueue("default", 1003L))
                                .then(userQueueService.allowUser("default", 4L))
                                .then(userQueueService.registerWaitQueue("default", 1004L))
                ).expectNext(1L)
                .verifyComplete();
    }

    @Test
    @DisplayName("진입 가능 큐에 유저를 넣지 않은 상태에서 진입 가능 여부 확인을 시도하면 false")
    void isNotAllowed() {
        StepVerifier.create(userQueueService.isAllowed("default", 1000L))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("진입 가능 큐에 유저를 넣은 후, 전혀 상관없는 유저에 대해 진입 가능 여부 확인을 시도하면 false")
    void isNotAllowed2() {
        StepVerifier.create(
                        userQueueService.registerWaitQueue("default", 1000L)
                                .then(userQueueService.allowUser("default", 3L))
                                .then(userQueueService.isAllowed("default", 101L))
                )
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("진입 가능 큐에 유저를 넣은 후, 해당 유저에 대해 진입 가능 여부 확인을 시도하면 true")
    void isAllowed() {
        StepVerifier.create(
                        userQueueService.registerWaitQueue("default", 1000L)
                                .then(userQueueService.allowUser("default", 3L))
                                .then(userQueueService.isAllowed("default", 1000L))
                )
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("대기 순번 확인 - 정상 케이스")
    void getRank() {
        StepVerifier.create(
                        userQueueService.registerWaitQueue("default", 1000L)
                                .then(userQueueService.getRank("default", 1000L))
                )
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(
                        userQueueService.registerWaitQueue("default", 1001L)
                                .then(userQueueService.getRank("default", 1001L))
                )
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    @DisplayName("대기 순번 확인 - 없는 유저를 찾으면 0 반환")
    void emptyRank() {
        StepVerifier.create(userQueueService.getRank("default", 5L))
                .expectNext(0L)
                .verifyComplete();
    }
}