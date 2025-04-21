package cwchoiit.ticketing.queue.controller;

import cwchoiit.ticketing.queue.service.UserQueueService;
import cwchoiit.ticketing.queue.service.response.AllowUserResponse;
import cwchoiit.ticketing.queue.service.response.AllowedUserResponse;
import cwchoiit.ticketing.queue.service.response.RegisterUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping("/allow")
    public Mono<AllowUserResponse> allowUser(@RequestParam(value = "queueName", defaultValue = "default") final String queueName,
                                             @RequestParam("count") final Long count) {
        return userQueueService.allowUser(queueName, count)
                .map(allowed -> new AllowUserResponse(count, allowed));
    }

    @GetMapping("/allowed")
    public Mono<AllowedUserResponse> isAllowedUser(@RequestParam(value = "queueName", defaultValue = "default") final String queueName,
                                                   @RequestParam("userId") final Long userId) {
        return userQueueService.isAllowed(queueName, userId).map(AllowedUserResponse::new);
    }
}
