package com.techatpark.workout.service;

import com.techatpark.workout.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
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


@Service
public class EventService {

    /**
     * Logger Facade.
     */
    private final Logger logger =
            LoggerFactory.getLogger(EventService.class);

    /**
     * this helps to execute sql queries.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * this is the connection for the database.
     */
    private final DataSource dataSource;

    /**
     * this is the constructor.
     *
     * @param ajdbcTemplate a jdbcTemplate
     * @param adataSource   a dataSource
     */
    public EventService(final JdbcTemplate ajdbcTemplate,
                        final DataSource adataSource) {
        this.jdbcTemplate = ajdbcTemplate;
        this.dataSource = adataSource;
    }

    /**
     * Maps the data from and to the database.
     *
     * @param rs
     * @param rowNum
     * @return p
     * @throws SQLException
     */
    private Event rowMapper(final ResultSet rs,
                            final Integer rowNum)
            throws SQLException {
        Event event = new Event((UUID)
                rs.getObject("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getObject("event_date", LocalDate.class),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getString("created_by"),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getString("modified_by"));

        return event;
    }

    /**
     * creates new syllabus.
     *
     * @param userName the userName
     * @param event    the syllabus
     * @param locale   the locale
     * @return event optional
     */
    public Event create(final String userName,
                        final Locale locale,
                        final Event event) {
        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("events")
                .usingColumns("id", "title",
                        "event_date",
                        "description", "created_by");

        final Map<String, Object> valueMap = new HashMap<>();

        valueMap.put("title", event.title());
        valueMap.put("event_date", event.event_date());
        valueMap.put("description", event.description());
        valueMap.put("created_by", userName);

        final UUID eventId = UUID.randomUUID();
        valueMap.put("id", eventId);
        insert.execute(valueMap);


        if (locale != null) {
            valueMap.put("event_id", eventId);
            valueMap.put("locale", locale.getLanguage());
            createLocalizedEvent(valueMap);
        }

        final Optional<Event> createdEvent =
                read(userName, locale, eventId);

        logger.info("Event Created {}", eventId);

        return createdEvent.get();
    }

    /**
     * Create Localized Event.
     *
     * @param valueMap
     * @return noOfEvents
     */
    private int createLocalizedEvent(final Map<String, Object> valueMap) {
        return new SimpleJdbcInsert(dataSource)
                .withTableName("events_localized")
                .usingColumns("event_id", "locale", "title", "description")
                .execute(valueMap);
    }

    /**
     * reads from syllabus.
     *
     * @param id       the id
     * @param locale   the locale
     * @param userName the userName
     * @return event optional
     */
    public Optional<Event> read(final String userName,
                                final Locale locale, final UUID id) {

        final String query = locale == null
                ? "SELECT id,title,description,event_date,created_by,"
                + "created_at, modified_at, modified_by FROM events "
                + "WHERE id = ?"
                : "SELECT DISTINCT b.ID, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.TITLE "
                + "ELSE b.TITLE "
                + "END AS TITLE, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.DESCRIPTION "
                + "ELSE b.DESCRIPTION "
                + "END AS DESCRIPTION,"
                + "event_date,created_by,created_at, modified_at, modified_by "
                + "FROM EVENTS b "
                + "LEFT JOIN EVENTS_LOCALIZED bl "
                + "ON b.ID = bl.EVENT_ID "
                + "WHERE b.ID = ? "
                + "AND (bl.LOCALE IS NULL "
                + "OR bl.LOCALE = ? OR "
                + "b.ID NOT IN "
                + "(SELECT EVENT_ID FROM EVENTS_LOCALIZED "
                + "WHERE EVENT_ID=b.ID AND LOCALE = ?))";

        try {
            final Event p = locale == null ? jdbcTemplate
                    .queryForObject(query, this::rowMapper, id)
                    : jdbcTemplate
                    .queryForObject(query, this::rowMapper,
                            locale.getLanguage(),
                            locale.getLanguage(),
                            id,
                            locale.getLanguage(),
                            locale.getLanguage());
            return Optional.of(p);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * update the event.
     *
     * @param id       the id
     * @param userName the userName
     * @param event    the event
     * @param locale   the locale
     * @return event optional
     */
    public Event update(final UUID id,
                        final String userName,
                        final Locale locale,
                        final Event event) {
        logger.debug("Entering update for Event {}", id);
        final String query = locale == null
                ? "UPDATE events SET title=?,event_date=?,"
                + "description=?,modified_by=? WHERE id=?"
                : "UPDATE events SET event_date=?,modified_by=? WHERE id=?";
        Integer updatedRows = locale == null
                ? jdbcTemplate.update(query, event.title(),
                event.event_date(),
                event.description(), userName, id)
                : jdbcTemplate.update(query, event.event_date(), userName, id);
        if (updatedRows == 0) {
            logger.error("Update not found", id);
            throw new IllegalArgumentException("Event not found");
        } else if (locale != null) {
            updatedRows = jdbcTemplate.update(
                    "UPDATE events_localized SET title=?,locale=?,"
                            + "description=? WHERE event_id=? AND locale=?",
                    event.title(), locale.getLanguage(),
                    event.description(), id, locale.getLanguage());
            if (updatedRows == 0) {
                final Map<String, Object> valueMap = new HashMap<>(4);
                valueMap.put("event_id", id);
                valueMap.put("locale", locale.getLanguage());
                valueMap.put("title", event.title());
                valueMap.put("description", event.description());
                createLocalizedEvent(valueMap);
            }
        }
        return read(userName, locale, id).get();
    }

    /**
     * delete the event.
     *
     * @param id       the id
     * @param userName the userName
     * @return event optional
     */
    public Boolean delete(final String userName, final UUID id) {
        final String query = "DELETE FROM events WHERE id = ?";
        final Integer updatedRows = jdbcTemplate.update(query, id);
        return !(updatedRows == 0);
    }

    /**
     * list the event.
     *
     * @param userName the userName
     * @param locale   the locale
     * @return event optional
     */
    public List<Event> list(final String userName,
                            final Locale locale) {
        final String query = locale == null
                ? "SELECT id,title,description,event_date,created_by,"
                + "created_at, modified_at, modified_by FROM events"
                : "SELECT DISTINCT b.ID, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.TITLE "
                + "ELSE b.TITLE "
                + "END AS TITLE, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.DESCRIPTION "
                + "ELSE b.DESCRIPTION "
                + "END AS DESCRIPTION,"
                + "event_date,created_by,created_at, modified_at, modified_by "
                + "FROM EVENTS b "
                + "LEFT JOIN EVENTS_LOCALIZED bl "
                + "ON b.ID = bl.EVENT_ID "
                + "WHERE bl.LOCALE IS NULL "
                + "OR bl.LOCALE = ? OR "
                + "b.ID NOT IN "
                + "(SELECT EVENT_ID FROM EVENTS_LOCALIZED "
                + "WHERE EVENT_ID=b.ID AND LOCALE = ?)";
        return locale == null
                ? jdbcTemplate.query(query, this::rowMapper)
                : jdbcTemplate
                .query(query, this::rowMapper,
                        locale.getLanguage(),
                        locale.getLanguage(),
                        locale.getLanguage(),
                        locale.getLanguage()
                );

    }

    /**
     * Register for event user.
     *
     * @param eventId   the event id
     * @param userEmail the user email
     * @return the event user
     */
    public boolean register(final UUID eventId,
                            final String userEmail) {

        String query = "SELECT EVENT_DATE FROM EVENTS WHERE ID=?";
        LocalDateTime event = jdbcTemplate
                .queryForObject(query, LocalDateTime.class, eventId);
        if (LocalDateTime.now().isAfter(event)) {
            throw new IllegalArgumentException("Event Date is expired");
        }
        UUID userId = getUserId(userEmail);

        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("event_users")
                .usingColumns("event_id", "user_id");

        final Map<String, Object> valueMap = new HashMap<>();

        valueMap.put("event_id", eventId);
        valueMap.put("user_id", userId);

        return insert.execute(valueMap) == 1;
    }

    /**
     * Gets User Id for email.
     *
     * @param email the email
     * @return bookId user id
     */
    public UUID getUserId(final String email) {
        String query = "SELECT ID FROM LEARNER WHERE EMAIL=?";
        return jdbcTemplate
                .queryForObject(query, UUID.class, email);
    }

    /**
     * Cleaning up all events.
     */
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM event_users");
        jdbcTemplate.update("DELETE FROM events_localized");
        jdbcTemplate.update("DELETE FROM events");
    }
}
