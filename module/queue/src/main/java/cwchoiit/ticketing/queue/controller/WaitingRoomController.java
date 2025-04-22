package cwchoiit.ticketing.queue.controller;

import cwchoiit.ticketing.queue.service.UserQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Controller
@RequiredArgsConstructor
public class WaitingRoomController {

    private final UserQueueService userQueueService;

    @GetMapping("/waiting-room")
    public Mono<Rendering> waitingRoom(@RequestParam(value = "queueName", defaultValue = "default") final String queueName,
                                       @RequestParam("userId") final Long userId,
                                       @RequestParam("redirectUrl") String redirectUrl) {
        return userQueueService.isAllowed(queueName, userId)
                .filter(Predicate.isEqual(true))
                .flatMap(allowed -> Mono.just(Rendering.redirectTo(redirectUrl).build()))
                .switchIfEmpty(userQueueService.registerWaitQueue(queueName, userId)
                        .onErrorResume(ex -> userQueueService.getRank(queueName, userId)) // registerWaitQueue 에서 에러가 발생하는 건, 유저가 이미 큐에 등록된 상태일때
                        .map(rank -> Rendering.view("waiting-room")
                                .modelAttribute("number", rank)
                                .modelAttribute("userId", userId)
                                .modelAttribute("queue", queueName)
                                .build()
                        )
                );
    }
}
