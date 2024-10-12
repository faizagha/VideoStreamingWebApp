package com.faiz.videostreaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@CrossOrigin("*")
public class VideoStreamingApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoStreamingApplication.class, args);
    }

}
