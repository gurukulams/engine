package com.techatpark.workout.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gurukulams.core.model.Annotations;
import com.techatpark.workout.service.AnnotationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
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
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/annotations/{onType}")
@Tag(name = "Annotations", description = "Resource to manage Annotations")
class AnnotationAPIController {

    /**
     * declare a bookservice.
     */
    private final AnnotationService annotationService;

    /**
     * Builds AnnotationAPIController.
     *
     * @param anAnnotationService
     */
    AnnotationAPIController(final AnnotationService anAnnotationService) {
        this.annotationService = anAnnotationService;
    }

    /**
     * Create response entity.
     *
     * @param principal  the principal
     * @param onType     the book name
     * @param onInstance
     * @param annotation the annotation
     * @param locale
     * @return the response entity
     */
    @Operation(summary = "Creates a new annotation",
            description = "Can be called "
                    + "only by users with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "201",
            description = "annotation created successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "400",
                    description = "annotation is invalid")})
    @PostMapping("/{*onInstance}")
    public final ResponseEntity<Optional<Annotations>> create(
            final Principal principal,
            final @NotBlank @PathVariable String onType,
            final @NotBlank @PathVariable String onInstance,
            @RequestHeader(
                    name = "Accept-Language",
                    required = false) final Locale locale,
            final @RequestBody Annotations annotation)
            throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                annotationService.create(onType,
                        onInstance,
                        annotation,
                        locale,
                        principal.getName()
                ));
    }

    /**
     * Create response entity.
     *
     * @param principal  the principal
     * @param onType     the book name
     * @param onInstance the annotation
     * @param locale
     * @return the response entity
     */
    @Operation(summary = "Creates a new annotation",
            description =
                    "Can be called only by users "
                            + "with 'auth management'"
                            + " rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "annotation found successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "annotation not found")})
    @GetMapping("/{*onInstance}")
    public final ResponseEntity<List<Annotations>> list(
            final Principal principal,
            final @PathVariable String onType,
            final @NotBlank @PathVariable String onInstance,
            @RequestHeader(
                    name = "Accept-Language",
                    required = false) final Locale locale) {
        return ResponseEntity.status(HttpStatus.OK).body(
                annotationService.list(principal.getName(), locale,
                        onType,
                        onInstance));
    }

    /**
     * Update response entity.
     *
     * @param id         the id
     * @param annotation the annotation
     * @param onType
     * @param locale
     * @return the response entity
     */
    @Operation(summary = "Updates the annotation by given id",
            description = "Can be called only by users with "
                    + "'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "note updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "note is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "note not found")})
    @PutMapping("/{id}")
    public final ResponseEntity<Optional<Annotations>> update(
            final @PathVariable String onType,
            @RequestHeader(
                    name = "Accept-Language",
                    required = false) final Locale locale,
            final @PathVariable UUID id,
            final @RequestBody Annotations annotation)
            throws JsonProcessingException {
        final Optional<Annotations> updatednote = annotationService.update(
                id, locale, annotation);
        return updatednote == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(updatednote);
    }

    /**
     * Delete annotation by id response entity.
     *
     * @param id     the id
     * @param onType
     * @param locale
     * @return the response entity
     */
    @Operation(summary = "Deletes the annotation by given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "note deleted successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "note not found")})
    @DeleteMapping("/{id}")
    public final ResponseEntity<Void> delete(
            final @PathVariable String onType,
            @RequestHeader(
                    name = "Accept-Language",
                    required = false) final Locale locale,
            final @PathVariable UUID id) {
        return annotationService.delete(id, locale)
                ?
                ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}
