package com.techatpark.workout.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techatpark.workout.model.Learner;
import com.techatpark.workout.service.LearnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.security.Principal;

/**
 * The type Learner api controller.
 */
@RestController
@RequestMapping("/api/learner")
@Tag(name = "Learners", description = "Resources to manage Learner")
class LearnerAPIController {

    /**
     * declare a learner service.
     */
    private final LearnerService learnerService;

    /**
     * @param alearnerService a learner service
     */
    LearnerAPIController(final LearnerService alearnerService) {
        this.learnerService = alearnerService;
    }


    @Operation(summary = "Get the Learner with given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "getting learner successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "learner not found")})
    @GetMapping(value = "/{id}", produces = "application/json")
    public final ResponseEntity<Learner> read(final Principal principal,
                                        final @PathVariable String id) {
        return ResponseEntity.of(learnerService.read(
                id));
    }

    @Operation(summary = "Updates the learner by given id",
            description = "Can be called only by users "
                    + "with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "learner updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "learner is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "learner not found")})
    @PutMapping(value = "/{id}", produces = "application/json", consumes =
            "application/json")
    public final ResponseEntity<Learner> update(final @PathVariable String id,
                                          final Principal principal,
                                          final @RequestBody Learner learner)
            throws JsonProcessingException {
        final Learner updatedLearner = learnerService.update(id,
                learner);
        return ResponseEntity.ok(updatedLearner);
    }


}
