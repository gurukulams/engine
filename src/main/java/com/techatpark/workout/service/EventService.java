package com.techatpark.workout.service;

import com.techatpark.workout.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The type Event service.
 */
@Service
public class EventService {

    /**
     * Index.
     */
    private static final int INDEX_1 = 1;
    /**
     * Index.
     */
    private static final int INDEX_2 = 2;
    /**
     * Index.
     */
    private static final int INDEX_3 = 3;
    /**
     * Index.
     */
    private static final int INDEX_4 = 4;
    /**
     * Index.
     */
    private static final int INDEX_5 = 5;
    /**
     * Index.
     */
    private static final int INDEX_6 = 6;
    /**
     * Index.
     */
    private static final int INDEX_7 = 7;
    /**
     * Index.
     */
    private static final int INDEX_8 = 8;
    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(EventService.class);
    /**
     * JdbcClient.
     */
    private final JdbcClient jdbcClient;

    /**
     * Instantiates a new Event service.
     *
     * @param aJdbcClient the jdbc client
     */
    public EventService(final JdbcClient aJdbcClient) {
        this.jdbcClient = aJdbcClient;
    }

    private Event rowMapper(final ResultSet rs, final Integer rowNum)
            throws SQLException {
        Event event = new Event((UUID) rs.getObject(INDEX_1),
                rs.getString(INDEX_2),
                rs.getString(INDEX_3),
                rs.getObject(INDEX_4, LocalDate.class),
                rs.getObject(INDEX_5, LocalDateTime.class),
                rs.getString(INDEX_6),
                rs.getObject(INDEX_7, LocalDateTime.class),
                rs.getString(INDEX_8));

        return event;
    }

    /**
     * Create event.
     *
     * @param userName the user name
     * @param locale   the locale
     * @param event    the event
     * @return the event
     */
    public Event create(final String userName, final Locale locale,
                        final Event event) {
        final UUID eventId = UUID.randomUUID();

        String insertEventSQL = """
                INSERT INTO events(id, title, event_date,
                description, created_by)
                VALUES (?, ?, ?, ?, ?)
                """;
        jdbcClient.sql(insertEventSQL)
                .param(INDEX_1, eventId)
                .param(INDEX_2, event.title())
                .param(INDEX_3, event.event_date())
                .param(INDEX_4, event.description())
                .param(INDEX_5, userName)
                .update();

        if (locale != null) {
            createLocalizedEvent(eventId, event, locale);
        }

        final Optional<Event> createdEvent = read(userName, locale, eventId);
        logger.info("Event Created {}", eventId);

        return createdEvent.get();
    }

    private int createLocalizedEvent(final UUID eventId, final Event event,
                                     final Locale locale) {
        String insertLocalizedEventSQL = """
                INSERT INTO events_localized(
                event_id, locale, title, description)
                VALUES (?, ?, ?, ?)
                """;
        return jdbcClient.sql(insertLocalizedEventSQL)
                .param(INDEX_1, eventId)
                .param(INDEX_2, locale.getLanguage())
                .param(INDEX_3, event.title())
                .param(INDEX_4, event.description())
                .update();
    }

    /**
     * Read optional.
     *
     * @param userName the user name
     * @param locale   the locale
     * @param id       the id
     * @return the optional
     */
    public Optional<Event> read(final String userName, final Locale locale,
                                final UUID id) {
        final String query = locale == null
                ? """
                SELECT id, title, description, event_date, created_at,
                created_by, modified_at, modified_by
                FROM events
                WHERE id = ?
                """
                : """
                SELECT DISTINCT e.id,
                    CASE WHEN el.locale = ? THEN el.title
                    ELSE e.title END AS title,
                    CASE WHEN el.locale = ? THEN el.description
                    ELSE e.description END AS description,
                    e.event_date, e.created_at, e.created_by,
                    e.modified_at, e.modified_by
                FROM events e
                LEFT JOIN events_localized el ON e.id = el.event_id
                WHERE e.id = ?
                    AND (el.locale IS NULL OR el.locale = ? OR e.id NOT IN (
                        SELECT event_id
                        FROM events_localized
                        WHERE event_id = e.id AND locale = ?
                    ))
                """;


            return locale == null ? jdbcClient
                    .sql(query).param(INDEX_1, id).query(this::rowMapper)
                    .optional()
                    : jdbcClient.sql(query)
                    .param(INDEX_1, locale.getLanguage())
                    .param(INDEX_2, locale.getLanguage())
                    .param(INDEX_3, id)
                    .param(INDEX_4, locale.getLanguage())
                    .param(INDEX_5, locale.getLanguage())
                    .query(this::rowMapper).optional();

    }

    /**
     * Update event.
     *
     * @param id       the id
     * @param userName the user name
     * @param locale   the locale
     * @param event    the event
     * @return the event
     */
    public Event update(final UUID id, final String userName,
                        final Locale locale, final Event event) {
        logger.debug("Entering update for Event {}", id);
        final String query = locale == null
                ? """
                UPDATE events SET title=?, event_date=?, description=?,
                modified_by=? WHERE id=?
                """
                : """
                UPDATE events SET event_date=?, modified_by=? WHERE id=?
                """;
        Integer updatedRows = locale == null
                ? jdbcClient.sql(query)
                .param(INDEX_1, event.title())
                .param(INDEX_2, event.event_date())
                .param(INDEX_3, event.description())
                .param(INDEX_4, userName)
                .param(INDEX_5, id).update()
                : jdbcClient.sql(query)
                .param(INDEX_1, event.event_date())
                .param(INDEX_2, userName)
                .param(INDEX_3, id).update();

        if (updatedRows == 0) {
            logger.error("Update not found", id);
            throw new IllegalArgumentException("Event not found");
        } else if (locale != null) {
            updatedRows = jdbcClient.sql("""
                            UPDATE events_localized SET title=?, locale=?,
                            description=?
                            WHERE event_id=? AND locale=?
                            """)
                    .param(INDEX_1, event.title())
                    .param(INDEX_2, locale.getLanguage())
                    .param(INDEX_3, event.description())
                    .param(INDEX_4, id)
                    .param(INDEX_5, locale.getLanguage())
                    .update();

            if (updatedRows == 0) {
                final Map<String, Object> valueMap = new HashMap<>(4);
                valueMap.put("event_id", id);
                valueMap.put("locale", locale.getLanguage());
                valueMap.put("title", event.title());
                valueMap.put("description", event.description());
                createLocalizedEvent(id, event, locale);
            }
        }
        return read(userName, locale, id).get();
    }

    /**
     * Delete boolean.
     *
     * @param userName the user name
     * @param id       the id
     * @return the boolean
     */
    public Boolean delete(final String userName, final UUID id) {
        final String query = """
                DELETE FROM events WHERE id = ?
                """;
        final Integer updatedRows = jdbcClient.sql(query)
                .param(INDEX_1, id)
                .update();
        return !(updatedRows == 0);
    }

    /**
     * List list.
     *
     * @param userName the user name
     * @param locale   the locale
     * @return the list
     */
    public List<Event> list(final String userName, final Locale locale) {
        final String query = locale == null
                ? """
                SELECT id, title, description, event_date, created_at,
                created_by, modified_at, modified_by
                FROM events
                """
                : """
                SELECT DISTINCT e.id,
                    CASE WHEN el.locale = ? THEN el.title
                    ELSE e.title END AS title,
                    CASE WHEN el.locale = ? THEN el.description
                    ELSE e.description END AS description,
                    e.event_date, e.created_at, e.created_by,
                    e.modified_at, e.modified_by
                FROM events e
                LEFT JOIN events_localized el ON e.id = el.event_id
                WHERE el.locale IS NULL OR el.locale = ? OR e.id NOT IN (
                    SELECT event_id
                    FROM events_localized
                    WHERE event_id = e.id AND locale = ?
                )
                """;
        return locale == null
                ? jdbcClient.sql(query).query(this::rowMapper).list()
                : jdbcClient.sql(query)
                .param(INDEX_1, locale.getLanguage())
                .param(INDEX_2, locale.getLanguage())
                .param(INDEX_3, locale.getLanguage())
                .param(INDEX_4, locale.getLanguage())
                .query(this::rowMapper).list();
    }

    /**
     * Register boolean.
     *
     * @param eventId   the event id
     * @param userEmail the user email
     * @return the boolean
     */
    public boolean register(final UUID eventId, final String userEmail) {
        String query = """
                SELECT EVENT_DATE FROM EVENTS WHERE ID=?
                """;
        LocalDateTime event = jdbcClient
                .sql(query)
                .param(INDEX_1, eventId)
                .query(LocalDateTime.class)
                .single();
        if (LocalDateTime.now().isAfter(event)) {
            throw new IllegalArgumentException("Event Date is expired");
        }
        UUID userId = getUserId(userEmail);


        String insertQuery = """
                INSERT INTO event_users(event_id, user_id) VALUES(?, ?)
                """;

        return jdbcClient.sql(insertQuery)
                .param(INDEX_1, eventId)
                .param(INDEX_2, userId).update() == 1;
    }

    /**
     * Gets user id.
     *
     * @param email the email
     * @return the user id
     */
    public UUID getUserId(final String email) {
        String query = """
                SELECT ID FROM LEARNER WHERE EMAIL=?
                """;
        return jdbcClient
                .sql(query)
                .param(INDEX_1, email)
                .query(UUID.class)
                .single();
    }

    /**
     * Delete all.
     */
    public void deleteAll() {
        jdbcClient.sql("DELETE FROM event_users").update();
        jdbcClient.sql("DELETE FROM events_localized").update();
        jdbcClient.sql("DELETE FROM events").update();
    }
}
