package com.example.microservices.core.room;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.example")
public class RoomServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomServiceApplication.class, args);
	}

}
