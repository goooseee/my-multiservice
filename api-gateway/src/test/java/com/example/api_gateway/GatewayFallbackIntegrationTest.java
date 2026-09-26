package com.example.api_gateway;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GatewayFallbackIntegrationTest {

    @LocalServerPort
    private int port;

    private WebTestClient webClient;

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("wiremock.server.port", wiremock::getPort);
    }

    @BeforeEach
    void setUp() {
        this.webClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/users/1: Успешный ответ проксируется с HATEOAS")
    void shouldReturnUserData_WhenUserServiceIsHealthy() {
        wiremock.stubFor(get(urlEqualTo("/api/v1/users/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/hal+json")
                        .withBody("""
                            {
                              "id": 1,
                              "username": "ivan",
                              "_links": {
                                "self": { "href": "http://localhost/api/v1/users/1" }
                              }
                            }
                        """)
                        .withStatus(200)));

        webClient.get()
                .uri("/api/v1/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$._links.self.href").exists();
    }

    @Test
    @DisplayName("GET /api/v1/users/slow: Таймаут -> Fallback 503")
    void shouldReturnFallback_WhenUserServiceTimesOut() {
        wiremock.stubFor(get(urlEqualTo("/api/v1/users/slow"))
                .willReturn(aResponse()
                        .withFixedDelay(2500)
                        .withStatus(200)));

        webClient.get()
                .uri("/api/v1/users/slow")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.status").isEqualTo("503")
                .jsonPath("$.error").isEqualTo("User Service временно недоступен")
                .jsonPath("$.message").isEqualTo("Пожалуйста, повторите попытку позже.");
    }

    @Test
    @DisplayName("POST /api/v1/notifications/send: Сбой 500 -> Fallback 503")
    void shouldReturnFallback_WhenNotificationServiceFails() {
        wiremock.stubFor(post(urlEqualTo("/api/v1/notifications/send"))
                .willReturn(aResponse().withStatus(500)));

        webClient.post()
                .uri("/api/v1/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"email\":\"test@test.com\",\"eventType\":\"USER_CREATED\"}")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.status").isEqualTo("503")
                .jsonPath("$.error").isEqualTo("Notification Service временно недоступен")
                .jsonPath("$.message").isEqualTo("Уведомление будет отправлено позже.");
    }
}