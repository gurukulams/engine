package com.techatpark.workout.controller;

import com.gurukulams.core.model.Org;
import com.gurukulams.core.service.OrgService;
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
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/**
 * The type org api controller.
 */
@RestController
@RequestMapping("/api/orgs")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Org",
        description = "Resource to manage Orgs")
@SecurityScheme(
    name = "bearerAuth",
    scheme = "bearer",
    bearerFormat = "JWT",
    type = SecuritySchemeType.HTTP,
    in = SecuritySchemeIn.HEADER
)
class OrgAPIController {

    /**
     * declare a Org service.
     */
    private final OrgService orgService;

    OrgAPIController(final OrgService paramOrgService) {
        this.orgService = paramOrgService;
    }

    // Hide by defalut -> Security
    // All public facing methods should be annotated with @operation
    // All the instance variables in controller should be final
    // All the public methods should be final
    // Controller should not be a public class
    @Operation(summary = "Creates a new Org",
            description = "Can be called "
                    + "only by users with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "201",
            description = "Org created successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Org is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = "application/json", consumes = "application/json")
    public final ResponseEntity<Org> create(final Principal principal,
                                       @RequestHeader(name = "Accept-Language",
                                  required = false) final Locale locale,
                                       final @RequestBody
                                       Org org) throws SQLException {
        Org createdOrg =
                orgService.create(principal.getName(), org);
        return ResponseEntity.created(URI.create("/api/orgs/"
                        + createdOrg.getId()))
                .body(createdOrg);

    }

    @Operation(summary = "Get the org with given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "getting Org successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "Org not found")})
    @GetMapping(value = "/{id}", produces = "application/json")
    public final ResponseEntity<Org> read(final @PathVariable String id,
                              @RequestHeader(name = "Accept-Language",
                            required = false) final Locale locale,
                             final Principal principal) throws SQLException {
        return ResponseEntity.of(
                orgService.read(principal.getName(), id));
    }

    @Operation(summary = "Updates the Org by given id",
            description = "Can be called only by users "
                    + "with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Org updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Org is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "Org not found")})
    @PutMapping(value = "/{id}", produces = "application/json", consumes =
            "application/json")
    public final ResponseEntity<Org> update(final @PathVariable
                                              String id,
                                           final Principal
                                              principal,
                                   @RequestHeader(name = "Accept-Language",
                              required = false) final Locale locale,
                                           final @RequestBody
                                           Org
                                              org) throws SQLException {
        final Org updatedOrg =
            orgService.update(id, principal.getName(), org);
        return ResponseEntity.ok(updatedOrg);
    }

    @Operation(summary = "Deletes the Org by given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Org deleted successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "Org not found")})
    @DeleteMapping("/{id}")
    public final ResponseEntity<Void> delete(final @PathVariable
                                               String id,
                                       final Principal
                                               principal) throws SQLException {
        return orgService.delete(principal.getName(), id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "lists the Orgs",
            description = "Can be invoked by auth users only",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Listing the orgs successfully"),
            @ApiResponse(responseCode = "204",
                    description = "Orgs are not available"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @GetMapping(produces = "application/json")
    public final ResponseEntity<List<Org>> list(final Principal
                                                  principal,
                    @RequestHeader(name = "Accept-Language",
                  required = false) final Locale locale) throws SQLException {
        final List<Org> orgs = orgService.list(
                principal.getName());
        return orgs.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(orgs);
    }


}
