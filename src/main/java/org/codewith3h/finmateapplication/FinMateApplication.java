package org.codewith3h.finmateapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "org.codewith3h.finmateapplication")
@EnableAsync
public class FinMateApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinMateApplication.class, args);
    }
}