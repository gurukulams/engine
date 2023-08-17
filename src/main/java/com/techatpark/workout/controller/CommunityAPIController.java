package com.techatpark.workout.controller;

import com.techatpark.workout.model.Community;
import com.techatpark.workout.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Locale;

/**
 * The type community api controller.
 */
@RestController
@RequestMapping("/api/communities")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Community",
        description = "Resource to manage Communities")
@SecurityScheme(
    name = "bearerAuth",
    scheme = "bearer",
    bearerFormat = "JWT",
    type = SecuritySchemeType.HTTP,
    in = SecuritySchemeIn.HEADER
)
class CommunityAPIController {

    /**
     * declare a Community service.
     */
    private final CommunityService communityService;

    CommunityAPIController(final CommunityService paramCommunityService) {
        this.communityService = paramCommunityService;
    }

    @Operation(summary = "Creates a new Community",
            description = "Can be called "
                    + "only by users with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "201",
            description = "Community created successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Community is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = "application/json", consumes = "application/json")
    public ResponseEntity<Community> create(final Principal principal,
                                       @RequestHeader(name = "Accept-Language",
                                  required = false) final Locale locale,
                                       final @RequestBody
                                       Community community) {
        Community createdCommunity =
                communityService.create(principal.getName(), locale, community);
        return ResponseEntity.created(URI.create("/api/communities/"
                        + createdCommunity.id()))
                .body(createdCommunity);

    }

    @Operation(summary = "Get the community with given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "getting Community successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "Community not found")})
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Community> read(final @PathVariable String id,
                                     @RequestHeader(name = "Accept-Language",
                            required = false) final Locale locale,
                                     final Principal principal) {
        return ResponseEntity.of(
                communityService.read(principal.getName(), id, locale));
    }

    @Operation(summary = "Updates the Community by given id",
            description = "Can be called only by users "
                    + "with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Community updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Community is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "Community not found")})
    @PutMapping(value = "/{id}", produces = "application/json", consumes =
            "application/json")
    public ResponseEntity<Community> update(final @PathVariable
                                              String id,
                                           final Principal
                                              principal,
                                   @RequestHeader(name = "Accept-Language",
                              required = false) final Locale locale,
                                           final @RequestBody
                                           Community
                                              community) {
        final Community updatedCommunity =
            communityService.update(id, principal.getName(), locale, community);
        return ResponseEntity.ok(updatedCommunity);
    }

    @Operation(summary = "Deletes the Community by given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Community deleted successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "Community not found")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(final @PathVariable
                                               String id,
                                       final Principal
                                               principal) {
        return communityService.delete(principal.getName(), id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "lists the Communities",
            description = "Can be invoked by auth users only",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Listing the communities successfully"),
            @ApiResponse(responseCode = "204",
                    description = "Communities are not available"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<Community>> list(final Principal
                                                  principal,
                                   @RequestHeader(name = "Accept-Language",
                              required = false) final Locale locale) {
        final List<Community> communities = communityService.list(
                principal.getName(), locale);
        return communities.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(communities);
    }


}
