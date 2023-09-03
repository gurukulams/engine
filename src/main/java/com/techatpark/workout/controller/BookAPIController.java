package com.techatpark.workout.controller;

import com.techatpark.workout.model.Book;
import com.techatpark.workout.service.BookService;
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

/**
 * The type Book api controller.
 */
@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Resource to manage Books")
class BookAPIController {
    /**
     * declare a bookservice.
     */
    private final BookService bookService;


    /**
     * Instantiates a new Book api controller.
     *
     * @param abookService   the book service
     */
    BookAPIController(final BookService abookService) {
        this.bookService = abookService;
    }

    /**
     * Create response entity.
     *
     * @param principal the principal
     * @param book      the book name
     * @param locale    the locale
     * @return the response entity
     */
    @Operation(summary = "Creates a new book",
            description = "Can be called "
                    + "only by users with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "201",
            description = "book created successfully"),
            @ApiResponse(responseCode = "400",
                    description = "book is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = "application/json", consumes = "application/json")
    public final ResponseEntity<Book> create(final Principal principal,
                                       @RequestHeader(name = "Accept-Language",
                                       required = false) final Locale locale,
                                       @RequestBody final Book book) {
        Book created = bookService.create(principal.getName(), locale, book);
        return ResponseEntity.created(URI.create("/api/book" + created.id()))
                .body(created);
    }

    /**
     * Read a book.
     *
     * @param id
     * @param principal
     * @param locale    the locale
     * @return a book
     */
    @Operation(summary = "Get the Book with given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "getting book successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "syllabus not found")})

    @GetMapping("/{id}")
    public final ResponseEntity<Book> read(@PathVariable final UUID id,
                                     @RequestHeader(name = "Accept-Language",
                                     required = false) final Locale locale,
                                     final Principal principal) {
        return ResponseEntity.of(bookService.read(principal.getName(),
                locale, id));
    }

    /**
     * Update a Book.
     *
     * @param id
     * @param principal
     * @param locale
     * @param book
     * @return a book
     */
    @Operation(summary = "Updates the book by given id",
            description = "Can be called only by users "
                    + "with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "book updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "book is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "syllabus not found")})
    @PutMapping(value = "/{id}", produces = "application/json", consumes =
            "application/json")
    public final ResponseEntity<Book> update(@PathVariable final UUID id,
                                       final Principal
                                               principal,
                                       @RequestHeader(name = "Accept-Language",
                                       required = false) final Locale locale,
                                       @RequestBody final Book
                                               book) {
        final Book updatedBook =
                bookService.update(id, principal.getName(), locale, book);
        return updatedBook == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(updatedBook);
    }

    /**
     * Delete a Book.
     *
     * @param id
     * @param principal
     * @return book
     */
    @Operation(summary = "Deletes the book by given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "book deleted successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "book not found")})
    @DeleteMapping("/{id}")
    public final ResponseEntity<Void> delete(@PathVariable final
                                       UUID id,
                                       final Principal principal) {
        return bookService.delete(principal.getName(),
                id) ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * List the Books.
     *
     * @param principal
     * @param locale
     * @return list of book
     */
    @Operation(summary = "lists the book",
            description = " Can be invoked by auth users only",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Listing the book"),
            @ApiResponse(responseCode = "204",
                    description = "book are not available"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @GetMapping(produces = "application/json")
    public final ResponseEntity<List<Book>> list(final Principal
                                                   principal,
                                           @RequestHeader(
                                                   name = "Accept-Language",
                                   required = false) final Locale locale) {
        final List<Book> bookList = bookService.list(
                principal.getName(), locale);
        return bookList.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(bookList);
    }





}
