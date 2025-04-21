package cwchoiit.ticketing.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SiteController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
