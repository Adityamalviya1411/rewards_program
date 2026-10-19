package com.charter.rewards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Kicks off the Charter Rewards application.
 *
 * Spring will wire everything up, connect to PostgreSQL, let Hibernate
 * create the tables, and seed the sample data from db/data.sql.
 */
@SpringBootApplication
public class RewardsApplication {

    /**
     * Main entry point — just starts Spring Boot.
     *
     * @param args any command-line args passed in at startup
     */
    public static void main(String[] args) {
        SpringApplication.run(RewardsApplication.class, args);
    }
}
