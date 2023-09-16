package com.techatpark.workout.service;

import com.gurukulams.core.model.Events;
import com.techatpark.workout.starter.security.payload.SignupRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@SpringBootTest
class EventServiceTest {

    public static final String STATE_BOARD_IN_ENGLISH = "State Events";
    public static final String STATE_BOARD_DESCRIPTION_IN_ENGLISH = "State Events Description";
    public static final String STATE_BOARD_TITLE_IN_FRENCH = "Conseil d'État";
    public static final String STATE_BOARD_DESCRIPTION_IN_FRENCH = "Description du conseil d'État";
    private static final String EMAIL = "EMAIL@email.com";
    @Autowired
    private EventService eventService;

    @Autowired
    private LearnerService learnerService;

    /**
     * Before.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void before() throws IOException {
        cleanUp();
    }

    /**
     * After.
     */
    @AfterEach
    void after() {
        cleanUp();
    }

    private void cleanUp() {
        eventService.delete();
    }

    @Test
    void create() {
        final Events event = eventService.create("mani", null,
                anEvent());
        Assertions.assertTrue(eventService.read("mani", null, event.getId()).isPresent(),
                "Created Events");
    }

    @Test
    void read() {
        final Events event = eventService.create("mani", null,
                anEvent());
        final UUID newEventId = event.getId();
        Assertions.assertTrue(eventService.read("mani", null, newEventId).isPresent(),
                "Events Created");
    }

    @Test
    void update() {

        final Events event = eventService.create("mani", null,
                anEvent());
        final UUID newEventId = event.getId();
        Events newEvent = anEvent(anEvent(), "Events", "A " +
                "Events", LocalDate.now());
        Events updatedEvent = eventService
                .update(newEventId, "mani", null, newEvent);
        Assertions.assertEquals("Events", updatedEvent.getTitle(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            eventService
                    .update(UUID.randomUUID(), "mani", null, newEvent);
        });
    }

    @Test
    void delete() {

        final Events event = eventService.create("mani", null,
                anEvent());
        eventService.delete("mani", event.getId());
        Assertions.assertFalse(eventService.read("mani", null, event.getId()).isPresent(),
                "Deleted Events");

    }

    @Test
    void list() {

        final Events event = eventService.create("mani", null,
                anEvent());
        Events newEvent = anEvent(event, "Events New", "A " +
                "Events", LocalDate.now());
        eventService.create("mani", null,
                newEvent);
        List<Events> listofevent = eventService.list("manikanta", null);
        Assertions.assertEquals(2, listofevent.size());

    }

    @Test
    void testLocalizationFromDefaultWithoutLocale() {
        // Create a Events without locale
        final Events event = eventService.create("mani", null,
                anEvent());

        testLocalization(event);

    }

    @Test
    void testLocalizationFromCreateWithLocale() {
        // Create a Events with locale
        final Events event = eventService.create("mani", Locale.GERMAN,
                anEvent());

        testLocalization(event);

    }

    void testLocalization(Events event) {

        // Update for China Language
        eventService.update(event.getId(), "mani", Locale.FRENCH, anEvent(event,
                STATE_BOARD_TITLE_IN_FRENCH,
                STATE_BOARD_DESCRIPTION_IN_FRENCH,
                LocalDate.now()));

        // Get for french Language
        Events createEvent = eventService.read("mani", Locale.FRENCH,
                event.getId()).get();
        Assertions.assertEquals(STATE_BOARD_TITLE_IN_FRENCH, createEvent.getTitle());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_FRENCH, createEvent.getDescription());

        final UUID id = createEvent.getId();
        createEvent = eventService.list("mani", Locale.FRENCH)
                .stream()
                .filter(event1 -> event1.getId().equals(id))
                .findFirst().get();
        Assertions.assertEquals(STATE_BOARD_TITLE_IN_FRENCH, createEvent.getTitle());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_FRENCH,
                createEvent.getDescription());

        // Get for France which does not have data
        createEvent = eventService.read("mani", Locale.CHINESE,
                event.getId()).get();
        Assertions.assertEquals(STATE_BOARD_IN_ENGLISH, createEvent.getTitle());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_ENGLISH, createEvent.getDescription());

        createEvent = eventService.list("mani", Locale.CHINESE)
                .stream()
                .filter(event1 -> event1.getId().equals(id))
                .findFirst().get();

        Assertions.assertEquals(STATE_BOARD_IN_ENGLISH, createEvent.getTitle());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_ENGLISH, createEvent.getDescription());

    }

    @Test
    void testRegister() throws SQLException {
        learnerService.delete();
        learnerService.signUp(aSignupRequest(),
                s -> String.valueOf(new StringBuilder(s).reverse()));
        final Events event = anEvent();

        Assertions.assertTrue(eventService.register(eventService.create("mani", null,
                event).getId(), EMAIL));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            event.setEventDate(LocalDate.now().minusDays(1L));
            eventService.register(eventService.create("mani", null,
                    event).getId(), EMAIL);
        });

    }

    /**
     * Gets event.
     *
     * @return the event
     */
    Events anEvent() {
        Events event = new Events();
        event.setTitle(STATE_BOARD_IN_ENGLISH);
        event.setDescription(STATE_BOARD_DESCRIPTION_IN_ENGLISH);
        event.setEventDate(LocalDate.now().plusDays(1L));
        return event;
    }

    /**
     * Gets event from reference event.
     *
     * @return the event
     */
    Events anEvent(final Events ref,
                   final String title,
                   final String description,
                   final LocalDate eventDate) {
        Events event = new Events();
        event.setId(ref.getId());
        event.setTitle(title);
        event.setDescription(description);
        event.setEventDate(eventDate);
        event.setCreatedAt(ref.getCreatedAt());
        event.setCreatedBy(ref.getCreatedBy());
        event.setModifiedAt(ref.getModifiedAt());
        event.setModifiedBy(ref.getModifiedBy());
        return event;
    }

    SignupRequest aSignupRequest() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail(EMAIL);
        signupRequest.setImageUrl("/images/user.png");
        signupRequest.setPassword("password");
        return signupRequest;
    }
}
