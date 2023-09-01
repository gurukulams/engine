package com.techatpark.workout.service;

import com.techatpark.workout.starter.security.payload.SignupRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LearnerServiceTest {
    private static final String HANDLE = "tom";
    private static final String EMAIL = HANDLE + "@gmail.com";
    @Autowired
    private LearnerService learnerService;

    @BeforeEach
    void before() {
        cleanup();
    }

    @AfterEach
    void after() {
        cleanup();
    }

    private void cleanup() {
        learnerService.delete();
    }
    @Test
    void testSignUp() {
        learnerService.signUp(aSignupRequest(),
                s -> String.valueOf(new StringBuilder(s).reverse()));

        Assertions.assertTrue(learnerService.read(HANDLE).isPresent());
        Assertions.assertTrue(learnerService.readByEmail(EMAIL).isPresent());
    }

    SignupRequest aSignupRequest() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail(EMAIL);
        signupRequest.setImageUrl("/images/" + HANDLE + ".png");
        signupRequest.setPassword("password");
        return signupRequest;
    }
}