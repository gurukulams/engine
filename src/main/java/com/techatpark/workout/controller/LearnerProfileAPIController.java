package com.techatpark.workout.controller;

import com.techatpark.workout.model.LearnerProfile;
import com.techatpark.workout.service.LearnerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.security.Principal;

/**
 * The type Learner profile api controller.
 */
@RestController
@RequestMapping("/api/profile")
@Tag(name = "Learner Profiles",
        description = "Resources to manage Learner Profile")
class LearnerProfileAPIController {

    /**
     * declare a learnerprofile service.
     */
    private final LearnerProfileService learnerProfileService;

    /**
     * Instantiates a new Learner profile api controller.
     *
     * @param aLearnerProfileService the learner profile service
     */
    LearnerProfileAPIController(final LearnerProfileService
                                               aLearnerProfileService) {
        this.learnerProfileService = aLearnerProfileService;
    }

    /**
     * Create response entity.
     *
     * @param principal      the principal
     * @param learnerProfile the learner profile
     * @return the response entity
     */
    @Operation(summary = "creates a new Learner Profile",
            description = "Can be called "
                    + "only by users with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "201",
            description = "learner profile created successfully"),
            @ApiResponse(responseCode = "400",
                    description = "learner profile is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = "application/json", consumes = "application/json")
    public final ResponseEntity<LearnerProfile> create(
                                        final Principal principal,
                                                 final @RequestBody
                                                 LearnerProfile
                                                         learnerProfile
                                                 ) {
        LearnerProfile created = learnerProfileService
                .create(learnerProfile);
        return ResponseEntity.created(
                URI.create("/api/profile" + created.userHandle()))
                .body(created);
    }

    /**
     * Read response entity.
     *
     * @param principal the principal
     * @param id        the id
     * @return the response entity
     */
    @Operation(summary = "Get the LearnerProfile with given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "getting LearnerProfile successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "LearnerProfile not found")})
    @GetMapping(value = "/{id}", produces = "application/json")
    public final ResponseEntity<LearnerProfile> read(final Principal principal,
                                        final @PathVariable String id) {
        return ResponseEntity.of(learnerProfileService.read(
                id));
    }

}
