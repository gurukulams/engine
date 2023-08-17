package com.techatpark.workout.controller;

import com.techatpark.workout.model.Event;
import com.techatpark.workout.service.EventService;
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
import java.util.List;
import java.util.Locale;

/**
 * The type event api controller.
 */
@RestController
@RequestMapping("/api/events")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Event",
        description = "Resource to manage Events")
@SecurityScheme(
    name = "bearerAuth",
    scheme = "bearer",
    bearerFormat = "JWT",
    type = SecuritySchemeType.HTTP,
    in = SecuritySchemeIn.HEADER
)
class EventAPIController {

    /**
     * declare a Event service.
     */
    private final EventService eventService;

    EventAPIController(final EventService paramEventService) {
        this.eventService = paramEventService;
    }

    @Operation(summary = "Creates a new Event",
            description = "Can be called "
                    + "only by users with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "201",
            description = "Event created successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Event is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = "application/json", consumes = "application/json")
    public ResponseEntity<Event> create(final Principal principal,
                                       @RequestHeader(name = "Accept-Language",
                                  required = false) final Locale locale,
                                       final @RequestBody
                                       Event event) {
        Event createdEvent =
                eventService.create(principal.getName(), locale, event);
        return ResponseEntity.created(URI.create("/api/events/"
                        + createdEvent.id()))
                .body(createdEvent);

    }

    @Operation(summary = "Get the event with given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "getting Event successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "Event not found")})
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Event> read(final @PathVariable String id,
                                     @RequestHeader(name = "Accept-Language",
                            required = false) final Locale locale,
                                     final Principal principal) {
        return ResponseEntity.of(
                eventService.read(principal.getName(), id, locale));
    }

    @Operation(summary = "Updates the Event by given id",
            description = "Can be called only by users "
                    + "with 'auth management' rights.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Event updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Event is invalid"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "Event not found")})
    @PutMapping(value = "/{id}", produces = "application/json", consumes =
            "application/json")
    public ResponseEntity<Event> update(final @PathVariable
                                              String id,
                                           final Principal
                                              principal,
                                   @RequestHeader(name = "Accept-Language",
                              required = false) final Locale locale,
                                           final @RequestBody
                                           Event
                                              event) {
        final Event updatedEvent =
            eventService.update(id, principal.getName(), locale, event);
        return ResponseEntity.ok(updatedEvent);
    }

    @Operation(summary = "Deletes the Event by given id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Event deleted successfully"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials"),
            @ApiResponse(responseCode = "404",
                    description = "Event not found")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(final @PathVariable
                                               String id,
                                       final Principal
                                               principal) {
        return eventService.delete(principal.getName(), id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "lists the Events",
            description = "Can be invoked by auth users only",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Listing the events successfully"),
            @ApiResponse(responseCode = "204",
                    description = "Events are not available"),
            @ApiResponse(responseCode = "401",
                    description = "invalid credentials")})
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<Event>> list(final Principal
                                                  principal,
                                   @RequestHeader(name = "Accept-Language",
                              required = false) final Locale locale) {
        final List<Event> events = eventService.list(
                principal.getName(), locale);
        return events.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(events);
    }


}
