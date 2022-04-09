package com.example.microservices.core.location;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.example")
public class LocationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LocationServiceApplication.class, args);
	}

}
