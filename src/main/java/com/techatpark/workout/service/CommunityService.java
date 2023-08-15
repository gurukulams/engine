package com.techatpark.workout.service;


import com.techatpark.workout.model.Community;
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
 * The type community service.
 */
@Service
public final class CommunityService {

    /**
     * Logger Facade.
     */
    private final Logger logger =
            LoggerFactory.getLogger(CommunityService.class);

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
    public CommunityService(
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
    private Community rowMapper(final ResultSet rs,
                               final Integer rowNum)
            throws SQLException {
        Community community = new Community(
                rs.getString("id"),
                rs.getString("title"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getString("created_by"),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getString("modified_by"));
        return community;
    }

    /**
     * inserts data.
     *
     * @param userName the userName
     * @param locale
     * @param community      the community
     * @return question optional
     */
    public Community create(final String userName,
                           final Locale locale,
                           final Community community) {

        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("communities")
                .usingColumns("id", "title",
                        "created_by");

        final Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("id", community.id());
        valueMap.put("title",
            community.title());
        valueMap.put("created_by", userName);

        insert.execute(valueMap);

        if (locale != null) {
            valueMap.put("community_id", community.id());
            valueMap.put("locale", locale.getLanguage());
            createLocalizedCommunity(valueMap);
        }

        final Optional<Community> optionalcommunity =
                read(userName, community.id(), locale);

        logger.info("Created community {}", community.id());

        return optionalcommunity.get();
    }

    /**
     * Create Localized community.
     *
     * @param valueMap
     * @return noOfcommunities
     */
    private int createLocalizedCommunity(final Map<String, Object> valueMap) {
        return new SimpleJdbcInsert(dataSource)
                .withTableName("communities_localized")
                .usingColumns("community_id", "locale", "title")
                .execute(valueMap);
    }

    /**
     * reads from community.
     *
     * @param id       the id
     * @param userName the userName
     * @param locale
     * @return question optional
     */
    public Optional<Community> read(final String userName,
                                   final String id,
                                   final Locale locale) {
        final String query = locale == null
                ? "SELECT id,title,created_by,"
                + "created_at, modified_at, modified_by FROM communities "
                + "WHERE id = ?"
                : "SELECT DISTINCT b.ID, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.TITLE "
                + "ELSE b.TITLE "
                + "END AS TITLE, "
                + "created_by,created_at, modified_at, modified_by "
                + "FROM communities b "
                + "LEFT JOIN communities_localized bl "
                + "ON b.ID = bl.community_id "
                + "WHERE b.ID = ? "
                + "AND (bl.LOCALE IS NULL "
                + "OR bl.LOCALE = ? OR "
                + "b.ID NOT IN "
                + "(SELECT community_id FROM communities_localized "
                + "WHERE community_id=b.ID AND LOCALE = ?))";

        try {
            final Community p = locale == null ? jdbcTemplate
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
     * update the community.
     *
     * @param id       the id
     * @param userName the userName
     * @param locale
     * @param community      the community
     * @return question optional
     */
    public Community update(final String id,
                           final String userName,
                           final Locale locale,
                           final Community community) {
        logger.debug("Entering update for community {}", id);
        final String query = locale == null
                ? "UPDATE communities SET title=?,"
                + "modified_by=? WHERE id=?"
                : "UPDATE communities SET modified_by=? WHERE id=?";
        Integer updatedRows = locale == null
                ? jdbcTemplate.update(query, community.title(),
                userName, id)
                : jdbcTemplate.update(query, userName, id);
        if (updatedRows == 0) {
            logger.error("Update not found", id);
            throw new IllegalArgumentException("community not found");
        } else if (locale != null) {
            updatedRows = jdbcTemplate.update(
                    "UPDATE communities_localized SET title=?,locale=?"
                            + " WHERE community_id=? AND locale=?",
                community.title(), locale.getLanguage(),
                    id, locale.getLanguage());
            if (updatedRows == 0) {
                final Map<String, Object> valueMap = new HashMap<>(4);
                valueMap.put("community_id", id);
                valueMap.put("locale", locale.getLanguage());
                valueMap.put("title", community.title());
                createLocalizedCommunity(valueMap);
            }
        }
        return read(userName, id, locale).get();
    }

    /**
     * delete the community.
     *
     * @param id       the id
     * @param userName the userName
     * @return false
     */
    public Boolean delete(final String userName, final String id) {
        String query = "DELETE FROM communities WHERE ID=?";

        final Integer updatedRows = jdbcTemplate.update(query, id);
        return !(updatedRows == 0);
    }


    /**
     * list of communities.
     *
     * @param userName the userName
     * @param locale
     * @return communities list
     */
    public List<Community> list(final String userName,
                               final Locale locale) {
        final String query = locale == null
                ? "SELECT id,title,created_by,"
                + "created_at, modified_at, modified_by FROM communities"
                : "SELECT DISTINCT b.ID, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.TITLE "
                + "ELSE b.TITLE "
                + "END AS TITLE, "
                + "created_by,created_at, modified_at, modified_by "
                + "FROM communities b "
                + "LEFT JOIN communities_localized bl "
                + "ON b.ID = bl.community_id "
                + "WHERE bl.LOCALE IS NULL "
                + "OR bl.LOCALE = ? OR "
                + "b.ID NOT IN "
                + "(SELECT community_id FROM communities_localized "
                + "WHERE community_id=b.ID AND LOCALE = ?)";
        return locale == null
                ? jdbcTemplate.query(query, this::rowMapper)
                : jdbcTemplate
                .query(query, this::rowMapper,
                        locale.getLanguage(),
                        locale.getLanguage(),
                        locale.getLanguage());
    }

    /**
     * Cleaning up all communities.
     *
     * @return no.of communities deleted
     */
    public Integer deleteAll() {
        jdbcTemplate.update("DELETE FROM communities_localized");
        final String query = "DELETE FROM communities";
        return jdbcTemplate.update(query);
    }
}
