package com.techatpark.workout.service;

import com.gurukulams.core.model.Categories;
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
 * The type Categories service.
 */
@Service
public class CategoryService {

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
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(
            CategoryService.class);
    /**
     * JdbcClient.
     */
    private final JdbcClient jdbcClient;
    /**
     * categories table.
     */
    private static final String CATEGORIES_TABLE = "categories";
    /**
     * categories_localized table.
     */
    private static final String CATEGORIES_LOCALIZED_TABLE =
            "categories_localized";

    /**
     * Instantiates a new Categories service.
     *
     * @param aJdbcClient the jdbc client
     */
    public CategoryService(final JdbcClient aJdbcClient) {
        this.jdbcClient = aJdbcClient;
    }

    private Categories rowMapper(final ResultSet rs, final Integer rowNum)
            throws SQLException {
        Categories category = new Categories();
        category.setId(rs.getString(INDEX_1));
        category.setTitle(rs.getString(INDEX_2));
        category.setCreatedAt(rs.getObject(INDEX_3, LocalDateTime.class));
        category.setCreatedBy(rs.getString(INDEX_4));
        category.setModifiedAt(rs.getObject(INDEX_5, LocalDateTime.class));
        category.setModifiedBy(rs.getString(INDEX_6));
        return category;
    }

    /**
     * Create category.
     *
     * @param userName the user name
     * @param locale   the locale
     * @param category the category
     * @return the category
     */
    public Categories create(final String userName,
                           final Locale locale,
                           final Categories category) {
        final String insertCategoriesQuery = """
                INSERT INTO %s(id, title, created_by)
                VALUES (?, ?, ?)
                """.formatted(CATEGORIES_TABLE);


        jdbcClient.sql(insertCategoriesQuery)
                .param(INDEX_1, category.getId())
                .param(INDEX_2, category.getTitle())
                .param(INDEX_3, userName)
                .update();

        if (locale != null) {
            createLocalizedCategories(category.getId(), category, locale);
        }

        return read(userName, category.getId(), locale).get();
    }

    private int createLocalizedCategories(final String categoryId,
                                        final Categories category,
                                        final Locale locale) {
        final String insertLocalizedCategoriesQuery = """
                INSERT INTO %s(category_id, locale, title)
                VALUES (?, ?, ?)
                """.formatted(CATEGORIES_LOCALIZED_TABLE);

        return jdbcClient.sql(insertLocalizedCategoriesQuery)
                .param(INDEX_1, categoryId)
                .param(INDEX_2, locale.getLanguage())
                .param(INDEX_3, category.getTitle())
                .update();
    }

    /**
     * Read optional.
     *
     * @param userName the user name
     * @param id       the id
     * @param locale   the locale
     * @return the optional
     */
    public Optional<Categories> read(final String userName,
                                     final String id,
                                     final Locale locale) {
        final String selectCategoriesQuery = locale == null
                ?
                """
                        SELECT id, title, created_at, created_by,
                        modified_at, modified_by
                        FROM %s
                        WHERE id = ?
                        """.formatted(CATEGORIES_TABLE)
                :
                """
                        SELECT DISTINCT c.id,
                            CASE WHEN cl.LOCALE = ?
                                THEN cl.TITLE
                                ELSE c.TITLE
                            END AS TITLE,
                            created_at, created_by, modified_at, modified_by
                        FROM %s c
                        LEFT JOIN %s cl ON c.ID = cl.CATEGORY_ID
                        WHERE c.ID = ?
                            AND (cl.LOCALE IS NULL
                            OR cl.LOCALE = ?
                            OR c.ID NOT IN (
                                SELECT CATEGORY_ID
                                FROM %s
                                WHERE CATEGORY_ID = c.ID
                                    AND LOCALE = ?
                            ))
                        """.formatted(CATEGORIES_TABLE,
                        CATEGORIES_LOCALIZED_TABLE,
                        CATEGORIES_LOCALIZED_TABLE);


            return locale == null
                    ?
                    jdbcClient.sql(selectCategoriesQuery)
                            .param(INDEX_1, id)
                            .query(this::rowMapper)
                            .optional()
                    :
                    jdbcClient.sql(selectCategoriesQuery)
                            .param(INDEX_1, locale.getLanguage())
                            .param(INDEX_2, id)
                            .param(INDEX_3, locale.getLanguage())
                            .param(INDEX_4, locale.getLanguage())
                            .query(this::rowMapper)
                            .optional();

    }

    /**
     * Update category.
     *
     * @param id       the id
     * @param userName the user name
     * @param locale   the locale
     * @param category the category
     * @return the category
     */
    public Categories update(final String id,
                           final String userName,
                           final Locale locale,
                           final Categories category) {
        logger.debug("Entering update for Categories {}", id);
        final String updateCategoriesQuery = locale == null
                ?
                """
                        UPDATE %s
                        SET title = ?, modified_by = ?
                        WHERE id = ?
                        """.formatted(CATEGORIES_TABLE)
                :
                "UPDATE %s SET modified_by = ? WHERE id = ?"
                        .formatted(CATEGORIES_TABLE);

        Integer updatedRows = locale == null
                ?
                jdbcClient.sql(updateCategoriesQuery)
                        .param(INDEX_1, category.getTitle())
                        .param(INDEX_2, userName)
                        .param(INDEX_3, id)
                        .update()
                :
                jdbcClient.sql(updateCategoriesQuery)
                        .param(INDEX_1, userName)
                        .param(INDEX_2, id)
                        .update();

        if (updatedRows == 0) {
            logger.error("Update not found", id);
            throw new IllegalArgumentException("Categories not found");
        } else if (locale != null) {
            updatedRows = jdbcClient.sql("""
                            UPDATE %s
                            SET title = ?, locale = ?
                            WHERE category_id = ?
                            AND locale = ?
                            """.formatted(CATEGORIES_LOCALIZED_TABLE))
                    .param(INDEX_1, category.getTitle())
                    .param(INDEX_2, locale.getLanguage())
                    .param(INDEX_3, id)
                    .param(INDEX_4, locale.getLanguage())
                    .update();
            if (updatedRows == 0) {
                createLocalizedCategories(id, category, locale);
            }
        }

        return read(userName, id, locale).get();
    }

    /**
     * Delete boolean.
     *
     * @param userName the user name
     * @param id       the id
     * @return the boolean
     */
    public Boolean delete(final String userName, final String id) {
        final String deleteCategoriesQuery =
                "DELETE FROM %s WHERE id = ?".formatted(CATEGORIES_TABLE);
        return jdbcClient.sql(deleteCategoriesQuery)
                .param(INDEX_1, id)
                .update() != 0;
    }

    /**
     * List list.
     *
     * @param userName the user name
     * @param locale   the locale
     * @return the list
     */
    public List<Categories> list(final String userName,
                                 final Locale locale) {
        final String listCategoriesQuery = locale == null
                ?
                """
                        SELECT id, title, created_at, created_by,
                        modified_at, modified_by FROM %s
                        """.formatted(CATEGORIES_TABLE)
                :
                """
                        SELECT DISTINCT c.id,
                            CASE WHEN cl.LOCALE = ?
                                THEN cl.TITLE
                                ELSE c.TITLE
                            END AS TITLE,
                            created_at, created_by, modified_at, modified_by
                        FROM %s c
                        LEFT JOIN %s cl ON c.ID = cl.CATEGORY_ID
                        WHERE cl.LOCALE IS NULL
                            OR cl.LOCALE = ?
                        """.formatted(CATEGORIES_TABLE,
                        CATEGORIES_LOCALIZED_TABLE);

        return locale == null
                ?
                jdbcClient.sql(listCategoriesQuery)
                        .query(this::rowMapper)
                        .list()
                :
                jdbcClient.sql(listCategoriesQuery)
                        .param(INDEX_1, locale.getLanguage())
                        .param(INDEX_2, locale.getLanguage())
                        .query(this::rowMapper)
                        .list();
    }

    /**
     * Cleaning up all categories.
     */
    public void deleteAll() {
        jdbcClient.sql("DELETE FROM questions_categories").update();
        jdbcClient.sql("DELETE FROM categories_localized").update();
        jdbcClient.sql("DELETE FROM categories").update();
    }
}
