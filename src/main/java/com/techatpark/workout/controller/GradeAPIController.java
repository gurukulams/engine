package com.techatpark.workout.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techatpark.workout.model.Grade;
import com.techatpark.workout.service.GradeService;
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
@RequestMapping("/api/grades")
@Tag(name = "Grades", description = "Resource to manage Grade")
class GradeAPIController {

    /**
     * declare a grade service.
     */
    private final GradeService gradeService;


    GradeAPIController(final GradeService agradeService) {
        this.gradeService = agradeService;
    }

    @Operation(summary = "Creates a new grade",
            description = "Can be called "
                    + "only by users with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "201",
            description = "grade created successfully"),
            @ApiResponse(responseCode = "400",
                    description = "grade is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = "application/json",
            consumes = "application/json")
    public final ResponseEntity<Grade> create(final Principal principal,
                                        @RequestHeader
                                                (name = "Accept-Language",
                                    required = false) final Locale locale,
                                        final @RequestBody Grade grade) {
        Grade created = gradeService.create(principal.getName(), locale,
                grade);
        return ResponseEntity.created(URI.create("/api/grade"
                        + created.id()))
                .body(created);
    }

    @Operation(summary = "Get the Grade with given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "getting grade successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "grade not found")})

    @GetMapping("/{id}")
    public final ResponseEntity<Grade> read(@PathVariable final UUID id,
                                      @RequestHeader
                                              (name = "Accept-Language",
                                      required = false) final Locale locale,
                                      final Principal principal) {
        return ResponseEntity.of(gradeService
                .read(principal.getName(), locale, id));
    }

    @Operation(summary = "Updates the grade by given id",
            description = "Can be called only by users "
                    + "with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "grade updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "grade is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "grade not found")})
    @PutMapping(value = "/{id}", produces = "application/json", consumes =
            "application/json")
    public final ResponseEntity<Grade> update(@PathVariable final UUID id,
                                        final Principal
                                                principal,
                                        @RequestHeader
                                                (name = "Accept-Language",
                                    required = false) final Locale locale,
                                        final @RequestBody
                                                Grade
                                                grade)
            throws JsonProcessingException {
        final Grade updatedGrade =
                gradeService.update(id, principal.getName(), locale,
                        grade);
        return updatedGrade == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(updatedGrade);
    }

    @Operation(summary = "Deletes the grade by given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "grade deleted successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "grade not found")})
    @DeleteMapping("/{id}")
    public final ResponseEntity<Void> delete(@PathVariable final
                                       UUID id,
                                       final Principal principal) {
        return gradeService.delete(principal.getName(),
                id) ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "lists the grade",
            description = " Can be invoked by auth users only",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Listing the grade"),
            @ApiResponse(responseCode = "204",
                    description = "grade are not available"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @GetMapping(produces = "application/json")
    public final ResponseEntity<List<Grade>> list(final Principal
                                                    principal,
                                            @RequestHeader
                                                    (name = "Accept-Language",
                                        required = false) final Locale locale) {
        final List<Grade> gradeList = gradeService.list(principal
                .getName(), locale);
        return gradeList.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(gradeList);
    }


}


