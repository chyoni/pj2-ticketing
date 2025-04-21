package cwchoiit.ticketing.queue.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class QueueAppException extends RuntimeException {
    private HttpStatus status;
    private String code;
    private String reason;
}
