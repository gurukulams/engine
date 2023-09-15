package com.techatpark.workout.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techatpark.workout.model.Annotation;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The type User Annotation service.
 */
@Service
public class AnnotationService {
    /**
     * this helps to execute sql queries.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * this creates connection functionalities.
     */
    private final DataSource dataSource;

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
        annotation.setId((UUID) rs.getObject("id"));
        final TypeReference<HashMap<String, Object>> typeRef
                = new TypeReference<>() {
        };
        try {
            String unwrappedJSON = objectMapper
                    .readValue(rs.getString("json_value"), String.class);
            annotation.setValue(objectMapper.readValue(unwrappedJSON,
                    typeRef));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return annotation;
    };

    /**
     * initializes.
     *  @param aJdbcTemplate the a jdbc template
     * @param aDataSource   the a data source
     * @param anObjectMapper
     */
    public AnnotationService(final JdbcTemplate aJdbcTemplate,
                             final DataSource aDataSource,
                             final ObjectMapper anObjectMapper) {
        this.jdbcTemplate = aJdbcTemplate;
        this.dataSource = aDataSource;
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
        final SimpleJdbcInsert insert =
                new SimpleJdbcInsert(dataSource).withTableName("annotations")
                        .usingColumns("id", "created_by",
                                "on_type", "on_instance",
                                "locale", "json_value");

        final Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("created_by", userName);
        valueMap.put("on_type", onType);
        valueMap.put("on_instance", onInstance);
        valueMap.put("json_value", objectMapper
                .writeValueAsString(annotation.getValue()));
        if (locale == null) {
            valueMap.put("locale", null);
        } else {
            valueMap.put("locale", locale.getLanguage());
        }
        final UUID id = UUID.randomUUID();
        valueMap.put("id", id);
        insert.execute(valueMap);
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
            return Optional.of(jdbcTemplate
                    .queryForObject(query, this::rowMapper, params));

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
                ? jdbcTemplate.query(query, this::rowMapper, onType, onInstance,
                userName)
                : jdbcTemplate.query(query, this::rowMapper, onType, onInstance,
                userName, locale.getLanguage());
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
        final Integer updatedRows = (locale == null)
                ?
                jdbcTemplate.update(query,
                        objectMapper.writeValueAsString(annotation.getValue()),
                        id)
                : jdbcTemplate.update(query,
                objectMapper.writeValueAsString(annotation.getValue()),
                id, locale.getLanguage());
        return updatedRows == 0 ? null : read(id, locale);
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
        final String query = "DELETE FROM annotations WHERE ID=?";
        final Integer updatedRows = jdbcTemplate.update(query, id);
        return !(updatedRows == 0);
    }
}
