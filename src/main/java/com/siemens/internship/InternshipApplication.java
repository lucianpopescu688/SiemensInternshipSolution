package com.siemens.internship;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/*
 * This is the main entry point for the Spring Boot application.
 * It uses @EnableAsync to enable asynchronous processing in the application.
 */
@SpringBootApplication
@EnableAsync
public class InternshipApplication {

	public static void main(String[] args) {
		SpringApplication.run(InternshipApplication.class, args);
	}

}
