package com.techatpark.workout.controller;

import com.gurukulams.core.payload.Profile;
import com.gurukulams.core.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.sql.SQLException;

/**
 * The type Profile api controller.
 */
@RestController
@RequestMapping("/api/profiles")
@Tag(name = "Profiles", description = "Resources to manage Profiles")
class ProfileAPIController {

    /**
     * declare a learner service.
     */
    private final ProfileService profileService;

    /**
     * @param profileService1 a learner service
     */
    ProfileAPIController(final ProfileService profileService1) {
        this.profileService = profileService1;
    }


    @Operation(summary = "Get the Profile with given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "getting learner successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "learner not found")})
    @GetMapping(value = "/{id}", produces = "application/json")
    public final ResponseEntity<Profile> read(final Principal principal,
                                        final @PathVariable String id)
            throws SQLException {
        return ResponseEntity.of(profileService.read(
                id));
    }

}
