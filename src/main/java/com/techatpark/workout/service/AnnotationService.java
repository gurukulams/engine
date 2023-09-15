package com.techatpark.workout.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techatpark.workout.model.Annotation;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
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
     * Maps the data from and to the database. return question.
     * @param rowNum
     * @param rs
     * @return annotation
     */
    private Annotation rowMapper(final ResultSet rs,
                                 final int rowNum)
            throws SQLException {
        final Annotation annotation = new Annotation();
        annotation.setId((UUID) rs.getObject(INDEX_1));
        final TypeReference<HashMap<String, Object>> typeRef
                = new TypeReference<>() {
        };
        try {
            String unwrappedJSON = objectMapper
                    .readValue(rs.getString(INDEX_2), String.class);
            annotation.setValue(objectMapper.readValue(unwrappedJSON,
                    typeRef));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return annotation;
    };

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
     * Create optional.
     *
     * @param userName user name
     * @param annotation the  annotation
     * @param onType
     * @param onInstance
     * @param locale tha language
     * @return the optional
     */
    public final Optional<Annotation> create(
            final String onType,
            final String onInstance,
            final Annotation annotation,
                                     final Locale locale,
                                     final String userName)
            throws JsonProcessingException {
        final UUID id = UUID.randomUUID();

        String sql =
            "INSERT INTO annotations(id, created_by, on_type, "
                + "on_instance, locale, json_value) values(?,?,?,?,?,?)";
        jdbcClient.sql(sql)
                .param(INDEX_1, id)
                .param(INDEX_2, userName)
                .param(INDEX_3, onType)
                .param(INDEX_4, onInstance)
                .param(INDEX_5, locale == null ? null : locale.getLanguage())
                .param(INDEX_6, objectMapper
                        .writeValueAsString(annotation.getValue()))
                .update();

        return read(id, locale);
    }

    /**
     * Read optional.
     *
     * @param id the id
     * @param locale tha language
     * @return the optional
     */
    public final Optional<Annotation> read(final UUID id,
                                   final Locale locale) {
        final String query =
                "SELECT id,json_value"
                        + " FROM "
                        + "annotations WHERE"
                        + " id = ? AND "
                        + ((locale == null)
                        ? "locale IS NULL" : "locale = ?");
        try {
            Object[] params;
            if (locale == null) {
                params = new Object[]{id};
            } else {
                params = new Object[]{id, locale.getLanguage()};
            }
            return Optional.of(jdbcClient
                    .sql(query)
                            .params(List.of(params))
                    .query(this::rowMapper).single());

        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * List list.
     *
     * @param userName   user name
     * @param onInstance the on instance
     * @param onType
     * @param locale tha language
     * @return the list
     */
    public final List<Annotation> list(final String userName,
                                 final Locale locale,
                                 final String onType,
                                 final String onInstance) {
        final String query = "SELECT id,"
                + "json_value FROM "
                + "annotations WHERE"
                + " on_type = ? and on_instance = ? and created_by = ? AND "
                + ((locale == null)
                ? "locale IS NULL" : "locale = ?");
        return (locale == null)
                ? jdbcClient.sql(query)
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
     * @param id       the id
     * @param annotation the user Annotation
     * @param locale tha language
     * @return the optional
     */
    public final Optional<Annotation> update(final UUID id,
                                       final Locale locale,
                                       final Annotation annotation)
            throws JsonProcessingException {
        final String query =
                "UPDATE annotations SET "
                        + "json_value = ? WHERE id = ? AND "
                        + ((locale == null) ? "locale IS NULL" : "locale = ?");
        final int updatedRows = (locale == null)
                ?
                jdbcClient.sql(query)
                    .param(INDEX_1,
                        objectMapper.writeValueAsString(annotation.getValue()))
                    .param(INDEX_2, id).update()
                :
                jdbcClient.sql(query)
                .param(INDEX_1,
                        objectMapper.writeValueAsString(annotation.getValue()))
                .param(INDEX_2, id)
                .param(INDEX_3, locale.getLanguage())
                .update();
        if (updatedRows == 0) {
            throw new IllegalArgumentException("Annotation not found");
        }
        return read(id, locale);
    }

    /**
     * Delete boolean.
     *
     * @param id the id
     * @return the boolean
     * @param locale tha language
     */
    public final boolean delete(final UUID id,
                          final Locale locale) {
        return jdbcClient.sql("DELETE FROM annotations WHERE ID=?")
                .param(INDEX_1, id).update() != 0;
    }

    /**
     * Deletes all Annotations.
     */
    public void deleteAll() {
        jdbcClient.sql("DELETE FROM annotations").update();
    }
}
