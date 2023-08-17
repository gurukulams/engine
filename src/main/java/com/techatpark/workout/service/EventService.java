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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * The type event service.
 */
@Service
public final class EventService {

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
     * @param anJdbcTemplate
     * @param aDataSource
     */
    public EventService(
            final JdbcTemplate anJdbcTemplate, final DataSource aDataSource) {
        this.jdbcTemplate = anJdbcTemplate;
        this.dataSource = aDataSource;
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
        Event event = new Event(
                rs.getString("id"),
                rs.getString("title"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getString("location"),
                rs.getObject("starts_at", LocalDateTime.class),
                rs.getObject("ends_at", LocalDateTime.class),
                rs.getString("description"),
                rs.getString("organizer"),
                rs.getInt("max_attendees"));
        return event;
    }

    /**
     * inserts data.
     *
     * @param userName the userName
     * @param locale
     * @param event      the event
     * @return question optional
     */
    public Event create(final String userName,
                           final Locale locale,
                           final Event event) {

        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("events")
                .usingColumns("id", "title", "location",
                        "created_by");

        final Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("id", event.id());
        valueMap.put("title",
            event.title());
        valueMap.put("location", event.location());
        valueMap.put("created_by", userName);

        insert.execute(valueMap);

        if (locale != null) {
            valueMap.put("event_id", event.id());
            valueMap.put("locale", locale.getLanguage());
            createLocalizedEvent(valueMap);
        }

        final Optional<Event> optionalevent =
                read(userName, event.id(), locale);

        logger.info("Created event {}", event.id());

        return optionalevent.get();
    }

    /**
     * Create Localized event.
     *
     * @param valueMap
     * @return noOfevents
     */
    private int createLocalizedEvent(final Map<String, Object> valueMap) {
        return new SimpleJdbcInsert(dataSource)
                .withTableName("events_localized")
                .usingColumns("event_id", "locale", "title")
                .execute(valueMap);
    }

    /**
     * reads from event.
     *
     * @param id       the id
     * @param userName the userName
     * @param locale
     * @return question optional
     */
    public Optional<Event> read(final String userName,
                                   final String id,
                                   final Locale locale) {
        final String query = locale == null
                ? "SELECT id,title,created_by,"
                + "created_at, modified_at, modified_by,location,"
                + "ends_at, description,organizer,max_attendees FROM events "
                + "WHERE id = ?"
                : "SELECT DISTINCT b.ID, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.TITLE "
                + "ELSE b.TITLE "
                + "END AS TITLE, "
                + "created_by,created_at, modified_at, modified_by "
                + "FROM events b "
                + "LEFT JOIN events_localized bl "
                + "ON b.ID = bl.event_id "
                + "WHERE b.ID = ? "
                + "AND (bl.LOCALE IS NULL "
                + "OR bl.LOCALE = ? OR "
                + "b.ID NOT IN "
                + "(SELECT event_id FROM events_localized "
                + "WHERE event_id=b.ID AND LOCALE = ?))";

        try {
            final Event p = locale == null ? jdbcTemplate
                    .queryForObject(query, this::rowMapper, id)
                    : jdbcTemplate
                    .queryForObject(query, this::rowMapper,
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
     * @param locale
     * @param event      the event
     * @return question optional
     */
    public Event update(final String id,
                           final String userName,
                           final Locale locale,
                           final Event event) {
        logger.debug("Entering update for event {}", id);
        final String query = locale == null
                ? "UPDATE events SET title=?,"
                + "modified_by=? WHERE id=?"
                : "UPDATE events SET modified_by=? WHERE id=?";
        Integer updatedRows = locale == null
                ? jdbcTemplate.update(query, event.title(),
                userName, id)
                : jdbcTemplate.update(query, userName, id);
        if (updatedRows == 0) {
            logger.error("Update not found", id);
            throw new IllegalArgumentException("event not found");
        } else if (locale != null) {
            updatedRows = jdbcTemplate.update(
                    "UPDATE events_localized SET title=?,locale=?"
                            + " WHERE event_id=? AND locale=?",
                event.title(), locale.getLanguage(),
                    id, locale.getLanguage());
            if (updatedRows == 0) {
                final Map<String, Object> valueMap = new HashMap<>(4);
                valueMap.put("event_id", id);
                valueMap.put("locale", locale.getLanguage());
                valueMap.put("title", event.title());
                createLocalizedEvent(valueMap);
            }
        }
        return read(userName, id, locale).get();
    }

    /**
     * delete the event.
     *
     * @param id       the id
     * @param userName the userName
     * @return false
     */
    public Boolean delete(final String userName, final String id) {
        String query = "DELETE FROM events WHERE ID=?";

        final Integer updatedRows = jdbcTemplate.update(query, id);
        return !(updatedRows == 0);
    }


    /**
     * list of events.
     *
     * @param userName the userName
     * @param locale
     * @return events list
     */
    public List<Event> list(final String userName,
                               final Locale locale) {
        final String query = locale == null
                ? "SELECT id,title,created_by,"
                + "created_at, modified_at, modified_by,location,"
                + "ends_at, description,organizer,max_attendees FROM events "
                : "SELECT DISTINCT b.ID, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.TITLE "
                + "ELSE b.TITLE "
                + "END AS TITLE, "
                + "created_by,created_at, modified_at, modified_by "
                + "FROM events b "
                + "LEFT JOIN events_localized bl "
                + "ON b.ID = bl.event_id "
                + "WHERE bl.LOCALE IS NULL "
                + "OR bl.LOCALE = ? OR "
                + "b.ID NOT IN "
                + "(SELECT event_id FROM events_localized "
                + "WHERE event_id=b.ID AND LOCALE = ?)";
        return locale == null
                ? jdbcTemplate.query(query, this::rowMapper)
                : jdbcTemplate
                .query(query, this::rowMapper,
                        locale.getLanguage(),
                        locale.getLanguage(),
                        locale.getLanguage());
    }

    /**
     * Cleaning up all events.
     *
     * @return no.of events deleted
     */
    public Integer deleteAll() {
        jdbcTemplate.update("DELETE FROM events_localized");
        final String query = "DELETE FROM events";
        return jdbcTemplate.update(query);
    }
}
