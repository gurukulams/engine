package com.techatpark.workout.starter.security.controller;


import com.gurukulams.core.payload.RegistrationRequest;
import com.gurukulams.core.service.LearnerService;
import com.techatpark.workout.starter.security.config.AppProperties;
import com.techatpark.workout.starter.security.payload.AuthenticationRequest;
import com.techatpark.workout.starter.security.payload.AuthenticationResponse;
import com.techatpark.workout.starter.security.payload.RefreshToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.StatusAssertions;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AuthenticationAPIControllerTest {
    @Value(value = "${local.server.port}")
    private int port;

    private final AuthenticationRequest signupRequest;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private LearnerService learnerService;

    @Autowired
    private AppProperties appProperties;

    AuthenticationAPIControllerTest() {
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
    public void basicLogin() throws Exception {

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                this.signupRequest.getEmail(),
                this.signupRequest.getPassword());

        AuthenticationResponse authenticationResponse =
                login(authenticationRequest);

        getMe(authenticationResponse).isEqualTo(HttpStatus.UNAUTHORIZED.value());

        authenticationResponse = register(authenticationRequest,
                authenticationResponse);

        getMe(authenticationResponse).isEqualTo(HttpStatus.OK.value());

        refresh(authenticationResponse.getAuthToken(),
                authenticationResponse.getRefreshToken()).isEqualTo(HttpStatus.UNAUTHORIZED.value());

        // Wait for Token Expiry
        TimeUnit.MILLISECONDS.sleep(appProperties.getAuth().getTokenExpirationMsec());

        AuthenticationResponse refreshedResponse = refresh(
                authenticationResponse.getAuthToken(),
                authenticationResponse.getRefreshToken())
                .isEqualTo(HttpStatus.OK.value())
                .expectBody(AuthenticationResponse.class)
                .returnResult().getResponseBody();

        getMe(authenticationResponse).isEqualTo(HttpStatus.UNAUTHORIZED.value());

        getMe(refreshedResponse).isEqualTo(HttpStatus.OK.value());

        logout(authenticationRequest, refreshedResponse).isEqualTo(HttpStatus.OK.value());

        getMe(authenticationResponse).isEqualTo(HttpStatus.UNAUTHORIZED.value());

    }

    @Test
    void testSwapping() throws InterruptedException {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                this.signupRequest.getEmail(),
                this.signupRequest.getPassword());

        AuthenticationResponse originalAuth = login(authenticationRequest);

        originalAuth = register(authenticationRequest, originalAuth);

        // Wait for Token Expiry
        TimeUnit.MILLISECONDS.sleep(appProperties.getAuth().getTokenExpirationMsec());


        AuthenticationResponse anotherAuth = login(authenticationRequest);

        refresh(anotherAuth.getAuthToken(),
                originalAuth.getRefreshToken()).isEqualTo(HttpStatus.UNAUTHORIZED.value());

    }

    @Test
    void testExpiredLogout() throws InterruptedException {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                this.signupRequest.getEmail(),
                this.signupRequest.getPassword());

        AuthenticationResponse authenticationResponse =
                login(authenticationRequest);

        authenticationResponse = register(authenticationRequest,
                authenticationResponse);

        getMe(authenticationResponse).isEqualTo(HttpStatus.OK.value());

        // Wait for Token Expiry
        TimeUnit.MILLISECONDS.sleep(appProperties.getAuth().getTokenExpirationMsec());

        logout(authenticationRequest, authenticationResponse).isEqualTo(HttpStatus.OK.value());

    }

    @Test
    void testMultiRegistration() throws InterruptedException {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                this.signupRequest.getEmail(),
                this.signupRequest.getPassword());

        AuthenticationResponse authenticationResponse =
                login(authenticationRequest);

        AuthenticationResponse authenticationResponse1 =
                register(authenticationRequest, authenticationResponse);

        AssertionError error = Assertions.assertThrows(AssertionError.class,
                () -> {
                    register(authenticationRequest, authenticationResponse1);
                });
        Assertions.assertEquals("Status expected:<201 CREATED> but was:<401 " +
                "UNAUTHORIZED>", error.getMessage());

        authenticationResponse = login(authenticationRequest);
        logout(authenticationRequest, authenticationResponse).isEqualTo(HttpStatus.OK.value());

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

    private AuthenticationResponse register(final AuthenticationRequest authenticationRequest,
                                            final AuthenticationResponse authenticationResponse) {

        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setName("Sathish Kumar");
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

    private StatusAssertions refresh(final String authToken,
                                     final String rToken) {
        RefreshToken refreshToken = new RefreshToken(rToken);
        return this.webTestClient
                .post()
                .uri("/api/auth/refresh")
                .body(Mono.just(refreshToken), RefreshToken.class)
                .header("Authorization", "Bearer " + authToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus();
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

    private StatusAssertions getMe(final AuthenticationResponse authenticationResponse) {
        return this.webTestClient
                .get()
                .uri("/api/auth/me")
                .header("Authorization",
                        "Bearer " + authenticationResponse.getAuthToken())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus();
    }

}