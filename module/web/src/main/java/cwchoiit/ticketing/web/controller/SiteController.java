package cwchoiit.ticketing.web.controller;

import cwchoiit.ticketing.web.response.AllowedUserResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;

@Controller
public class SiteController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/")
    public String index(@RequestParam(name = "queueName", defaultValue = "default") String queueName,
                        @RequestParam("userId") Long userId,
                        HttpServletRequest request) {

        String token = getTokenByCookie(queueName, request);

        URI uri = UriComponentsBuilder.fromUriString("http://127.0.0.1:9001")
                .path("/api/v1/queue/allowed")
                .queryParam("queueName", queueName)
                .queryParam("userId", userId)
                .queryParam("token", token)
                .encode()
                .build()
                .toUri();

        ResponseEntity<AllowedUserResponse> response = restTemplate.getForEntity(uri, AllowedUserResponse.class);
        if (response.getBody() == null || !response.getBody().allowed()) {
            return "redirect:http://127.0.0.1:9001/waiting-room?userId=%d&redirectUrl=%s"
                    .formatted(
                            userId,
                            "http://127.0.0.1:9000?userId=%d".formatted(userId)
                    );
        }

        return "index";
    }

    private String getTokenByCookie(String queueName, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String cookieName = "user-queue-%s-token".formatted(queueName);

        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equalsIgnoreCase(cookieName))
                    .findFirst()
                    .orElseGet(() -> new Cookie(cookieName, ""))
                    .getValue();
        }
        return "";
    }
}
