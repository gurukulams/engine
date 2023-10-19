package com.techatpark.workout.controller;

import com.gurukulams.core.model.Annotations;
import com.techatpark.workout.service.LearnerService;
import com.techatpark.workout.starter.security.config.AppProperties;
import com.techatpark.workout.starter.security.payload.AuthenticationRequest;
import com.techatpark.workout.starter.security.payload.AuthenticationResponse;
import com.techatpark.workout.starter.security.payload.RegistrationRequest;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.StatusAssertions;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AnnotationAPIControllerTest {
    @Value(value = "${local.server.port}")
    private int port;

    private final AuthenticationRequest signupRequest;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private LearnerService learnerService;

    @Autowired
    private AppProperties appProperties;

    AnnotationAPIControllerTest() {
        this.signupRequest = new AuthenticationRequest(
                "tom@email.com",
                "password");
    }

    @DynamicPropertySource
    static void authProperties(DynamicPropertyRegistry registry) {
        registry.add("app.auth.tokenExpirationMsec", () -> 1500);
    }

    @BeforeEach
    void before() throws SQLException {
        cleanup();
    }

    @AfterEach
    void after() throws SQLException {
        cleanup();
    }

    void cleanup() throws SQLException {
        learnerService.delete();
        this.webTestClient
                .post()
                .uri("/api/auth/login")
                .body(Mono.just(signupRequest), AuthenticationRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK.value());
    }
    @Test
    void testBasic() {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                this.signupRequest.getUserName(),
                this.signupRequest.getPassword());

        AuthenticationResponse authenticationResponse =
                login(authenticationRequest);

        authenticationResponse = register(authenticationRequest,
                authenticationResponse);

        Annotations annotations = create(authenticationResponse);

        List<Annotations> annotationsList = list(authenticationResponse) ;

        logout(authenticationRequest, authenticationResponse).isEqualTo(HttpStatus.OK.value());


    }

    private List<Annotations> list(final AuthenticationResponse authenticationResponse) {

        return this.webTestClient
                .get()
                .uri("/api/annotations/books/c-programing/foundations")
                .header("Authorization",
                        "Bearer " + authenticationResponse.getAuthToken())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(Annotations.class)
                .returnResult()
                .getResponseBody();
    }

    private Annotations create(final AuthenticationResponse authenticationResponse) {

        return this.webTestClient
                .post()
                .uri("/api/annotations/books/c-programing/foundations")
                .body(Mono.just(anAnnotation()),
                        Annotations.class)
                .header("Authorization",
                        "Bearer " + authenticationResponse.getAuthToken())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(Annotations.class)
                .returnResult()
                .getResponseBody();
    }

    private StatusAssertions logout(final AuthenticationRequest authenticationRequest, final AuthenticationResponse authenticationResponse) {
        return this.webTestClient
                .post()
                .uri("/api/auth/logout")
                .body(Mono.just(authenticationRequest),
                        AuthenticationRequest.class)
                .header("Authorization",
                        "Bearer " + authenticationResponse.getAuthToken())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus();
    }

    private AuthenticationResponse register(final AuthenticationRequest authenticationRequest,
                                            final AuthenticationResponse authenticationResponse) {

        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setName("Sathish Kumar");
        registrationRequest.setAadhar("3675 9834 6012");
        registrationRequest.setDob(LocalDate.now().minusYears(20L));

        return this.webTestClient
                .post()
                .uri("/api/auth/register")
                .body(Mono.just(registrationRequest), RegistrationRequest.class)
                .header("Authorization",
                        "Bearer " + authenticationResponse.getRegistrationToken())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CREATED.value())
                .expectBody(AuthenticationResponse.class)
                .returnResult().getResponseBody();
    }

    private AuthenticationResponse login(final AuthenticationRequest authenticationRequest) {
        AuthenticationResponse authenticationResponse = this.webTestClient
                .post()
                .uri("/api/auth/login")
                .body(Mono.just(authenticationRequest),
                        AuthenticationRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK.value())
                .expectBody(AuthenticationResponse.class)
                .returnResult().getResponseBody();
        return authenticationResponse;
    }

    private Annotations anAnnotation() {
        Annotations annotation = new Annotations();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("a", "a");
        jsonObject.put("b", "b");
        annotation.setJsonValue(jsonObject);
        return annotation;
    }

}