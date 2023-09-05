package com.techatpark.workout.service;

import com.techatpark.workout.model.Learner;
import com.techatpark.workout.starter.security.payload.SignupRequest;
import jakarta.validation.ConstraintViolationException;
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

    @Test
    void testEmptyReads() {
        Assertions.assertFalse(learnerService.read(HANDLE).isPresent());
        Assertions.assertFalse(learnerService.readByEmail(EMAIL).isPresent());
    }

    @Test
    void testInvalidSignUp() {
        SignupRequest signupRequest = aSignupRequest();

        signupRequest.setEmail("Invalid Email");

        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            learnerService.signUp(signupRequest,
                    s -> String.valueOf(new StringBuilder(s).reverse()));
        });

    }

    @Test
    void testUpdate() {
        learnerService.signUp(aSignupRequest(),
                s -> String.valueOf(new StringBuilder(s).reverse()));

        Learner learner = getLearnerWithNewPassword(
                learnerService.readByEmail(EMAIL).get());

        learnerService.update(HANDLE, learner);

        Assertions.assertEquals(learner.password(),
                learnerService.readByEmail(EMAIL).get().password());


    }

    @Test
    void testInvalidUpdate() {
        learnerService.signUp(aSignupRequest(),
                s -> String.valueOf(new StringBuilder(s).reverse()));

        Learner learner = getLearnerWithNewPassword(
                learnerService.readByEmail(EMAIL).get());

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            learnerService.update(HANDLE+"INVALID", learner);
        });
    }


    private static Learner getLearnerWithNewPassword(final Learner existingLearner) {
        return new Learner(
                existingLearner.userHandle(),
                existingLearner.email(),
                String.valueOf(System.currentTimeMillis()),
                existingLearner.imageUrl(),
                existingLearner.provider(),
                existingLearner.createdAt(),
                existingLearner.modifiedAt());
    }

    SignupRequest aSignupRequest() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail(EMAIL);
        signupRequest.setImageUrl("/images/" + HANDLE + ".png");
        signupRequest.setPassword("password");
        return signupRequest;
    }
}