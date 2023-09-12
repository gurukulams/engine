package com.techatpark.workout.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techatpark.workout.model.Practice;
import com.techatpark.workout.model.sql.SqlPractice;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The type Practice service.
 */
@Service
public class PracticeService {


    /**
     * this helps to execute sql queries.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * this is the connection for the database.
     */
    private final DataSource dataSource;
    /**
     * this is ApplicationContext of Spring.
     */
    private final ApplicationContext applicationContext;

    /**
     * this is ObjectMapper of Spring.
     */
    private final ObjectMapper objectMapper;

    /**
     * Validator.
     */
    private final Validator validator;


    /**
     * Instantiates a new Practice service.
     *
     * @param aJdbcTemplate        the a jdbc template
     * @param aDatasource          the a datasource
     * @param anApplicationContext the an application context
     * @param aObjectMapper        the a object mapper
     * @param aValidator           the validator
     */
    public PracticeService(
            final JdbcTemplate aJdbcTemplate,
            final DataSource aDatasource,
            final ApplicationContext anApplicationContext,
            final ObjectMapper aObjectMapper,
            final Validator aValidator) {
        this.jdbcTemplate = aJdbcTemplate;
        this.dataSource = aDatasource;
        this.applicationContext = anApplicationContext;
        this.objectMapper = aObjectMapper;
        this.validator = aValidator;
    }

    /**
     * Maps the data from and to the database.
     *
     * @param rs
     * @param rowNum
     * @param <T>
     * @return p
     * @throws SQLException
     */
    private <T extends Practice> T rowMapper(final ResultSet rs,
                                             final Integer rowNum)
            throws SQLException {
        final String metaData = rs.getString("meta_data");
        Practice practice;
        if (metaData == null) {
            practice = new Practice();
        } else {
            try {
                practice = new ObjectMapper().readValue(metaData,
                        getPracticeClass(rs.getString("type")));
            } catch (final JsonProcessingException e) {
                practice = new Practice();
            }
        }
        practice.setId((UUID) rs.getObject("id"));
        practice.setTitle(rs.getString("title"));
        practice.setCreatedBy(rs.getString("created_by"));
        practice.setDescription(rs.getString("description"));


        LocalDate calendarDate = rs.getDate("created_at")
                .toLocalDate();
        ZonedDateTime zdt = calendarDate.atStartOfDay(ZoneId
                .of("Europe/Paris"));


        practice.setCreatedAt(zdt.toInstant());
        Date sqlDate = rs.getDate("modified_at");

        if (sqlDate != null) {
            calendarDate = sqlDate.toLocalDate();
            zdt = calendarDate.atStartOfDay(ZoneId.of("Europe/Paris"));
            practice.setUpdatedAt(zdt.toInstant());
        }
        return (T) practice;
    }

    /**
     * Gets Class for Practice Type.
     *
     * @param type
     * @return clz.
     */
    private Class<? extends Practice> getPracticeClass(final String type) {
        switch (type) {
            case "sql":
                return SqlPractice.class;
            default:
                return Practice.class;
        }
    }

    private String getMetadata(final Practice practice)
            throws JsonProcessingException {
        if (!practice.getClass().equals(Practice.class)) {
            final ObjectNode oNode = objectMapper.valueToTree(practice);
            oNode.remove("id");
            oNode.remove("title");
            oNode.remove("description");
            return objectMapper.writeValueAsString(oNode);
        }
        return null;
    }

    /**
     * inserts data to database.
     *
     * @param <T>       the type parameter
     * @param type      the type
     * @param createdBy the createdBy
     * @param locale    the locale
     * @param practice  the practice
     * @return p. optional
     * @throws JsonProcessingException the json processing exception
     */
    public <T extends Practice> Optional<T> create(final String type,
                                                   final String createdBy,
                                                   final Locale locale,
                                                   final T practice)
            throws JsonProcessingException {

        Set<ConstraintViolation<Practice>> violations = validator
                .validate(practice);
        if (violations.isEmpty()) {
            final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                    .withTableName("practices")
                    .usingColumns("id", "title",
                            "type",
                            "created_by",
                            "description",
                            "meta_data");
            final String metaData = getMetadata(practice);
            final Map<String, Object> valueMap = new HashMap<>(6);
            valueMap.put("title",
                    practice.getTitle());
            valueMap.put("type", type);
            valueMap.put("created_by", createdBy);
            valueMap.put("description", practice.getDescription());
            valueMap.put("meta_data", metaData);
            final UUID examId = UUID.randomUUID();
            valueMap.put("id", examId);
            insert.execute(valueMap);

            final Optional<T> createdExam = read(examId, locale);
            createdExam.ifPresent(exam1 -> {
                if (locale != null) {
                    valueMap.put("practice_id", exam1.getId());
                    valueMap.put("locale", locale.getLanguage());
                    createLocalizedBoard(valueMap);
                }

            });
            return createdExam;
        } else {
            throw new ConstraintViolationException(violations);
        }

    }

    /**
     * Create Localized practice.
     *
     * @param valueMap
     * @return noOfPractices
     */
    private int createLocalizedBoard(final Map<String, Object> valueMap) {
        return new SimpleJdbcInsert(dataSource)
                .withTableName("practices_localized")
                .usingColumns("practice_id", "locale", "title", "description")
                .execute(valueMap);
    }

    /**
     * deletes pracice for a bookPath.
     * create if not exists alredy.
     *
     * @param bookPath
     * @return pracice
     */
    public boolean deleteQuestionBank(final String bookPath) {
        Optional<Practice> oPractice = readByBook(bookPath);
        if (oPractice.isPresent()) {
            return delete(oPractice.get().getId());
        }
        return false;
    }

    /**
     * get pracice for a bookPath.
     * create if not exists alredy.
     *
     * @param bookPath
     * @param locale
     * @return pracice
     */
    public Practice getQuestionBank(final String bookPath, final Locale locale)
            throws JsonProcessingException {
        Optional<Practice> oPractice = readByBook(bookPath);

        if (oPractice.isEmpty()) {
            Practice practice = new Practice();
            practice.setTitle("Book:$" + bookPath);
            practice.setDescription("Question Bank for the bookPath "
                    + bookPath);
            oPractice = create(bookPath,
                    "sathish", locale,
                    practice);

            addPracticeToBook(oPractice.get().getId(), getBookId(bookPath));
        }

        return oPractice.get();
    }

    /**
     * Gets Book Id for BookPath.
     *
     * @param bookPath
     * @return bookId
     */
    public UUID getBookId(final String bookPath) {
        String query = "SELECT ID FROM BOOKS WHERE PATH=?";
        return jdbcTemplate
                .queryForObject(query, UUID.class, bookPath);
    }

    /**
     * Adds Practice to a Book.
     *
     * @param practiceId
     * @param bookId
     * @return added
     */
    private boolean addPracticeToBook(final UUID practiceId,
                                      final UUID bookId) {
        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("practices_books")
                .usingColumns("practice_id",
                        "book_id");
        final Map<String, Object> valueMap = new HashMap<>(6);
        valueMap.put("practice_id", practiceId);
        valueMap.put("book_id", bookId);
        return insert.execute(valueMap) == 1;
    }
    private <T extends Practice> Optional<T> readByBook(
            final String bookPath) {
        final String query =
            "SELECT practices.id, practices.title, practices.created_by, "
            + "practices.type, "
        + "practices.meta_data, practices.description, practices.created_at, "
            + "practices.modified_at FROM practices JOIN practices_books ON "
            + "practices.id = practices_books.practice_id JOIN books "
            + "ON practices_books.book_id = books.id WHERE books.PATH = ?";

        try {
            final T p = (T) jdbcTemplate
                    .queryForObject(query, this::rowMapper, bookPath);
            return Optional.of(p);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    /**
     * read an practice.
     *
     * @param <T>           the type parameter
     * @param locale        the locale
     * @param newPracticeId the new practice id
     * @return p. optional
     */
    public <T extends Practice> Optional<T> read(final UUID newPracticeId,
                                                 final Locale locale) {
        final String query = locale == null
                ? "SELECT id,title,created_by,type,meta_data,"
                + "description,created_at,modified_at "
                + "FROM practices WHERE id = ?"
                : "SELECT practices.id,practices_localized.title,"
                + "practices.created_by,practices.type,practices.meta_data,"
                + "practices_localized.description,practices.created_at,"
                + "practices.modified_at,"
                + "practices.modified_by FROM practices "
                + "JOIN practices_localized ON "
                + "practices.id=practices_localized.practice_id "
                + "WHERE practices_localized.practice_id = ?"
                + "AND practices_localized.locale = ?";

        try {
            final T p = locale == null ? (T) jdbcTemplate
                    .queryForObject(query, this::rowMapper, newPracticeId)
                    : (T) jdbcTemplate
                    .queryForObject(query, this::rowMapper, newPracticeId,
                                    locale.getLanguage());
            return Optional.of(p);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    /**
     * update database.
     *
     * @param <T>      the type parameter
     * @param id       the id
     * @param practice the practice
     * @param locale   the locale
     * @return p. optional
     * @throws JsonProcessingException the json processing exception
     */
    public <T extends Practice> Optional<T> update(final UUID id,
                                                   final Locale locale,
                                                   final T practice)
            throws JsonProcessingException {
        Set<ConstraintViolation<Practice>> violations = validator
                .validate(practice);
        if (violations.isEmpty()) {
            final String query = locale == null
                    ? "UPDATE practices SET title=?, meta_data=?,"
                    + "description=?, modified_at=CURRENT_TIMESTAMP "
                    + "WHERE id = ?"
                    : "UPDATE practices SET meta_data=?,"
                    + "modified_at=CURRENT_TIMESTAMP "
                    + "WHERE id = ?";
            Integer updatedRows = locale == null
                    ? jdbcTemplate.update(query,
                    practice.getTitle(),
                    getMetadata(practice),
                    practice.getDescription(), id)
                    : jdbcTemplate.update(query,
                    getMetadata(practice),
                    id);
            if (updatedRows == 0) {
                throw new IllegalArgumentException("Practice not found");
            } else if (locale != null) {
                updatedRows = jdbcTemplate.update(
                        "UPDATE practices_localized SET title=?,locale=?,"
                            + "description=? WHERE practice_id=? AND locale=?",
                        practice.getTitle(), locale.getLanguage(),
                        practice.getDescription(), id, locale.getLanguage());
                if (updatedRows == 0) {
                    final Map<String, Object> valueMap = new HashMap<>(4);
                    valueMap.put("practice_id", id);
                    valueMap.put("locale", locale.getLanguage());
                    valueMap.put("title", practice.getTitle());
                    valueMap.put("description", practice.getDescription());
                    createLocalizedBoard(valueMap);
                }
            }
            return read(id, locale);
        } else {
            throw new ConstraintViolationException(violations);
        }

    }

    /**
     * deletes from database.
     *
     * @param id the id
     * @return successflag boolean
     */
    public Boolean delete(final UUID id) {
        final Optional<Practice> oPractice = read(id, null);
        Boolean success = false;
        if (oPractice.isPresent()) {

            String query = "DELETE FROM QUESTION_CHOICES WHERE question_id IN "
                    + "(SELECT id FROM questions WHERE exam_id=?)";
            jdbcTemplate.update(query, id);
            query = "DELETE FROM questions WHERE exam_id=?";
            jdbcTemplate.update(query, id);
            query = "DELETE FROM PRACTICES_BOOKS WHERE practice_id=?";
            jdbcTemplate.update(query, id);
            query = "DELETE FROM PRACTICES WHERE ID=?";
            final Integer updatedRows = jdbcTemplate.update(query, id);
            final Practice practice = oPractice.get();
            success = !(updatedRows == 0);
        }
        return success;
    }

    /**
     * Cleaning up all practices.
     *
     * @param type the type
     * @return no.of practices deleted
     */
    public Integer delete(final String type) {
        final int count = 0;
        final List<Practice> practices = list(type);
        practices.parallelStream()
                .forEach(exam -> delete(exam.getId()));
        return count;
    }

    /**
     * lists all from table.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @return lp list
     */
    public <T extends Practice> List<T> list(final String type) {

        final String recordsQuery =
                "SELECT id,title,created_by,type,meta_data,description,"
                        + "created_at,modified_at"
                        + " FROM practices where type = ?";
        final List<T> tList =
                jdbcTemplate.query(recordsQuery, this::rowMapper, type);
        return tList;
    }

    /**
     * lists all from table as page.
     *
     * @param <T>      the type parameter
     * @param type     the type
     * @param pageable the pageable
     * @return lp page
     */
    public <T extends Practice> Page<T> page(final String type,
                                             final Pageable pageable) {

        final String recordsQuery =
                "SELECT id,title,created_by,type,meta_data,description,"
                        + "created_at,modified_at"
                        + " FROM practices where type = ? LIMIT "
                        + pageable.getPageSize()
                        + " OFFSET "
                        + ((pageable.getPageNumber() * pageable.getPageSize()));

        final String countsQuery = "SELECT COUNT(id) FROM practices"
                + " where type = ?";

        return new PageImpl<T>(
                jdbcTemplate.query(recordsQuery, this::rowMapper, type),
                pageable,
                jdbcTemplate.queryForObject(countsQuery, Long.class, type));
    }

    /**
     * Cleaning up all practices.
     *
     * @return no.of exams deleted
     */
    public Integer delete() {
        final String query = "DELETE FROM practices";
        return jdbcTemplate.update(query);
    }
}
