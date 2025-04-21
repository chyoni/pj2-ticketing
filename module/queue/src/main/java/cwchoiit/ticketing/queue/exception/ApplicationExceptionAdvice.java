package cwchoiit.ticketing.queue.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class ApplicationExceptionAdvice {

    @ExceptionHandler(QueueAppException.class)
    public Mono<ResponseEntity<ServerExceptionResponse>> queueAppExceptionHandler(QueueAppException exception) {
        return Mono.just(
                ResponseEntity
                        .status(exception.getStatus())
                        .body(new ServerExceptionResponse(exception.getCode(), exception.getReason()))
        );
    }

    public record ServerExceptionResponse(String code, String reason) {
    }
}
