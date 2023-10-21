package com.techatpark.workout.service;

import com.gurukulams.core.GurukulamsManager;
import com.gurukulams.core.model.Category;
import com.gurukulams.core.model.CategoryLocalized;
import com.gurukulams.core.store.CategoryLocalizedStore;
import com.gurukulams.core.store.CategoryStore;
import com.gurukulams.core.store.QuestionCategoryStore;
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
 * The type Category service.
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
     * categoryStore.
     */
    private final CategoryStore categoryStore;

    /**
     * categoryStore.
     */
    private final CategoryLocalizedStore categoryLocalizedStore;

    /**
     * questionCategoryStore.
     */
    private final QuestionCategoryStore questionCategoryStore;

    /**
     * JdbcClient.
     */
    private final JdbcClient jdbcClient;


    /**
     * Instantiates a new Category service.
     *
     * @param aJdbcClient               the jdbc client
     * @param gurukulamsManager
     */
    public CategoryService(final JdbcClient aJdbcClient,
                           final GurukulamsManager gurukulamsManager) {
        this.categoryStore = gurukulamsManager.getCategoryStore();
        this.categoryLocalizedStore
                = gurukulamsManager.getCategoryLocalizedStore();
        this.questionCategoryStore
                = gurukulamsManager.getQuestionCategoryStore();
        this.jdbcClient = aJdbcClient;
    }

    private Category rowMapper(final ResultSet rs, final Integer rowNum)
            throws SQLException {
        Category category = new Category();
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
    public Category create(final String userName,
                           final Locale locale,
                           final Category category)
            throws SQLException {
        category.setCreatedBy(userName);
        this.categoryStore.insert().values(category).execute();

        if (locale != null) {
            create(category.getId(), category, locale);
        }

        return read(userName, category.getId(), locale).get();
    }

    private int create(final String categoryId,
                       final Category category,
                       final Locale locale) throws SQLException {

        CategoryLocalized categoryLocalized = new CategoryLocalized();
        categoryLocalized.setCategoryId(categoryId);
        categoryLocalized.setLocale(locale.getLanguage());
        categoryLocalized.setTitle(category.getTitle());
        return this.categoryLocalizedStore.insert()
                .values(categoryLocalized)
                .execute();
    }

    /**
     * Read optional.
     *
     * @param userName the user name
     * @param id       the id
     * @param locale   the locale
     * @return the optional
     */
    public Optional<Category> read(final String userName,
                                     final String id,
                                     final Locale locale)
            throws SQLException {

        if (locale == null) {
            return this.categoryStore.select(id);
        }

        final String selectCategoryQuery =
                """
                        SELECT DISTINCT c.id,
                            CASE WHEN cl.LOCALE = ?
                                THEN cl.TITLE
                                ELSE c.TITLE
                            END AS TITLE,
                            created_at, created_by, modified_at, modified_by
                        FROM category c
                        LEFT JOIN category_localized cl ON c.ID = cl.CATEGORY_ID
                        WHERE c.ID = ?
                            AND (cl.LOCALE IS NULL
                            OR cl.LOCALE = ?
                            OR c.ID NOT IN (
                                SELECT CATEGORY_ID
                                FROM category_localized
                                WHERE CATEGORY_ID = c.ID
                                    AND LOCALE = ?
                            ))
                        """;


            return jdbcClient.sql(selectCategoryQuery)
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
    public Category update(final String id,
                           final String userName,
                           final Locale locale,
                           final Category category) throws SQLException {
        logger.debug("Entering update for Category {}", id);

        int updatedRows = 0;

        if (locale == null) {
            updatedRows = this.categoryStore.update()
                    .set(CategoryStore.title(category.getTitle()),
                            CategoryStore.modifiedBy(userName))
                    .where(CategoryStore.id().eq(id)).execute();
        } else {
            updatedRows = this.categoryStore.update()
                    .set(CategoryStore.modifiedBy(userName))
                    .where(CategoryStore.id().eq(id)).execute();
            if (updatedRows != 0) {
                updatedRows = this.categoryLocalizedStore.update().set(
                        CategoryLocalizedStore.title(category.getTitle()),
                        CategoryLocalizedStore.locale(locale.getLanguage()))
                        .where(CategoryLocalizedStore.categoryId().eq(id)
                        .and().locale().eq(locale.getLanguage())).execute();

                if (updatedRows == 0) {
                    updatedRows = create(id, category, locale);
                }
            }
        }


        if (updatedRows == 0) {
            logger.error("Update not found", id);
            throw new IllegalArgumentException("Category not found");
        }

        return read(userName, id, locale).get();
    }



    /**
     * List list.
     *
     * @param userName the user name
     * @param locale   the locale
     * @return the list
     */
    public List<Category> list(final String userName,
                                 final Locale locale) throws SQLException {
        if (locale == null) {
            return this.categoryStore.select().execute();
        }
        final String listCategoryQuery =
                """
                        SELECT DISTINCT c.id,
                            CASE WHEN cl.LOCALE = ?
                                THEN cl.TITLE
                                ELSE c.TITLE
                            END AS TITLE,
                            created_at, created_by, modified_at, modified_by
                        FROM category c
                        LEFT JOIN category_localized cl ON c.ID = cl.CATEGORY_ID
                        WHERE cl.LOCALE IS NULL
                            OR cl.LOCALE = ?
                        """;

        return jdbcClient.sql(listCategoryQuery)
                        .param(INDEX_1, locale.getLanguage())
                        .param(INDEX_2, locale.getLanguage())
                        .query(this::rowMapper)
                        .list();
    }


    /**
     * Delete boolean.
     *
     * @param userName the user name
     * @param id       the id
     * @return the boolean
     */
    public boolean delete(final String userName, final String id)
            throws SQLException {
        this.questionCategoryStore
                .delete(QuestionCategoryStore.categoryId().eq(id))
                .execute();
        this.categoryLocalizedStore
                .delete(CategoryLocalizedStore.categoryId().eq(id))
                .execute();
        return this.categoryStore.delete(id) == 1;
    }


    /**
     * Cleaning up all category.
     */
    public void delete() throws SQLException {
        this.questionCategoryStore.delete().execute();
        this.categoryLocalizedStore.delete().execute();
        this.categoryStore.delete().execute();
    }
}
