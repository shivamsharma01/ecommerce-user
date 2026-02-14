package com.mcart.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * User profile microservice.
 * <p>
 * Consumes user signup events from the auth service via Pub/Sub and exposes
 * the current user's profile via the /user/me endpoint.
 * </p>
 */
@SpringBootApplication
public class UserApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
