package com.techatpark.workout.controller;

import com.techatpark.workout.model.Category;
import com.techatpark.workout.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
 * The type Category api controller.
 */
@RestController
@RequestMapping("/api/categories")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Categories",
        description = "Resource to manage Categories")
class CategoryAPIController {

    /**
     * declare a tag service.
     */
    private final CategoryService categoryService;

    CategoryAPIController(final CategoryService anTagService) {
        this.categoryService = anTagService;
    }

    @Operation(summary = "Creates a new tag",
            description = "Can be called "
                    + "only by users with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "201",
            description = "tag created successfully"),
            @ApiResponse(responseCode = "400",
                    description = "tag is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = "application/json", consumes = "application/json")
    public ResponseEntity<Category> create(final Principal principal,
                                       @RequestHeader(name = "Accept-Language",
                                  required = false) final Locale locale,
                                       final @RequestBody
                                               Category tag) {
        Category createdTag =
                categoryService.create(principal.getName(), locale, tag);
        return ResponseEntity.created(URI.create("/api/syllabus"
                        + createdTag.id()))
                .body(createdTag);

    }

    @Operation(summary = "Get the Category with given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "getting tag successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "tag not found")})
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Category> read(final @PathVariable String id,
                                     @RequestHeader(name = "Accept-Language",
                            required = false) final Locale locale,
                                     final Principal principal) {
        return ResponseEntity.of(
                categoryService.read(principal.getName(), id, locale));
    }

    @Operation(summary = "Updates the tag by given id",
            description = "Can be called only by users "
                    + "with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "tag updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "tag is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "tag not found")})
    @PutMapping(value = "/{id}", produces = "application/json", consumes =
            "application/json")
    public ResponseEntity<Category> update(final @PathVariable
                                              String id,
                                           final Principal
                                              principal,
                                   @RequestHeader(name = "Accept-Language",
                              required = false) final Locale locale,
                                           final @RequestBody
                                                   Category
                                              tag) {
        final Category updatedTag =
                categoryService.update(id, principal.getName(), locale, tag);
        return ResponseEntity.ok(updatedTag);
    }

    @Operation(summary = "Deletes the tag by given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "tag deleted successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "tag not found")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(final @PathVariable
                                               String id,
                                       final Principal
                                               principal) {
        return categoryService.delete(principal.getName(), id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "lists the tag",
            description = " Can be invoked by auth users only",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Listing the tag"),
            @ApiResponse(responseCode = "204",
                    description = "syllabus are not available"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<Category>> list(final Principal
                                                  principal,
                                   @RequestHeader(name = "Accept-Language",
                              required = false) final Locale locale) {
        final List<Category> tagList = categoryService.list(
                principal.getName(), locale);
        return tagList.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(tagList);
    }


}
