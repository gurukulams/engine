package com.techatpark.workout.service;


import com.techatpark.workout.model.Category;
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
 * The type Category service.
 */
@Service
public final class CategoryService {

    /**
     * Logger Facade.
     */
    private final Logger logger =
            LoggerFactory.getLogger(CategoryService.class);

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
    public CategoryService(
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
    private Category rowMapper(final ResultSet rs,
                               final Integer rowNum)
            throws SQLException {
        Category category = new Category(
                rs.getString("id"),
                rs.getString("title"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getString("created_by"),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getString("modified_by"));
        return category;
    }

    /**
     * inserts data.
     *
     * @param userName the userName
     * @param locale
     * @param tag      the tag
     * @return question optional
     */
    public Category create(final String userName,
                           final Locale locale,
                           final Category tag) {

        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("categories")
                .usingColumns("id", "title",
                        "created_by");

        final Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("id", tag.id());
        valueMap.put("title",
                tag.title());
        valueMap.put("created_by", userName);

        insert.execute(valueMap);

        if (locale != null) {
            valueMap.put("category_id", tag.id());
            valueMap.put("locale", locale.getLanguage());
            createLocalizedTag(valueMap);
        }

        final Optional<Category> optionalCategory =
                read(userName, tag.id(), locale);

        logger.info("Created Category {}", tag.id());

        return optionalCategory.get();
    }

    /**
     * Create Localized Category.
     *
     * @param valueMap
     * @return noOfCategories
     */
    private int createLocalizedTag(final Map<String, Object> valueMap) {
        return new SimpleJdbcInsert(dataSource)
                .withTableName("categories_localized")
                .usingColumns("category_id", "locale", "title")
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
    public Optional<Category> read(final String userName,
                                   final String id,
                                   final Locale locale) {
        final String query = locale == null
                ? "SELECT id,title,created_by,"
                + "created_at, modified_at, modified_by FROM categories "
                + "WHERE id = ?"
                : "SELECT DISTINCT b.ID, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.TITLE "
                + "ELSE b.TITLE "
                + "END AS TITLE, "
                + "created_by,created_at, modified_at, modified_by "
                + "FROM CATEGORIES b "
                + "LEFT JOIN CATEGORIES_LOCALIZED bl "
                + "ON b.ID = bl.category_id "
                + "WHERE b.ID = ? "
                + "AND (bl.LOCALE IS NULL "
                + "OR bl.LOCALE = ? OR "
                + "b.ID NOT IN "
                + "(SELECT category_id FROM CATEGORIES_LOCALIZED "
                + "WHERE category_id=b.ID AND LOCALE = ?))";

        try {
            final Category p = locale == null ? jdbcTemplate
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
    public Category update(final String id,
                           final String userName,
                           final Locale locale,
                           final Category tag) {
        logger.debug("Entering update for Category {}", id);
        final String query = locale == null
                ? "UPDATE categories SET title=?,"
                + "modified_by=? WHERE id=?"
                : "UPDATE categories SET modified_by=? WHERE id=?";
        Integer updatedRows = locale == null
                ? jdbcTemplate.update(query, tag.title(),
                userName, id)
                : jdbcTemplate.update(query, userName, id);
        if (updatedRows == 0) {
            logger.error("Update not found", id);
            throw new IllegalArgumentException("Category not found");
        } else if (locale != null) {
            updatedRows = jdbcTemplate.update(
                    "UPDATE categories_localized SET title=?,locale=?"
                            + " WHERE category_id=? AND locale=?",
                    tag.title(), locale.getLanguage(),
                    id, locale.getLanguage());
            if (updatedRows == 0) {
                final Map<String, Object> valueMap = new HashMap<>(4);
                valueMap.put("category_id", id);
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
        String query = "DELETE FROM categories WHERE ID=?";

        final Integer updatedRows = jdbcTemplate.update(query, id);
        return !(updatedRows == 0);
    }


    /**
     * list of categories.
     *
     * @param userName the userName
     * @param locale
     * @return categories list
     */
    public List<Category> list(final String userName,
                               final Locale locale) {
        final String query = locale == null
                ? "SELECT id,title,created_by,"
                + "created_at, modified_at, modified_by FROM categories"
                : "SELECT DISTINCT b.ID, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.TITLE "
                + "ELSE b.TITLE "
                + "END AS TITLE, "
                + "created_by,created_at, modified_at, modified_by "
                + "FROM CATEGORIES b "
                + "LEFT JOIN CATEGORIES_LOCALIZED bl "
                + "ON b.ID = bl.category_id "
                + "WHERE bl.LOCALE IS NULL "
                + "OR bl.LOCALE = ? OR "
                + "b.ID NOT IN "
                + "(SELECT category_id FROM CATEGORIES_LOCALIZED "
                + "WHERE category_id=b.ID AND LOCALE = ?)";
        return locale == null
                ? jdbcTemplate.query(query, this::rowMapper)
                : jdbcTemplate
                .query(query, this::rowMapper,
                        locale.getLanguage(),
                        locale.getLanguage(),
                        locale.getLanguage());
    }

    /**
     * Cleaning up all categories.
     *
     * @return no.of categories deleted
     */
    public Integer deleteAll() {
        jdbcTemplate.update("DELETE FROM categories_localized");
        final String query = "DELETE FROM categories";
        return jdbcTemplate.update(query);
    }
}
