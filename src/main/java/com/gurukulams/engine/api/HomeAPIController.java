package com.gurukulams.engine.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeAPIController {

    @GetMapping("/api")
    public String index() {
        return "Greetings 2!";
    }

}
