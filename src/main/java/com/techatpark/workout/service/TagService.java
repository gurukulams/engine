package com.techatpark.workout.service;

import com.techatpark.workout.model.Tag;
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
 * The type Tag service.
 */
@Service
public final class TagService {

    /**
     * Logger Facade.
     */
    private final Logger logger =
            LoggerFactory.getLogger(TagService.class);

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
    public TagService(
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
    private Tag rowMapper(final ResultSet rs,
                               final Integer rowNum)
            throws SQLException {
        Tag tag = new Tag(
                rs.getString("id"),
                rs.getString("title"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getString("created_by"),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getString("modified_by"));
        return tag;
    }

    /**
     * inserts data.
     *
     * @param userName the userName
     * @param locale
     * @param tag      the tag
     * @return question optional
     */
    public Tag create(final String userName,
                           final Locale locale,
                           final Tag tag) {

        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("tags")
                .usingColumns("id", "title",
                        "created_by");

        final Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("id", tag.id());
        valueMap.put("title",
                tag.title());
        valueMap.put("created_by", userName);

        insert.execute(valueMap);

        if (locale != null) {
            valueMap.put("tag_id", tag.id());
            valueMap.put("locale", locale.getLanguage());
            createLocalizedTag(valueMap);
        }

        final Optional<Tag> optionalTag =
                read(userName, tag.id(), locale);

        logger.info("Created Tag {}", tag.id());

        return optionalTag.get();
    }

    /**
     * Create Localized Tag.
     *
     * @param valueMap
     * @return noOfCategories
     */
    private int createLocalizedTag(final Map<String, Object> valueMap) {
        return new SimpleJdbcInsert(dataSource)
                .withTableName("tags_localized")
                .usingColumns("tag_id", "locale", "title")
                .execute(valueMap);
    }

    /**
     * reads from tag.
     *
     * @param id       the id
     * @param userName the userName
     * @param locale
     * @return question optional
     */
    public Optional<Tag> read(final String userName,
                                   final String id,
                                   final Locale locale) {
        final String query = locale == null
                ? "SELECT id,title,created_by,"
                + "created_at, modified_at, modified_by FROM tags "
                + "WHERE id = ?"
                : "SELECT DISTINCT b.ID, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.TITLE "
                + "ELSE b.TITLE "
                + "END AS TITLE, "
                + "created_by,created_at, modified_at, modified_by "
                + "FROM TAGS b "
                + "LEFT JOIN TAGS_LOCALIZED bl "
                + "ON b.ID = bl.tag_id "
                + "WHERE b.ID = ? "
                + "AND (bl.LOCALE IS NULL "
                + "OR bl.LOCALE = ? OR "
                + "b.ID NOT IN "
                + "(SELECT tag_id FROM TAGS_LOCALIZED "
                + "WHERE tag_id=b.ID AND LOCALE = ?))";

        try {
            final Tag p = locale == null ? jdbcTemplate
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
     * update the tag.
     *
     * @param id       the id
     * @param userName the userName
     * @param locale
     * @param tag      the tag
     * @return question optional
     */
    public Tag update(final String id,
                           final String userName,
                           final Locale locale,
                           final Tag tag) {
        logger.debug("Entering update for Tag {}", id);
        final String query = locale == null
                ? "UPDATE tags SET title=?,"
                + "modified_by=? WHERE id=?"
                : "UPDATE tags SET modified_by=? WHERE id=?";
        Integer updatedRows = locale == null
                ? jdbcTemplate.update(query, tag.title(),
                userName, id)
                : jdbcTemplate.update(query, userName, id);
        if (updatedRows == 0) {
            logger.error("Update not found", id);
            throw new IllegalArgumentException("Tag not found");
        } else if (locale != null) {
            updatedRows = jdbcTemplate.update(
                    "UPDATE tags_localized SET title=?,locale=?"
                            + " WHERE tag_id=? AND locale=?",
                    tag.title(), locale.getLanguage(),
                    id, locale.getLanguage());
            if (updatedRows == 0) {
                final Map<String, Object> valueMap = new HashMap<>(4);
                valueMap.put("tag_id", id);
                valueMap.put("locale", locale.getLanguage());
                valueMap.put("title", tag.title());
                createLocalizedTag(valueMap);
            }
        }
        return read(userName, id, locale).get();
    }

    /**
     * delete the tag.
     *
     * @param id       the id
     * @param userName the userName
     * @return false
     */
    public Boolean delete(final String userName, final String id) {
        String query = "DELETE FROM tags WHERE ID=?";

        final Integer updatedRows = jdbcTemplate.update(query, id);
        return !(updatedRows == 0);
    }


    /**
     * list of tags.
     *
     * @param userName the userName
     * @param locale
     * @return tags list
     */
    public List<Tag> list(final String userName,
                               final Locale locale) {
        final String query = locale == null
                ? "SELECT id,title,created_by,"
                + "created_at, modified_at, modified_by FROM tags"
                : "SELECT DISTINCT b.ID, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.TITLE "
                + "ELSE b.TITLE "
                + "END AS TITLE, "
                + "created_by,created_at, modified_at, modified_by "
                + "FROM TAGS b "
                + "LEFT JOIN TAGS_LOCALIZED bl "
                + "ON b.ID = bl.tag_id "
                + "WHERE bl.LOCALE IS NULL "
                + "OR bl.LOCALE = ? OR "
                + "b.ID NOT IN "
                + "(SELECT tag_id FROM TAGS_LOCALIZED "
                + "WHERE tag_id=b.ID AND LOCALE = ?)";
        return locale == null
                ? jdbcTemplate.query(query, this::rowMapper)
                : jdbcTemplate
                .query(query, this::rowMapper,
                                locale.getLanguage(),
                                locale.getLanguage(),
                                locale.getLanguage());
    }

    /**
     * Cleaning up all tags.
     *
     * @return no.of tags deleted
     */
    public Integer deleteAll() {
        jdbcTemplate.update("DELETE FROM tags_localized");
        final String query = "DELETE FROM tags";
        return jdbcTemplate.update(query);
    }
}
