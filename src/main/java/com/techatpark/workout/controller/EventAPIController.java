package com.techatpark.workout.controller;

import com.gurukulams.event.model.Event;
import com.gurukulams.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Principal;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events",
        description = "Resource to manage Event")
class EventAPIController {

    /**
     * declare a event service.
     */
    private final EventService eventService;

    EventAPIController(final EventService aEventService) {
        this.eventService = aEventService;
    }

    /**
     * Create response entity.
     *
     * @param principal the principal
     * @param event     the event name
     * @param locale    the locale
     * @param request the request
     * @return the response entity
     */
    @Operation(summary = "Creates a new event",
            description = "Can be called "
                    + "only by users with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "201",
            description = "event created successfully"),
            @ApiResponse(responseCode = "400",
                    description = "event is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/**", produces = "application/json",
            consumes = "application/json")
    public final ResponseEntity<Event> create(final Principal principal,
                                      @RequestHeader(name = "Accept-Language",
                                        required = false) final Locale locale,
                                              @RequestBody final Event event,
                                              final HttpServletRequest request)
            throws SQLException {
        Event created = eventService.create(
                getCategories(request.getRequestURI()),
                null,
                principal.getName(),
                locale,
                event);
        return ResponseEntity.created(URI.create("/api/event"
                        + created.getId()))
                .body(created);
    }


    /**
     * Read a event.
     *
     * @param id
     * @param principal
     * @param locale    the locale
     * @return a event
     */
    @Operation(summary = "Get the Event with given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "getting event successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "syllabus not found")})

    @GetMapping("/{id}")
    public final ResponseEntity<Event> read(@PathVariable final UUID id,
                      @RequestHeader(name = "Accept-Language",
                              required = false) final Locale locale,
                      final Principal principal)
            throws SQLException {
        return ResponseEntity.of(eventService.read(principal.getName(),
                id, locale));
    }

    /**
     * Update a Event.
     *
     * @param id
     * @param principal
     * @param locale
     * @param event
     * @return a event
     */
    @Operation(summary = "Updates the event by given id",
            description = "Can be called only by users "
                    + "with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "event updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "event is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "syllabus not found")})
    @PutMapping(value = "/{id}", produces = "application/json", consumes =
            "application/json")
    public final ResponseEntity<Event> update(@PathVariable final UUID id,
                    final Principal
                            principal,
                    @RequestHeader(name = "Accept-Language",
                            required = false) final Locale locale,
                    @RequestBody final Event
                            event) throws SQLException {
        final Event updatedEvent =
                eventService.update(id, principal.getName(),
                        locale, event);
        return updatedEvent == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(updatedEvent);
    }

    /**
     * Delete a Event.
     *
     * @param id
     * @param principal
     * @return event
     */
    @Operation(summary = "Deletes the event by given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "event deleted successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "event not found")})
    @DeleteMapping("/{id}")
    public final ResponseEntity<Void> delete(@PathVariable final
                                             UUID id,
                                             final Principal principal)
            throws SQLException {
        return eventService.delete(principal.getName(),
                id) ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * List the Event.
     *
     * @param principal
     * @param locale
     * @return list of event
     */
    @Operation(summary = "lists the event of an user",
            description = " Can be invoked by auth users only",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Listing the event"),
            @ApiResponse(responseCode = "204",
                    description = "event are not available"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @GetMapping(produces = "application/json")
    public final ResponseEntity<List<Event>> list(final Principal
                                  principal,
                          @RequestHeader(name = "Accept-Language",
                                  required = false) final Locale locale)
            throws SQLException {
        final List<Event> tagList = eventService.list(
                principal.getName(), locale);
        return tagList.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(tagList);
    }


    /**
     * List the Event.
     *
     * @param principal
     * @param locale
     * @param request the request
     * @return list of event
     */
    @Operation(summary = "lists the event",
            description = " Can be invoked by auth users only",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Listing the event"),
            @ApiResponse(responseCode = "204",
                    description = "event are not available"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @GetMapping(value = "/**", produces = "application/json")
    public final ResponseEntity<List<Event>> list(final Principal
                                    principal,
                              @RequestHeader(name = "Accept-Language",
                                    required = false) final Locale locale,
                              final HttpServletRequest request)
            throws SQLException {
        final List<Event> tagList = eventService.list(
                principal.getName(), locale,
                getCategories(request.getRequestURI()));
        return tagList.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(tagList);
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
                                               UUID id,
                                               final Principal principal)
            throws SQLException {
        return eventService.isRegistered(principal.getName(),
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
                                               UUID id,
                                               final Principal principal)
            throws SQLException {
        return eventService.register(principal.getName(),
                id) ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * Starts an Event.
     *
     * @param id
     * @param url
     * @param principal
     * @return event
     */
    @Operation(summary = "Starts the event by given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "event registered successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "event not found")})
    @PostMapping("/{id}/_start")
    public final ResponseEntity<Void> start(@PathVariable final
                                           UUID id,
                                           final @RequestBody String url,
                                           final Principal principal)
            throws SQLException, URISyntaxException,
            MalformedURLException {
        return eventService.start(principal.getName(), id, new URL(url))
                ? ResponseEntity.created(new URI(url)).build()
                : ResponseEntity.notFound().build();
    }

    /**
     * Joins an Event.
     *
     * @param id
     * @param principal
     * @return event
     */
    @Operation(summary = "Joins the event by given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "event registered successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "event not found")})
    @PostMapping("/{id}/_join")
    public final ResponseEntity<Void> join(@PathVariable final
                                               UUID id,
                                               final Principal principal)
            throws SQLException {
        String meetingUrl = eventService.join(principal.getName(), id);
        return ResponseEntity.created(URI.create(meetingUrl)).build();
    }


    private List<String> getCategories(final String requestURI) {
        return List.of(requestURI
                .replaceFirst("/api/events/", "")
                .split("/"));
    }

}
