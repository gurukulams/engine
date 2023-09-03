package com.techatpark.workout.service;

import com.techatpark.workout.model.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@SpringBootTest
public class EventServiceTest {

    public static final String STATE_BOARD_IN_ENGLISH = "State Event";
    public static final String STATE_BOARD_DESCRIPTION_IN_ENGLISH = "State Event Description";
    public static final String STATE_BOARD_TITLE_IN_FRENCH = "Conseil d'État";
    public static final String STATE_BOARD_DESCRIPTION_IN_FRENCH = "Description du conseil d'État";
    @Autowired
    private EventService eventService;

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
        eventService.deleteAll();
    }

    @Test
    void create() {
        final Event event = eventService.create("mani", null,
                anEvent());
        Assertions.assertTrue(eventService.read("mani", null, event.id()).isPresent(),
                "Created Event");
    }

    @Test
    void read() {
        final Event event = eventService.create("mani", null,
                anEvent());
        final UUID newEventId = event.id();
        Assertions.assertTrue(eventService.read("mani", null, newEventId).isPresent(),
                "Event Created");
    }

    @Test
    void update() {

        final Event event = eventService.create("mani", null,
                anEvent());
        final UUID newEventId = event.id();
        Event newEvent = new Event(null, "Event", "A " +
                "Event", LocalDate.now(),
                null, "tom", null, null);
        Event updatedEvent = eventService
                .update(newEventId, "mani", null, newEvent);
        Assertions.assertEquals("Event", updatedEvent.title(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            eventService
                    .update(UUID.randomUUID(), "mani", null, newEvent);
        });
    }

    @Test
    void delete() {

        final Event event = eventService.create("mani", null,
                anEvent());
        eventService.delete("mani", event.id());
        Assertions.assertFalse(eventService.read("mani", null, event.id()).isPresent(),
                "Deleted Event");

    }

    @Test
    void list() {

        final Event event = eventService.create("mani", null,
                anEvent());
        Event newEvent = new Event(null, "Event New", "A " +
                "Event", LocalDate.now(),
                null, "tom", null, null);
        eventService.create("mani", null,
                newEvent);
        List<Event> listofevent = eventService.list("manikanta", null);
        Assertions.assertEquals(2, listofevent.size());

    }

    @Test
    void testLocalizationFromDefaultWithoutLocale() {
        // Create a Event without locale
        final Event event = eventService.create("mani", null,
                anEvent());

        testLocalization(event);

    }

    @Test
    void testLocalizationFromCreateWithLocale() {
        // Create a Event with locale
        final Event event = eventService.create("mani", Locale.GERMAN,
                anEvent());

        testLocalization(event);

    }

    void testLocalization(Event event) {

        // Update for China Language
        eventService.update(event.id(), "mani", Locale.FRENCH, anEvent(event,
                STATE_BOARD_TITLE_IN_FRENCH,
                STATE_BOARD_DESCRIPTION_IN_FRENCH));

        // Get for french Language
        Event createEvent = eventService.read("mani", Locale.FRENCH,
                event.id()).get();
        Assertions.assertEquals(STATE_BOARD_TITLE_IN_FRENCH, createEvent.title());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_FRENCH, createEvent.description());

        final UUID id = createEvent.id();
        createEvent = eventService.list("mani", Locale.FRENCH)
                .stream()
                .filter(event1 -> event1.id().equals(id))
                .findFirst().get();
        Assertions.assertEquals(STATE_BOARD_TITLE_IN_FRENCH, createEvent.title());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_FRENCH,
                createEvent.description());

        // Get for France which does not have data
        createEvent = eventService.read("mani", Locale.CHINESE,
                event.id()).get();
        Assertions.assertEquals(STATE_BOARD_IN_ENGLISH, createEvent.title());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_ENGLISH, createEvent.description());

        createEvent = eventService.list("mani", Locale.CHINESE)
                .stream()
                .filter(event1 -> event1.id().equals(id))
                .findFirst().get();

        Assertions.assertEquals(STATE_BOARD_IN_ENGLISH, createEvent.title());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_ENGLISH, createEvent.description());

    }

    /**
     * Gets event.
     *
     * @return the event
     */
    Event anEvent() {
        Event event = new Event(null, STATE_BOARD_IN_ENGLISH,
                STATE_BOARD_DESCRIPTION_IN_ENGLISH, LocalDate.now().plusDays(1L),
                null, null,
                null, null);
        return event;
    }

    /**
     * Gets event from reference event.
     *
     * @return the event
     */
    Event anEvent(final Event ref, final String title, final String description) {
        return new Event(ref.id(), title,
                description, ref.event_date(),
                ref.created_at(), ref.created_by(),
                ref.modified_at(), ref.modified_by());
    }
}
