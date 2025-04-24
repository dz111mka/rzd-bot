package ru.chepikov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class RzdbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(RzdbotApplication.class, args);
	}
}
