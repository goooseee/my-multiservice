package com.example.notification_for_userservice.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.notification_for_userservice.dto.UserEventDTO.EventType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
	
	//private final JavaMailSender mailSender;
	@Async
	public void processAndSend(String email, EventType actionType) {
        String subject;
        String text;

        if (actionType == EventType.CREATED) {
            subject = "Уведомление о регистрации";
            text = "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.";
        } else {
            subject = "Уведомление об удалении";
            text = "Здравствуйте! Ваш аккаунт был удалён.";
        }

        sendEmail(email, subject, text);
    }
	
	private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        //mailSender.send(message);
        log.info("Email successfully sent to {}", to);
    }
	
}
