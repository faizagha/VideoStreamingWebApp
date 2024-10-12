package com.faiz.videostreaming;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class healthCheckUp {
    @GetMapping("/health")
    public String healthCheck() {
        return "I am healthy";
    }
}
