package com.gurukulams.controller;

import com.gurukulams.notebook.model.Annotation;
import com.gurukulams.notebook.service.AnnotationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/api/annotations/{onType}")
@Tag(name = "Annotation", description = "Resource to manage Annotation")
class AnnotationAPIController {
    /**
     * Logger.
     */
    private final Logger logger =
            LoggerFactory.getLogger(AnnotationAPIController.class);
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
    public final ResponseEntity<Annotation> create(
            final Principal principal,
            final @NotBlank @PathVariable String onType,
            final @NotBlank @PathVariable String onInstance,
            @RequestHeader(
                    name = "Accept-Language",
                    required = false) final Locale locale,
            final @RequestBody Annotation annotation)
            throws SQLException, IOException {
        logger.info("Create a new Annotation for type {} at {}",
                onType, onInstance);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                annotationService.create(principal.getName(),
                        onType,
                        onInstance,
                        annotation,
                        locale
                ));
    }

    /**
     * Create response entity.
     *
     * @param principal  the principal
     * @param onType     the book name
     * @param onInstance the annotation
     * @param buddy
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
    public final ResponseEntity<List<Annotation>> list(
            final Principal principal,
            final @PathVariable String onType,
            final @NotBlank @PathVariable String onInstance,
            @RequestHeader(
                    name = "X-Buddy-For",
                    required = false)
            final String buddy,
            @RequestHeader(
                    name = "Accept-Language",
                    required = false) final Locale locale)
            throws SQLException, IOException {
        String notesOfUser = buddy == null
                ? principal.getName() : buddy;
        logger.info("Listing Annotations for type {} at {} for the user {}",
                onType, onInstance, notesOfUser);
        return ResponseEntity.status(HttpStatus.OK).body(
                annotationService.list(notesOfUser, locale,
                        onType,
                        onInstance));
    }

    /**
     * Update response entity.
     * @param principal
     * @param id         the id
     * @param annotation the annotation
     * @param onType
     * @param onInstance
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
    @PutMapping("/{id}/{*onInstance}")
    public final ResponseEntity<Optional<Annotation>> update(
            final Principal principal,
            final @PathVariable String onType,
            final @NotBlank @PathVariable String onInstance,
            @RequestHeader(
                    name = "Accept-Language",
                    required = false) final Locale locale,
            final @PathVariable String id,
            final @RequestBody Annotation annotation)
            throws SQLException, IOException {
        final Optional<Annotation> updatednote = annotationService.update(
                principal.getName(),
                "#" + id, onType, onInstance, locale, annotation);
        return updatednote.isEmpty() ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(updatednote);
    }

    /**
     * Delete annotation by id response entity.
     * @param principal
     * @param id     the id
     * @param onType
     * @param onInstance
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
    @DeleteMapping("/{id}/{*onInstance}")
    public final ResponseEntity<Void> delete(
            final Principal principal,
            final @PathVariable String onType,
            final @NotBlank @PathVariable String onInstance,
            @RequestHeader(
                    name = "Accept-Language",
                    required = false) final Locale locale,
            final @PathVariable String id)
            throws SQLException, IOException {
        return annotationService.delete(
                principal.getName(), "#" + id,
                onType, onInstance, locale)
                ?
                ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}
