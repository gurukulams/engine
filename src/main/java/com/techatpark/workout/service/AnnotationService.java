package com.techatpark.workout.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurukulams.core.model.Annotation;
import org.json.JSONObject;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * The type User Annotation service.
 */
@Service
public class AnnotationService {

    /**
     * Index Number.
     */
    private static final int INDEX_1 = 1;
    /**
     * Index Number.
     */
    private static final int INDEX_2 = 2;

    /**
     * Index Number.
     */
    private static final int INDEX_3 = 3;
    /**
     * Index Number.
     */
    private static final int INDEX_4 = 4;
    /**
     * Index Number.
     */
    private static final int INDEX_5 = 5;
    /**
     * Index Number.
     */
    private static final int INDEX_6 = 6;

    /**
     * this helps to execute sql queries.
     */
    private final JdbcClient jdbcClient;

    /**
     * this is ObjectMapper of Spring.
     */
    private final ObjectMapper objectMapper;

    /**
     * initializes.
     *
     * @param aJdbcClient
     * @param anObjectMapper
     */
    public AnnotationService(final JdbcClient aJdbcClient,
                             final ObjectMapper anObjectMapper) {
        this.jdbcClient = aJdbcClient;
        this.objectMapper = anObjectMapper;
    }

    /**
     * Maps the data from and to the database. return question.
     *
     * @param rowNum
     * @param rs
     * @return annotation
     */
    private Annotation rowMapper(final ResultSet rs,
                                  final int rowNum) throws SQLException {
        final Annotation annotation = new Annotation();
        annotation.setId((UUID) rs.getObject(INDEX_1));
        String jsonString = rs.getString(INDEX_2);
        annotation.setJsonValue(new JSONObject(jsonString.substring(1,
                jsonString.length() - 1).replace("\\", "")));
        return annotation;
    }

    /**
     * Create optional.
     *
     * @param userName   user name
     * @param annotation the  annotation
     * @param onType
     * @param onInstance
     * @param locale     tha language
     * @return the optional
     */
    public final Annotation create(final String onType,
                                              final String onInstance,
                                              final Annotation annotation,
                                              final Locale locale,
                                              final String userName) {
        final UUID id = UUID.randomUUID();

        String sql = "INSERT INTO annotation(id, created_by, on_type, "
                + "on_instance, locale, json_value) values(?,?,?,?,?,?)";
        jdbcClient.sql(sql)
                .param(INDEX_1, id)
                .param(INDEX_2, userName)
                .param(INDEX_3, onType)
                .param(INDEX_4, onInstance)
                .param(INDEX_5, locale == null ? null : locale.getLanguage())
                .param(INDEX_6, annotation.getJsonValue().toString()).update();

        return read(id, locale).get();
    }


    /**
     * Read optional.
     *
     * @param id     the id
     * @param locale tha language
     * @return the optional
     */
    public final Optional<Annotation> read(final UUID id,
                                            final Locale locale) {
        final String query = "SELECT id,json_value"
                + " FROM "
                + "annotation WHERE"
                + " id = ? AND "
                + ((locale == null)
                ? "locale IS NULL" : "locale = ?");

        Object[] params;
        if (locale == null) {
            params = new Object[]{id};
        } else {
            params = new Object[]{id, locale.getLanguage()};
        }
        return jdbcClient.sql(query)
                .params(List.of(params))
                .query(this::rowMapper).optional();


    }

    /**
     * List list.
     *
     * @param userName   user name
     * @param onInstance the on instance
     * @param onType
     * @param locale     tha language
     * @return the list
     */
    public final List<Annotation> list(final String userName,
                                        final Locale locale,
                                        final String onType,
                                        final String onInstance) {
        final String query = "SELECT id,"
                + "json_value FROM "
                + "annotation WHERE"
                + " on_type = ? and on_instance = ? and created_by = ? AND "
                + ((locale == null) ? "locale IS NULL" : "locale = ?");
        return (locale == null) ? jdbcClient
                .sql(query)
                .param(INDEX_1, onType)
                .param(INDEX_2, onInstance)
                .param(INDEX_3, userName)
                .query(this::rowMapper).list()

                : jdbcClient.sql(query)
                .param(INDEX_1, onType)
                .param(INDEX_2, onInstance)
                .param(INDEX_3, userName)
                .param(INDEX_4, locale.getLanguage())
                .query(this::rowMapper).list();
    }

    /**
     * Update Annotation optional.
     *
     * @param id         the id
     * @param annotation the user Annotation
     * @param locale     tha language
     * @return the optional
     */
    public final Optional<Annotation> update(final UUID id,
                                              final Locale locale,
                                              final Annotation annotation) {
        final String query = "UPDATE annotation SET "
                + "json_value = ? WHERE id = ? AND "
                + ((locale == null) ? "locale IS NULL" : "locale = ?");
        final int updatedRows = (locale == null) ? jdbcClient
                .sql(query)
                .param(INDEX_1, annotation.getJsonValue().toString())
                .param(INDEX_2, id).update()
                : jdbcClient.sql(query)
                .param(INDEX_1, annotation.getJsonValue().toString())
                .param(INDEX_2, id).param(INDEX_3, locale.getLanguage())
                .update();
        if (updatedRows == 0) {
            throw new IllegalArgumentException("Annotation not found");
        }
        return read(id, locale);
    }

    /**
     * Delete boolean.
     *
     * @param id     the id
     * @param locale tha language
     * @return the boolean
     */
    public final boolean delete(final UUID id, final Locale locale) {
        if (locale == null) {
            return jdbcClient
                    .sql("DELETE FROM annotation WHERE ID=? AND locale IS "
                            + "NULL")
                    .param(INDEX_1, id)
                    .update() != 0;
        }
        return jdbcClient.sql("DELETE FROM annotation WHERE ID=? AND locale=?")
                .param(INDEX_1, id)
                .param(INDEX_2, locale.getLanguage())
                .update() != 0;
    }

    /**
     * Deletes all Annotations.
     */
    public void delete() {
        jdbcClient.sql("DELETE FROM annotation").update();
    }


}
