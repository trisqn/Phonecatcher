package com.example.javaproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bootstraps the scam-reporting application and enables scheduled tasks.
 */
@SpringBootApplication
@EnableScheduling
public class JavaProjectApplication {

    /**
     * Creates the application entry-point class.
     */
    public JavaProjectApplication() {
    }

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line application arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(JavaProjectApplication.class, args);
    }

}
