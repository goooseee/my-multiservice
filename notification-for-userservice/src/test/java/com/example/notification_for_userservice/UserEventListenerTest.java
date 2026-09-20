package com.example.notification_for_userservice;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.notification_for_userservice.dto.UserEventDTO;
import com.example.notification_for_userservice.dto.UserEventDTO.EventType;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;
@SpringBootTest(properties = {
	    "spring.kafka.consumer.auto-offset-reset=earliest",
	    "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
	    "spring.kafka.consumer.properties.spring.json.trusted.packages=*",
	    "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer"
	})
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	@EmbeddedKafka(
	    partitions = 1,
	    topics = {"user-events"},
	    bootstrapServersProperty = "spring.kafka.bootstrap-servers"
	)
public class UserEventListenerTest {
	
	@Autowired
    private KafkaTemplate<String, UserEventDTO> kafkaTemplate;

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    void shouldReceiveCreatedEventFromKafkaAndSendEmail() {
        String email = "test-created@example.com";
        UserEventDTO event = new UserEventDTO(email, EventType.CREATED);

        kafkaTemplate.send("user-events", email, event);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(messageCaptor.capture());

            SimpleMailMessage sentMessage = messageCaptor.getValue();
            assertThat(sentMessage.getTo()).containsExactly(email);
            assertThat(sentMessage.getText()).contains("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.");
        });
    }

    @Test
    void shouldReceiveDeletedEventFromKafkaAndSendEmail() {
        String email = "test-deleted@example.com";
        UserEventDTO event = new UserEventDTO(email, EventType.DELETED);

        kafkaTemplate.send("user-events", email, event);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(messageCaptor.capture());

            SimpleMailMessage sentMessage = messageCaptor.getValue();
            assertThat(sentMessage.getTo()).containsExactly(email);
            assertThat(sentMessage.getText()).contains("Здравствуйте! Ваш аккаунт был удалён.");
        });
    }
	
}
