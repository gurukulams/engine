package com.techatpark.workout.controller;

import com.gurukulams.event.model.Event;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


import java.security.Principal;
import java.sql.SQLException;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments",
        description = "Resource to manage Payments")
public class PaymentAPIController {

    /**
     * Create response entity.
     *
     * @param principal the principal
     * @param event the event
     * @param request the request
     * @return the response entity
     */
    @Operation(summary = "Creates a new event",
            description = "Can be called "
                    + "only by users with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "201",
            description = "event created successfully"),
            @ApiResponse(responseCode = "400",
                    description = "event is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/**", produces = "application/json",
            consumes = "application/json")
    public final ResponseEntity<Void> create(final Principal principal,
                                              @RequestBody final Event event,
                                              final HttpServletRequest request)
            throws SQLException {
        return ResponseEntity.ok().build();
    }
}
