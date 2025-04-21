package cwchoiit.ticketing.queue.controller;

import cwchoiit.ticketing.queue.service.UserQueueService;
import cwchoiit.ticketing.queue.service.response.RegisterUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/queue")
public class UserQueueController {

    private final UserQueueService userQueueService;

    @PostMapping
    public Mono<RegisterUserResponse> registerUser(@RequestParam("userId") final Long userId,
                                                   @RequestParam(value = "queueName", defaultValue = "default") String queueName) {
        return userQueueService.registerWaitQueue(queueName, userId).map(RegisterUserResponse::new);
    }
}
