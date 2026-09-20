package com.example.notification_for_userservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.notification_for_userservice.dto.UserEventDTO;
import com.example.notification_for_userservice.dto.UserEventDTO.EventType;
import com.example.notification_for_userservice.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {
	
	private final EmailService emailService;
	
	@KafkaListener(topics = "user-events", groupId = "notification-group")
    public void handleUserEvent(UserEventDTO event) {
		log.info("Получено сообщение из Kafka: email={}, type={}", event.email(), event.eventType());
        EventType actionType = EventType.valueOf(event.eventType().name());
        emailService.processAndSend(event.email(), actionType);
    }
	
}
