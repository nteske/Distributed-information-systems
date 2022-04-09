package microservices.core.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;	
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ComponentScan
public class HotelServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelServiceApplication.class, args);
	}

}