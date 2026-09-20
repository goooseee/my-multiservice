package com.example.notification_for_userservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.notification_for_userservice.dto.UserEventDTO;
import com.example.notification_for_userservice.service.EmailService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class EmailController {
	
	private final EmailService emailService;
	
	@PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@Valid @RequestBody UserEventDTO request) {
        emailService.processAndSend(request.email(), request.eventType());
        return ResponseEntity.ok().build();
    }
	
}
