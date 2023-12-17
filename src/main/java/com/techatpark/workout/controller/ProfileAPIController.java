package com.techatpark.workout.controller;

import com.gurukulams.core.model.Org;
import com.gurukulams.core.payload.Profile;
import com.gurukulams.core.service.OrgService;
import com.gurukulams.core.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

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
     * declare a org Service.
     */
    private final OrgService orgService;

    /**
     * @param theProfileService a learner service
     * @param theOrgService a Org service
     */
    ProfileAPIController(final ProfileService theProfileService,
                         final OrgService theOrgService) {
        this.profileService = theProfileService;
        this.orgService = theOrgService;
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

    @Operation(summary = "Get the Profile with given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "getting learner successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "learner not found")})
    @GetMapping(value = "/{id}/orgs", produces = "application/json")
    public final ResponseEntity<List<Org>> getOrgs(final Principal principal,
                                   final @PathVariable String id,
                                   @RequestHeader(name = "Accept-Language",
                                       required = false) final Locale locale)
            throws SQLException {
        final List<Org> orgs = orgService.getOrganizationsOf(
                id,
                locale);
        return orgs.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(orgs);
    }

    @Operation(summary = "Get the buddies with given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "getting learner successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "learner not found")})
    @GetMapping(value = "/{id}/buddies", produces = "application/json")
    public final ResponseEntity<List<Profile>> getBuddies(
                                final Principal principal,
                                   final @PathVariable String id)
            throws SQLException {
        final List<Profile> buddies = profileService.getBuddies(
                id);
        return buddies.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(buddies);
    }
    /**
     * Register a Event.
     *
     * @param id
     * @param principal
     * @return event
     */
    @Operation(summary = "Registers the event by given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "event registered successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "event not found")})
    @RequestMapping(method = RequestMethod.HEAD, value = "/{id}")
    public final ResponseEntity<Void> isRegistered(@PathVariable final
                                                   String id,
                                                   final Principal principal)
            throws SQLException {
        return profileService.isRegistered(principal.getName(),
                id) ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * Register a Event.
     *
     * @param id
     * @param principal
     * @return event
     */
    @Operation(summary = "Registers the event by given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "event registered successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "event not found")})
    @PostMapping("/{id}")
    public final ResponseEntity<Void> register(@PathVariable final
                                               String id,
                                               final Principal principal)
            throws SQLException {
        return profileService.register(principal.getName(),
                id) ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}
