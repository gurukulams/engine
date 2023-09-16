package com.techatpark.workout.service;

import com.gurukulams.core.model.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * The type Events service.
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
     * Instantiates a new Events service.
     *
     * @param aJdbcClient the jdbc client
     */
    public EventService(final JdbcClient aJdbcClient) {
        this.jdbcClient = aJdbcClient;
    }

    private Events rowMapper(final ResultSet rs, final Integer rowNum)
            throws SQLException {
        Events event = new Events();
        event.setId((UUID) rs.getObject(INDEX_1));
        event.setTitle(rs.getString(INDEX_2));
        event.setDescription(rs.getString(INDEX_3));
        event.setEventDate(rs.getObject(INDEX_4, LocalDate.class));
                event.setCreatedAt(rs.getObject(INDEX_5, LocalDateTime.class));
        event.setCreatedBy(rs.getString(INDEX_6));
                event.setModifiedAt(rs.getObject(INDEX_7, LocalDateTime.class));
        event.setModifiedBy(rs.getString(INDEX_8));
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
    public Events create(final String userName, final Locale locale,
                        final Events event) {
        final UUID eventId = UUID.randomUUID();

        String insertEventsSQL = """
                INSERT INTO events(id, title, event_date,
                description, created_by)
                VALUES (?, ?, ?, ?, ?)
                """;
        jdbcClient.sql(insertEventsSQL)
                .param(INDEX_1, eventId)
                .param(INDEX_2, event.getTitle())
                .param(INDEX_3, event.getEventDate())
                .param(INDEX_4, event.getDescription())
                .param(INDEX_5, userName)
                .update();

        if (locale != null) {
            createLocalizedEvents(eventId, event, locale);
        }

        final Optional<Events> createdEvents = read(userName, locale, eventId);
        logger.info("Events Created {}", eventId);

        return createdEvents.get();
    }

    private int createLocalizedEvents(final UUID eventId, final Events event,
                                     final Locale locale) {
        String insertLocalizedEventsSQL = """
                INSERT INTO events_localized(
                event_id, locale, title, description)
                VALUES (?, ?, ?, ?)
                """;
        return jdbcClient.sql(insertLocalizedEventsSQL)
                .param(INDEX_1, eventId)
                .param(INDEX_2, locale.getLanguage())
                .param(INDEX_3, event.getTitle())
                .param(INDEX_4, event.getDescription())
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
    public Optional<Events> read(final String userName, final Locale locale,
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
    public Events update(final UUID id, final String userName,
                        final Locale locale, final Events event) {
        logger.debug("Entering update for Events {}", id);
        final String query = locale == null
                ? """
                UPDATE events SET title=?, event_date=?, description=?,
                modified_by=? WHERE id=?
                """
                : """
                UPDATE events SET event_date=?, modified_by=? WHERE id=?
                """;
        int updatedRows = locale == null
                ? jdbcClient.sql(query)
                .param(INDEX_1, event.getTitle())
                .param(INDEX_2, event.getEventDate())
                .param(INDEX_3, event.getDescription())
                .param(INDEX_4, userName)
                .param(INDEX_5, id).update()
                : jdbcClient.sql(query)
                .param(INDEX_1, event.getEventDate())
                .param(INDEX_2, userName)
                .param(INDEX_3, id).update();

        if (updatedRows == 0) {
            logger.error("Update not found {}", id);
            throw new IllegalArgumentException("Events not found");
        } else if (locale != null) {
            updatedRows = jdbcClient.sql("""
                            UPDATE events_localized SET title=?, locale=?,
                            description=?
                            WHERE event_id=? AND locale=?
                            """)
                    .param(INDEX_1, event.getTitle())
                    .param(INDEX_2, locale.getLanguage())
                    .param(INDEX_3, event.getDescription())
                    .param(INDEX_4, id)
                    .param(INDEX_5, locale.getLanguage())
                    .update();

            if (updatedRows == 0) {

                createLocalizedEvents(id, event, locale);
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
    public boolean delete(final String userName, final UUID id) {
        return jdbcClient.sql("DELETE FROM events WHERE id = ?")
                .param(INDEX_1, id)
                .update() == 1;
    }

    /**
     * List list.
     *
     * @param userName the user name
     * @param locale   the locale
     * @return the list
     */
    public List<Events> list(final String userName, final Locale locale) {
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
        LocalDateTime event = jdbcClient
                .sql("SELECT EVENT_DATE FROM EVENTS WHERE ID=?")
                .param(INDEX_1, eventId)
                .query(LocalDateTime.class)
                .single();
        if (LocalDateTime.now().isAfter(event)) {
            throw new IllegalArgumentException("Events Date is expired");
        }

        return jdbcClient
                .sql("INSERT INTO event_users(event_id, user_id) VALUES(?, ?)")
                .param(INDEX_1, eventId)
                .param(INDEX_2, getUserId(userEmail))
                .update() == 1;
    }

    /**
     * Gets user id.
     *
     * @param email the email
     * @return the user id
     */
    public String getUserId(final String email) {
        String query = """
                SELECT user_handle FROM LEARNER WHERE EMAIL=?
                """;
        return jdbcClient
                .sql(query)
                .param(INDEX_1, email)
                .query(String.class)
                .single();
    }

    /**
     * Delete all the events related data.
     */
    public void delete() {
        jdbcClient.sql("DELETE FROM event_users").update();
        jdbcClient.sql("DELETE FROM events_localized").update();
        jdbcClient.sql("DELETE FROM events").update();
    }
}
