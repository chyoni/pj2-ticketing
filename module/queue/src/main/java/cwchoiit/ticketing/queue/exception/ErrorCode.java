package cwchoiit.ticketing.queue.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ErrorCode {

    QUEUE_ALREADY_REGISTERED_USER(HttpStatus.CONFLICT, "UQ-001", "Already registered user in queue");

    private final HttpStatus status;
    private final String code;
    private final String reason;

    public QueueAppException build() {
        return new QueueAppException(status, code, reason);
    }

    public QueueAppException build(Object... args) {
        return new QueueAppException(status, code, reason.formatted(args));
    }
}
