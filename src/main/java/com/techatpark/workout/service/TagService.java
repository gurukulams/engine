package com.techatpark.workout.service;

import com.gurukulams.core.model.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The type Tag service.
 */
@Service
public final class TagService {

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
     * Tags table.
     */
    private static final String TAGS_TABLE = "tags";
    /**
     * Tags localized table.
     */
    private static final String TAGS_LOCALIZED_TABLE = "tags_localized";

    /**
     * Logger.
     */
    private final Logger logger =
            LoggerFactory.getLogger(TagService.class);
    /**
     * JdbcClient.
     */
    private final JdbcClient jdbcClient;

    /**
     * Instantiates a new Tag service.
     *
     * @param ajdbcClient the jdbc client
     */
    public TagService(final JdbcClient ajdbcClient) {
        this.jdbcClient = ajdbcClient;
    }

    private Tags rowMapper(final ResultSet rs, final Integer rowNum)
            throws SQLException {
        Tags tag = new Tags();
        tag.setId(rs.getString(INDEX_1));
        tag.setTitle(rs.getString(INDEX_2));
        tag.setCreatedAt(rs.getObject(INDEX_3, LocalDateTime.class));
        tag.setCreatedBy(rs.getString(INDEX_4));
        tag.setModifiedAt(rs.getObject(INDEX_5, LocalDateTime.class));
        tag.setModifiedBy(rs.getString(INDEX_6));
        return tag;
    }

    /**
     * Inserts data.
     *
     * @param userName the user name
     * @param locale   the locale
     * @param tag      the tag
     * @return the tag
     */
    public Tags create(final String userName,
                      final Locale locale,
                      final Tags tag) {

        final String insertTagQuery = """
                INSERT INTO %s(id, title, created_by)
                VALUES (?, ?, ?)
                """.formatted(TAGS_TABLE);

        jdbcClient.sql(insertTagQuery)
                .param(INDEX_1, tag.getId())
                .param(INDEX_2, tag.getTitle())
                .param(INDEX_3, userName)
                .update();

        if (locale != null) {
            jdbcClient.sql("""
                            INSERT INTO %s(tag_id, locale, title)
                            VALUES (?, ?, ?)
                            """.formatted(TAGS_LOCALIZED_TABLE))
                    .param(INDEX_1, tag.getId())
                    .param(INDEX_2, locale.getLanguage())
                    .param(INDEX_3, tag.getTitle())
                    .update();
        }

        final Optional<Tags> optionalTag = read(userName, tag.getId(), locale);

        logger.info("Created Tag {}", tag.getId());

        return optionalTag.get();
    }

    /**
     * Reads data from tag.
     *
     * @param userName the user name
     * @param id       the id
     * @param locale   the locale
     * @return the optional tag
     */
    public Optional<Tags> read(final String userName,
                              final String id,
                              final Locale locale) {
        final String query = locale == null
                ? """
                SELECT id, title, created_at, created_by, modified_at,
                modified_by FROM %s WHERE id = ?
                """.formatted(TAGS_TABLE)
                : """
                SELECT DISTINCT b.ID,
                CASE WHEN bl.LOCALE = ?
                THEN bl.TITLE
                ELSE b.TITLE
                END AS TITLE,
                created_at, created_by, modified_at, modified_by
                FROM %s b
                LEFT JOIN %s bl ON b.ID = bl.tag_id
                WHERE b.ID = ?
                AND (bl.LOCALE IS NULL
                OR bl.LOCALE = ?
                OR b.ID NOT IN (
                SELECT tag_id FROM %s
                WHERE tag_id=b.ID AND LOCALE = ?))
                """.formatted(TAGS_TABLE, TAGS_LOCALIZED_TABLE,
                TAGS_LOCALIZED_TABLE);


            return locale == null ? jdbcClient
                    .sql(query)
                    .param(INDEX_1, id)
                    .query(this::rowMapper)
                    .optional()
                    : jdbcClient
                    .sql(query)
                    .param(INDEX_1, locale.getLanguage())
                    .param(INDEX_2, id)
                    .param(INDEX_3, locale.getLanguage())
                    .param(INDEX_4, locale.getLanguage())
                    .query(this::rowMapper)
                    .optional();

    }

    /**
     * Update the tag.
     *
     * @param id       the id
     * @param userName the user name
     * @param locale   the locale
     * @param tag      the tag
     * @return the tag
     */
    public Tags update(final String id,
                      final String userName,
                      final Locale locale,
                      final Tags tag) {
        logger.debug("Entering update for Tag {}", id);
        final String updateTagQuery = locale == null
                ?
                "UPDATE %s SET title=?, modified_by=? WHERE id=?".formatted(
                        TAGS_TABLE)
                :
                "UPDATE %s SET modified_by=? WHERE id=?".formatted(TAGS_TABLE);

        final int updatedRows = locale == null ? jdbcClient
                .sql(updateTagQuery)
                .param(INDEX_1, tag.getTitle())
                .param(INDEX_2, userName)
                .param(INDEX_3, id)
                .update()
                : jdbcClient
                .sql(updateTagQuery)
                .param(INDEX_1, userName)
                .param(INDEX_2, id)
                .update();

        if (updatedRows == 0) {
            logger.error("Update not found", id);
            throw new IllegalArgumentException("Tag not found");
        } else if (locale != null) {
            final int localizedTagUpdatedRows = jdbcClient
                    .sql("""
                            UPDATE %s
                            SET title=?, locale=?
                            WHERE tag_id=? AND locale=?
                            """.formatted(TAGS_LOCALIZED_TABLE))
                    .param(INDEX_1, tag.getTitle())
                    .param(INDEX_2, locale.getLanguage())
                    .param(INDEX_3, id)
                    .param(INDEX_4, locale.getLanguage())
                    .update();
            if (localizedTagUpdatedRows == 0) {
                jdbcClient
                        .sql("""
                                INSERT INTO %s(tag_id, locale, title)
                                VALUES (?, ?, ?)
                                """.formatted(TAGS_LOCALIZED_TABLE))
                        .param(INDEX_1, id)
                        .param(INDEX_2, locale.getLanguage())
                        .param(INDEX_3, tag.getTitle())
                        .update();
            }
        }

        return read(userName, id, locale).get();
    }

    /**
     * Delete the tag.
     *
     * @param userName the user name
     * @param id       the id
     * @return true if the tag was deleted, false otherwise
     */
    public Boolean delete(final String userName, final String id) {
        final String deleteTagQuery =
                "DELETE FROM %s WHERE ID=?".formatted(TAGS_TABLE);

        final int updatedRows = jdbcClient
                .sql(deleteTagQuery)
                .param(INDEX_1, id)
                .update();

        return updatedRows != 0;
    }

    /**
     * List of tags.
     *
     * @param userName the user name
     * @param locale   the locale
     * @return the list of tags
     */
    public List<Tags> list(final String userName, final Locale locale) {
        final String query = locale == null
                ? """
                SELECT id, title, created_at, created_by, modified_at,
                modified_by FROM %s
                """.formatted(TAGS_TABLE)
                : """
                SELECT DISTINCT b.ID,
                CASE WHEN bl.LOCALE = ?
                THEN bl.TITLE
                ELSE b.TITLE
                END AS TITLE,
                created_at, created_by, modified_at, modified_by
                FROM %s b
                LEFT JOIN %s bl ON b.ID = bl.tag_id
                WHERE bl.LOCALE IS NULL
                OR bl.LOCALE = ?
                OR b.ID NOT IN (
                SELECT tag_id FROM %s
                WHERE tag_id=b.ID AND LOCALE = ?)
                """.formatted(TAGS_TABLE, TAGS_LOCALIZED_TABLE,
                TAGS_LOCALIZED_TABLE);

        return locale == null ? jdbcClient
                .sql(query)
                .query(this::rowMapper)
                .list()
                : jdbcClient
                .sql(query)
                .param(INDEX_1, locale.getLanguage())
                .param(INDEX_2, locale.getLanguage())
                .param(INDEX_3, locale.getLanguage())
                .query(this::rowMapper)
                .list();
    }

    /**
     * Cleaning up all tags.
     */
    public void deleteAll() {
        jdbcClient.sql("DELETE FROM %s".formatted(TAGS_LOCALIZED_TABLE))
                .update();
        final String deleteTagsQuery = "DELETE FROM %s".formatted(TAGS_TABLE);
        jdbcClient.sql(deleteTagsQuery).update();
    }
}
