package cwchoiit.ticketing.queue.controller;

import cwchoiit.ticketing.queue.service.UserQueueService;
import cwchoiit.ticketing.queue.service.response.AllowUserResponse;
import cwchoiit.ticketing.queue.service.response.AllowedUserResponse;
import cwchoiit.ticketing.queue.service.response.RankResponse;
import cwchoiit.ticketing.queue.service.response.RegisterUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

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
                                                   @RequestParam(name = "token") String token,
                                                   @RequestParam("userId") final Long userId) {
        return userQueueService.isAllowedByToken(queueName, userId, token).map(AllowedUserResponse::new);
    }

    @GetMapping("/rank")
    public Mono<RankResponse> getRank(@RequestParam(value = "queueName", defaultValue = "default") final String queueName,
                                      @RequestParam("userId") final Long userId) {
        return userQueueService.getRank(queueName, userId).map(RankResponse::new);
    }

    @GetMapping("/touch")
    public Mono<?> touch(@RequestParam(value = "queueName", defaultValue = "default") final String queueName,
                  @RequestParam("userId") final Long userId,
                  ServerWebExchange exchange) {
        return Mono.defer(() -> userQueueService.generateToken(queueName, userId))
                .map(token -> {
                    exchange.getResponse().addCookie(ResponseCookie
                                    .from("user-queue-%s-token".formatted(queueName), token)
                                    .maxAge(Duration.ofSeconds(300))
                                    .path("/")
                                    .build());
                    return token;
                });
    }
}
