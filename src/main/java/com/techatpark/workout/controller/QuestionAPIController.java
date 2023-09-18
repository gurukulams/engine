package com.techatpark.workout.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techatpark.workout.model.Question;
import com.techatpark.workout.model.QuestionType;
import com.techatpark.workout.service.AnswerService;
import com.techatpark.workout.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
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

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * The type Question api controller.
 */
@RestController
@RequestMapping("/api/questions")
@Tag(name = "Questions", description = "Resource to manage Questions")
class QuestionAPIController {
    /**
     * declare a QuestionService.
     */
    private final QuestionService questionService;

    /**
     * answerService.
     */
    private final AnswerService answerService;

    /**
     * Instantiates a new Book api controller.
     *
     * @param aQuestionService   the book service
     * @param aAnswerService a Answer Service
     */
    QuestionAPIController(final QuestionService aQuestionService,
                          final AnswerService aAnswerService) {
        this.questionService = aQuestionService;
        this.answerService = aAnswerService;
    }
    /**
     * Create response entity.
     *
     * @param questionType the question type
     * @param question     the question
     * @param request      the request
     * @param locale        the locale
     * @param principal     the principal
     * @return the response entity
     */
    @Operation(summary = "Creates a new question",
            description = "Can be called only by users with"
                    + " 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "201",
            description = "question created successfully"),
            @ApiResponse(responseCode = "400",
                    description = "question is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{questionType}/**")
    public final ResponseEntity<Optional<Question>> create(
                                                     final @PathVariable
                                                     QuestionType
                                                             questionType,
                                                     final
                                                     @RequestBody
                                                             Question
                                                             question,
                                                     @RequestHeader(
                                                     name = "Accept-Language",
                                         required = false) final Locale locale,
                                                     final Principal principal,
                                             final HttpServletRequest request)
            throws ServletException, IOException {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                questionService.create(
                        getCategories(request.getRequestURI(), questionType),
                        null,
                        questionType, locale,
                        principal.getName(), question));
    }

    /**
     * Update response entity.
     *
     * @param questionType the question type
     * @param questionId   the questionId
     * @param question     the question
     * @param request      the request
     * @param locale    the locale
     * @return the response entity
     */
    @Operation(summary = "Updates the question by given questionId",
            description = "Can be called only by users "
                    + "with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "question updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "question is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "question not found")})
    @PutMapping("/{questionType}/{questionId}/**")
    public final ResponseEntity<Optional<Question>> update(
                                                     final @PathVariable
                                                             UUID questionId,
                                                     @RequestHeader(
                                                     name = "Accept-Language",
                                         required = false) final Locale locale,
                                                     final @PathVariable
                                                             QuestionType
                                                             questionType,
                                                     final
                                                     @RequestBody
                                                             Question
                                                             question,
                                                     final
                                                     HttpServletRequest request)
            throws JsonProcessingException {

        final Optional<Question> updatedQuestion =
                questionService.update(
                        questionType, questionId, locale,
                        question);
        return updatedQuestion == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(updatedQuestion);
    }


    /**
     * Delete a question from the given question bank.
     *
     * @param id           the id
     * @param questionType the question type
     * @return the response entity
     */
    @Operation(summary = "Deletes the question by given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "question deleted successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "question not found")})
    @DeleteMapping("/{questionType}/{id}/**")
    public final ResponseEntity<Void> deleteAQuestionById(
                                                    final @PathVariable UUID id,
                                                    final @PathVariable
                                                            QuestionType
                                                            questionType) {
        questionService.deleteAQuestion(id, QuestionType.CHOOSE_THE_BEST);
        return null;
    }


    /**
     * Find all questions response entity.
     *
     * @param principal the principal
     * @param request   the request
     * @param locale    the locale
     * @return the response entity
     */
    @Operation(summary = "lists all the questions for given book and give "
            + "chapter",
            description = " Can be invoked by auth users only",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Listing all the questions"),
            @ApiResponse(responseCode = "204",
                    description = "questions are not available"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @GetMapping("/**")
    public final ResponseEntity<List<Question>>
    findAllQuestionsByChap(final Principal
                                   principal,
                           @RequestHeader(
                                   name = "Accept-Language",
                                   required = false) final Locale locale,
                           final HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(questionService.list(principal.getName(),
                        locale, getCategories(request.getRequestURI())));
    }

    /**
     * Answer response entity.
     *
     * @param questionId the question id
     * @param answer     the answer
     * @return the response entity
     */
    @Operation(summary = "Answer a question",
            description = "Can be called only by"
                    + " users with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "202",
            description = "Answered a question successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "406",
                    description = "Answer is invalid")})
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/{questionId}/answer")
    public final ResponseEntity<Void> answer(final @PathVariable
                                               UUID questionId,
                                       final @RequestBody
                                               String answer) {
        return answerService.answer(questionId, answer)
                ? ResponseEntity.status(
                HttpStatus.ACCEPTED).build()
                : ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    private List<String> getCategories(final String requestURI,
                                       final QuestionType questionType) {
        return List.of(
                requestURI.split("/"
                        + questionType.toString() + "/")[1]
                .split("/"));
    }

    private List<String> getCategories(final String requestURI) {
        return List.of(requestURI
                .replaceFirst("/api/questions/", "")
                .split("/"));
    }

}
