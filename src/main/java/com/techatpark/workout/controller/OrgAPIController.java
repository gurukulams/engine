package com.techatpark.workout.controller;

import com.gurukulams.core.model.Org;
import com.gurukulams.core.service.OrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.security.Principal;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/orgs")
@Tag(name = "Orgs",
        description = "Resource to manage Org")
class OrgAPIController {

    /**
     * declare a org service.
     */
    private final OrgService orgService;

    OrgAPIController(final OrgService aOrgService) {
        this.orgService = aOrgService;
    }

    /**
     * Create response entity.
     *
     * @param principal the principal
     * @param org     the org name
     * @param locale    the locale
     * @return the response entity
     */
    @Operation(summary = "Creates a new org",
            description = "Can be called "
                    + "only by users with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "201",
            description = "org created successfully"),
            @ApiResponse(responseCode = "400",
                    description = "org is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = "application/json",
            consumes = "application/json")
    public final ResponseEntity<Org> create(final Principal principal,
                                    @RequestHeader(name = "Accept-Language",
                                        required = false) final Locale locale,
                                            @RequestBody final Org org)
            throws SQLException {
        Org created = orgService.create(principal.getName(), locale, org);
        return ResponseEntity.created(URI.create("/api/org"
                        + created.getUserHandle()))
                .body(created);
    }


    /**
     * Read a org.
     *
     * @param id
     * @param principal
     * @param locale    the locale
     * @return a org
     */
    @Operation(summary = "Get the Org with given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "getting org successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "syllabus not found")})

    @GetMapping("/{id}")
    public final ResponseEntity<Org> read(@PathVariable final String id,
                      @RequestHeader(name = "Accept-Language",
                              required = false) final Locale locale,
                      final Principal principal)
            throws SQLException {
        return ResponseEntity.of(orgService.read(principal.getName(),
                id, locale));
    }

    /**
     * Update a Org.
     *
     * @param id
     * @param principal
     * @param locale
     * @param org
     * @return a org
     */
    @Operation(summary = "Updates the org by given id",
            description = "Can be called only by users "
                    + "with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "org updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "org is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "syllabus not found")})
    @PutMapping(value = "/{id}", produces = "application/json", consumes =
            "application/json")
    public final ResponseEntity<Org> update(@PathVariable final String id,
                    final Principal
                            principal,
                    @RequestHeader(name = "Accept-Language",
                            required = false) final Locale locale,
                    @RequestBody final Org
                            org) throws SQLException {
        final Org updatedOrg =
                orgService.update(id, principal.getName(),
                        locale, org);
        return updatedOrg == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(updatedOrg);
    }

    /**
     * Delete a Org.
     *
     * @param id
     * @param principal
     * @return org
     */
    @Operation(summary = "Deletes the org by given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "org deleted successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "org not found")})
    @DeleteMapping("/{id}")
    public final ResponseEntity<Void> delete(@PathVariable final
                                             String id,
                                             final Principal principal)
            throws SQLException {
        return orgService.delete(principal.getName(),
                id) ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * List the Org.
     *
     * @param principal
     * @param locale
     * @return list of org
     */
    @Operation(summary = "lists the org of an user",
            description = " Can be invoked by auth users only",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Listing the org"),
            @ApiResponse(responseCode = "204",
                    description = "org are not available"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @GetMapping(produces = "application/json")
    public final ResponseEntity<List<Org>> list(final Principal
                                  principal,
                          @RequestHeader(name = "Accept-Language",
                                  required = false) final Locale locale)
            throws SQLException {
        final List<Org> orgs = orgService.list(
                principal.getName(), locale);
        return orgs.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(orgs);
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
        return orgService.isRegistered(principal.getName(),
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
        return orgService.register(principal.getName(),
                id) ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}
