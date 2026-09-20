package com.example.notification_for_userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableKafka
public class NotificationForUserserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationForUserserviceApplication.class, args);
	}

}
