package com.techatpark.workout.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gurukulams.core.model.Subjects;
import com.techatpark.workout.service.SubjectService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/subjects")
@Tag(name = "Subjects", description = "Resource to manage Subjects")
class SubjectAPIController {
    /**
     * declare a subjects service.
     */
    private final SubjectService subjectsService;

    SubjectAPIController(final SubjectService asubjectsService) {
        this.subjectsService = asubjectsService;
    }


    @Operation(summary = "Creates a new subjects",
            description = "Can be called "
                    + "only by users with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "201",
            description = "subjects created successfully"),
            @ApiResponse(responseCode = "400",
                    description = "subjects is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = "application/json", consumes = "application/json")
    public final ResponseEntity<Subjects> create(final Principal principal,
                                                 @RequestHeader
                                                  (name = "Accept-Language",
                                          required = false) final Locale locale,
                                                 final @RequestBody Subjects subjects) {
        Subjects created = subjectsService.create(principal.getName(), locale,
                subjects);
        return ResponseEntity.created(
                URI.create("/api/subjects" + created.getId()))
                .body(created);
    }

    @Operation(summary = "Get the Subjects with given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "getting subjects successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "subjects not found")})

    @GetMapping("/{id}")
    public final ResponseEntity<Subjects> read(final @PathVariable UUID id,
                                        @RequestHeader
                                                (name = "Accept-Language",
                                        required = false) final Locale locale,
                                        final Principal principal) {
        return ResponseEntity.of(subjectsService.read(principal.getName(),
                locale, id));
    }

    @Operation(summary = "Updates the subjects by given id",
            description = "Can be called only by users "
                    + "with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "subjects updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "subjects is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "subjects not found")})
    @PutMapping(value = "/{id}", produces = "application/json", consumes =
            "application/json")
    public final ResponseEntity<Subjects> update(final @PathVariable UUID id,
                                          final Principal
                                                  principal,
                                          @RequestHeader
                                                  (name = "Accept-Language",
                                      required = false) final Locale locale,
                                          final @RequestBody
                                                  Subjects
                                                  subjects)
            throws JsonProcessingException {
        final Subjects updatedSubject =
                subjectsService.update(id, principal.getName(),
                        locale, subjects);
        return updatedSubject == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(updatedSubject);
    }

    @Operation(summary = "Deletes the subjects by given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "subjects deleted successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "subjects not found")})
    @DeleteMapping("/{id}")
    public final ResponseEntity<Void> delete(final @PathVariable
                                               UUID id,
                                       final Principal principal) {
        return subjectsService.delete(principal.getName(),
                id) ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "lists the subjects",
            description = " Can be invoked by auth users only",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Listing the subjects"),
            @ApiResponse(responseCode = "204",
                    description = "subjects are not available"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @GetMapping(produces = "application/json")
    public final ResponseEntity<List<Subjects>> list(final Principal
                                                      principal,
                                              @RequestHeader
                                                      (name = "Accept-Language",
                                      required = false) final Locale locale) {
        final List<Subjects> subjectsList = subjectsService.list(
                principal.getName(), locale);
        return subjectsList.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(subjectsList);
    }
}


