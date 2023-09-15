package com.techatpark.workout.service;

import com.techatpark.workout.model.Category;
import com.techatpark.workout.model.Choice;
import com.techatpark.workout.model.Question;
import com.techatpark.workout.model.QuestionType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.apache.commons.lang3.math.IEEE754rUtils;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.lang.annotation.ElementType;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The type Question service.
 */
@Service
public class QuestionService {
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
     * Index.
     */
    private static final int INDEX_7 = 7;
    /**
     * Index.
     */
    private static final int INDEX_8 = 8;
    /**
     * this helps to practiceService.
     */
    private final CategoryService categoryService;

    /**
     * Validator.
     */
    private final Validator validator;

    /**
     * this creates connection functionalities.
     */
    private final DataSource dataSource;
    /**
     * JdbcClient.
     */
    private final JdbcClient jdbcClient;
    /**
     * Maps the data from and to the database. return question.
     */
    private final RowMapper<Question> rowMapper = (rs, rowNum) -> {
        final Question question = new Question();
        question.setId((UUID) rs.getObject(INDEX_1));
        question.setQuestion(rs.getString(INDEX_2));
        question.setExplanation(rs.getString(INDEX_3));
        question.setType(QuestionType.valueOf(rs.getString(INDEX_4)));
        question.setCreatedBy(rs.getString(INDEX_5));
        question.setAnswer(rs.getString(INDEX_6));


        LocalDate calendarDate = rs.getDate(INDEX_7)
                .toLocalDate();
        ZonedDateTime zdt = calendarDate.atStartOfDay(ZoneId
                .of("Europe/Paris"));


        question.setCreatedAt(zdt.toInstant());
        Date sqlDate = rs.getDate(INDEX_8);

        if (sqlDate != null) {
            calendarDate = sqlDate.toLocalDate();
            zdt = calendarDate.atStartOfDay(ZoneId.of("Europe/Paris"));
            question.setUpdatedAt(zdt.toInstant());
        }
        return question;
    };
    /**
     * Maps the data from and to the database. return question.
     */
    private final RowMapper<Choice> rowMapperQuestionChoice = (
            rs, rowNum) -> {
        final Choice choice = new Choice();
        choice.setId((UUID) rs.getObject(INDEX_1));
        choice.setValue(rs.getString(INDEX_2));
        choice.setAnswer(rs.getBoolean(INDEX_3));
        // https://docs.oracle.com/javase/7/docs/api/java/sql
        // /ResultSet.html#wasNull%28%29
        if (rs.wasNull()) {
            choice.setAnswer(null);
        }
        return choice;
    };

    /**
     * initializes.
     *
     * @param aJdbcClient      a jdbcClient
     * @param aCategoryService the practiceservice
     * @param aValidator       thevalidator
     * @param aDataSource      the a data source
     */
    public QuestionService(final CategoryService aCategoryService,
                           final Validator aValidator,
                           final DataSource aDataSource,
                           final JdbcClient aJdbcClient) {
        this.categoryService = aCategoryService;
        this.validator = aValidator;
        this.dataSource = aDataSource;
        this.jdbcClient = aJdbcClient;
    }


    /**
     * inserts data.
     *
     * @param categories the categories
     * @param type       the type
     * @param tags
     * @param locale     the locale
     * @param createdBy  the createdBy
     * @param question   the question
     * @return question optional
     */
    @Transactional
    public Optional<Question> create(
            final List<String> categories,
            final List<String> tags,
            final QuestionType type,
            final Locale locale,
            final String createdBy,
            final Question question) {
        question.setType(type);
        Set<ConstraintViolation<Question>> violations =
                getViolations(question);
        if (violations.isEmpty()) {
            final UUID id = UUID.randomUUID();

            final String insertQuery = """
                    INSERT INTO questions(id, question, explanation, type,
                    created_By, answer)
                    VALUES(?, ?, ?, ?, ?, ?)
                    """;
            jdbcClient.sql(insertQuery)
                    .param(INDEX_1, id)
                    .param(INDEX_2, question.getQuestion())
                    .param(INDEX_3, question.getExplanation())
                    .param(INDEX_4, type.toString())
                    .param(INDEX_5, createdBy)
                    .param(INDEX_6, question.getAnswer())
                    .update();

            if (locale != null) {
                final String insertQueryLocalized = """
                        INSERT INTO questions_localized(question_id, locale,
                        question, explanation)
                        VALUES(?, ?, ?, ?)
                            """;
                jdbcClient.sql(insertQueryLocalized)
                        .param(INDEX_1, id)
                        .param(INDEX_2, locale.getLanguage())
                        .param(INDEX_3, question.getQuestion())
                        .param(INDEX_4, question.getExplanation())
                        .update();
            }

            if ((question.getType().equals(QuestionType.CHOOSE_THE_BEST)
                    || question.getType().equals(QuestionType.MULTI_CHOICE))) {
                createChoices(question.getChoices(), locale, id);
            }

            categories.forEach(category -> attachCategories(createdBy,
                    id, category));

            return read(id, locale);
        } else {
            throw new ConstraintViolationException(violations);
        }

    }

    private void createChoice(final Choice choice,
                              final Locale locale,
                              final UUID questionId) {
        UUID choiceId = UUID.randomUUID();


        final String query = """
                INSERT INTO question_choices(id, question_id, c_value,
                is_answer)
                VALUES(?, ?, ?, ?)
                """;
        jdbcClient.sql(query)
                .param(INDEX_1, choiceId)
                .param(INDEX_2, questionId)
                .param(INDEX_3, choice.getValue())
                .param(INDEX_4,
                        choice.isAnswer() != null && choice.isAnswer())
                .update();

        if (locale != null) {
            choice.setId(choiceId);
            createLocalizedChoice(locale, choice);
        }


    }

    private void saveLocalizedChoice(final Locale locale,
                                     final Choice choice) {
        final String query = """
                UPDATE question_choices_localized
                SET c_value = ?
                WHERE choice_id = ? AND locale = ?
                """;

        int updatedRows = jdbcClient.sql(query)
                .param(INDEX_1, choice.getValue())
                .param(INDEX_2, choice.getId())
                .param(INDEX_3, locale.getLanguage()).update();
        if (updatedRows == 0) {
            createLocalizedChoice(locale, choice);
        }
    }

    private void createLocalizedChoice(final Locale locale,
                                       final Choice choice) {
        String query = """
                INSERT INTO question_choices_localized(
                choice_id, locale, c_value)
                VALUES(?, ?, ?)
                """;
        jdbcClient.sql(query)
                .param(INDEX_1, choice.getId())
                .param(INDEX_2, locale.getLanguage())
                .param(INDEX_3, choice.getValue())
                .update();
    }

    private void createChoices(final List<Choice> choices,
                               final Locale locale,
                               final UUID id) {
        if (choices != null) {
            choices.forEach(choice -> createChoice(choice, locale, id));
        }
    }

    /**
     * List question choice list.
     *
     * @param isOwner    isOwner calling
     * @param questionId the question choice id
     * @param locale
     * @return the list
     */
    private List<Choice> listQuestionChoice(final boolean isOwner,
                                            final UUID questionId,
                                            final Locale locale) {
        final String query = locale == null
                ? "SELECT id, c_value,"
                + (isOwner ? "is_answer" : "NULL")
                + " AS is_answer"
                + " FROM question_choices WHERE"
                + " question_id = ?"
                : "SELECT id,"
                + "CASE WHEN qcl.LOCALE = ? "
                + "THEN qcl.c_value "
                + "ELSE qc.c_value "
                + "END AS c_value, "
                + (isOwner ? "is_answer" : "NULL")
                + " AS is_answer"
                + " FROM question_choices qc "
                + "LEFT JOIN question_choices_localized qcl ON"
                + " qc.ID = qcl.choice_id WHERE"
                + " question_id = ? AND ( qcl.LOCALE IS NULL OR "
                + "qcl.LOCALE = ? OR qc.ID "
                + "NOT IN (SELECT choice_id FROM "
                + "question_choices_localized WHERE "
                + "choice_id=qc.ID AND LOCALE = ?))";
        return locale == null
                ?
                jdbcClient.sql(query).param(INDEX_1, questionId)
                        .query(rowMapperQuestionChoice).list()
                : jdbcClient.sql(query)
                .param(INDEX_1, locale.getLanguage())
                .param(INDEX_2, questionId)
                .param(INDEX_3, locale.getLanguage())
                .param(INDEX_4, locale.getLanguage())
                .query(rowMapperQuestionChoice).list();
    }

    /**
     * reads from question with given id.
     *
     * @param id     the id
     * @param locale
     * @return question optional
     */
    public Optional<Question> read(final UUID id,
                                   final Locale locale) {
        final String query = locale == null
                ? """
                SELECT id, question, explanation, type, created_by, answer,
                created_at, modified_at
                FROM questions
                WHERE id = ?
                """
                : """
                SELECT id,
                       CASE WHEN ql.LOCALE = ?
                       THEN ql.question ELSE q.question END AS question,
                       CASE WHEN ql.LOCALE = ?
                       THEN ql.explanation ELSE q.explanation END AS explanation,
                       type, created_by, answer, created_at, modified_at
                FROM questions q
                LEFT JOIN questions_localized ql ON q.ID = ql.QUESTION_ID
                WHERE q.id = ?
                AND (ql.LOCALE IS NULL OR ql.LOCALE = ? OR q.ID NOT IN (
                    SELECT question_id
                    FROM questions_localized
                    WHERE QUESTION_ID = q.ID AND LOCALE = ?
                ))
                """;

        try {

            Question question = locale == null ? jdbcClient
                    .sql(query).param(INDEX_1, id).query(rowMapper).single()
                    : jdbcClient
                    .sql(query).param(INDEX_1, locale.getLanguage())
                    .param(INDEX_2, locale.getLanguage())
                    .param(INDEX_3, id)
                    .param(INDEX_4, locale.getLanguage())
                    .param(INDEX_5, locale.getLanguage())
                    .query(rowMapper).single();

            if ((question.getType().equals(QuestionType.CHOOSE_THE_BEST)
                    || question.getType().equals(QuestionType.MULTI_CHOICE))) {
                question.setChoices(
                        listQuestionChoice(true, question.getId(), locale));
            }
            return Optional.of(question);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * updates question with id.
     *
     * @param id       the id
     * @param locale   the language
     * @param type     the type
     * @param question the question
     * @return question optional
     */
    public Optional<Question> update(
            final QuestionType type,
            final UUID id,
            final Locale locale,
            final Question question) {
        question.setType(type);
        Set<ConstraintViolation<Question>> violations =
                getViolations(question);
        if (violations.isEmpty()) {
            final String query = locale == null
                    ? """
                    UPDATE questions
                    SET question = ?, explanation = ?, answer = ?, 
                    modified_at = CURRENT_TIMESTAMP
                    WHERE id = ? AND type = ?
                    """
                    : """
                    UPDATE questions
                    SET answer = ?, modified_at = CURRENT_TIMESTAMP
                    WHERE id = ? AND type = ?
                    """;

            Integer updatedRows = locale == null
                    ? jdbcClient.sql(query)
                    .param(INDEX_1, question.getQuestion())
                    .param(INDEX_2, question.getExplanation())
                    .param(INDEX_3, question.getAnswer())
                    .param(INDEX_4, id)
                    .param(INDEX_5, type.toString()).update()
                    : jdbcClient.sql(query)
                    .param(INDEX_1, question.getAnswer())
                    .param(INDEX_2, id).param(INDEX_3, type.toString())
                    .update();


            if (locale != null) {
                final String localizedUpdateQuery = """
                        UPDATE QUESTIONS_LOCALIZED SET question = ?,
                        explanation = ?
                            WHERE question_id = ? AND
                                    locale = ? AND
                                question_id IN
                                    ( SELECT id from questions
                                            where type
                                            = ?  )
                        """;

                updatedRows = jdbcClient.sql(localizedUpdateQuery)
                        .param(INDEX_1, question.getQuestion())
                        .param(INDEX_2, question.getExplanation())
                        .param(INDEX_3, id)
                        .param(INDEX_4, locale.getLanguage())
                        .param(INDEX_5, type.toString())
                        .update();

                if (updatedRows == 0) {
                    final String localizedInsertQuery = """
                            INSERT INTO QUESTIONS_LOCALIZED
                                ( question_id, locale, question, explanation )
                                VALUES ( ?, ? , ?, ?)
                            """;
                    updatedRows = jdbcClient.sql(localizedInsertQuery)
                            .param(INDEX_1, id)
                            .param(INDEX_2, locale.getLanguage())
                            .param(INDEX_3, question.getQuestion())
                            .param(INDEX_4, question.getExplanation())
                            .update();

                }
            }

            if ((type.equals(QuestionType.CHOOSE_THE_BEST)
                    || type.equals(QuestionType.MULTI_CHOICE))
                    && question.getChoices() != null) {

                List<UUID> availableIds = question.getChoices()
                        .stream()
                        .filter(choice -> choice.getId() != null)
                        .map(Choice::getId)
                        .collect(Collectors.toList());

                if (!availableIds.isEmpty()) {
                    final String deletequestionChoice =
                            "DELETE FROM question_choices "
                                    + "WHERE question_id = ? AND id NOT IN ("
                                    + availableIds.stream()
                                    .map(aId -> "?")
                                    .collect(Collectors.joining(","))
                                    + ")";
                    availableIds.add(0, id);
                    jdbcClient.sql(deletequestionChoice).params(availableIds)
                            .update();
                }


                question.getChoices().forEach(choice -> {
                    if (choice.getId() == null) {
                        createChoice(choice, locale, id);
                    } else {
                        updateChoice(choice, locale);
                    }
                });

            }
            return updatedRows == 0 ? null : read(id, locale);
        } else {
            throw new ConstraintViolationException(violations);
        }


    }

    private void updateChoice(final Choice choice,
                              final Locale locale) {
        final String updatequestionChoice = locale == null
                ? """
                UPDATE question_choices
                SET c_value = ?,
                    is_answer = ?
                WHERE id = ?
                """
                : """
                UPDATE question_choices
                SET is_answer = ?
                WHERE id = ?
                """;
        if (locale == null) {
            jdbcClient.sql(updatequestionChoice)
                    .param(INDEX_1, choice.getValue())
                    .param(INDEX_2,
                            choice.isAnswer() != null && choice.isAnswer())
                    .param(INDEX_3, choice.getId());
        } else {
            jdbcClient.sql(updatequestionChoice)
                    .param(INDEX_1,
                            choice.isAnswer() != null && choice.isAnswer())
                    .param(INDEX_2, choice.getId());

            saveLocalizedChoice(locale, choice);
        }

    }


    /**
     * deletes from database.
     *
     * @param id the id
     * @return successflag boolean
     */
    public Boolean delete(final UUID id) {
        final String queryL =
                """
                        DELETE FROM questions_localized WHERE question_id = ?
                        """;
        jdbcClient.sql(queryL).param(INDEX_1, id).update();
        String query = "DELETE FROM questions WHERE ID=?";

        final Integer updatedRows = jdbcClient.sql(query)
                .param(INDEX_1, id).update();
        return !(updatedRows == 0);
    }

    /**
     * delete all records from questionchoice with the given question id.
     *
     * @param questionId
     * @return successflag boolean
     */
    public Boolean deleteQuestionChoice(final UUID questionId) {
        final String queryL =
                """
                        DELETE FROM question_choices_localized WHERE choice_id IN
                        (SELECT id FROM question_choices WHERE question_id = ?)
                                """;
        jdbcClient.sql(queryL)
                .param(INDEX_1, questionId).update();
        final String query =
                "DELETE FROM question_choices WHERE question_id = ?";
        final Integer updatedRows = jdbcClient.sql(query)
                .param(INDEX_1, questionId).update();
        return !(updatedRows == 0);
    }


    /**
     * List questions of exam.
     *
     * @param userName   the user name
     * @param categories the categories
     * @param locale     the locale
     * @return quetions in given exam
     */
    public List<Question> list(final String userName,
                               final Locale locale,
                               final List<String> categories) {

        boolean isOwner = true;

        final String query = locale == null
                ? "SELECT id,question,explanation,type,"
                + "created_by"
                + (isOwner ? "answer" : "NULL")
                + " AS answer,"
                + "created_at,modified_at"
                + " FROM questions"
                + " where "
                + "id IN (" + getQuestionIdFilter(categories) + ") "
                + " order by id"
                : "SELECT id,"
                + "CASE WHEN ql.LOCALE = ? "
                + "THEN ql.question "
                + "ELSE q.question "
                + "END AS question,"
                + "CASE WHEN ql.LOCALE = ? "
                + "THEN ql.explanation "
                + "ELSE q.explanation "
                + "END AS explanation,"
                + "type, created_by"
                + (isOwner ? "q.answer" : "NULL")
                + " AS answer"
                + ",created_at,modified_at FROM "
                + "questions q LEFT JOIN questions_localized ql ON "
                + "q.ID = ql.QUESTION_ID WHERE"
                + " q.ID IN (" + getQuestionIdFilter(categories) + ") "
                + "  AND"
                + " (ql.LOCALE IS NULL "
                + "OR ql.LOCALE = ? OR "
                + "q.ID NOT IN "
                + "(SELECT question_id FROM questions_localized "
                + "WHERE QUESTION_ID=q.ID AND LOCALE = ?))";

        List<Object> parameters = new ArrayList<>();
        if (locale == null) {
            parameters.addAll(categories);
        } else {
            parameters.add(locale.getLanguage());
            parameters.add(locale.getLanguage());
            parameters.addAll(categories);
            parameters.add(locale.getLanguage());
            parameters.add(locale.getLanguage());
        }


        List<Question> questions =
                jdbcClient.sql(query).params(parameters).query(rowMapper)
                        .list();

        if (!questions.isEmpty()) {
            questions.forEach(question -> {
                if ((question.getType().equals(QuestionType.CHOOSE_THE_BEST)
                        || question.getType()
                        .equals(QuestionType.MULTI_CHOICE))) {
                    question.setChoices(this
                            .listQuestionChoice(isOwner,
                                    question.getId(), locale));
                }
            });
        }
        return questions;
    }

    private String getQuestionIdFilter(final List<String> categories) {
        String builder = "SELECT QUESTION_ID FROM "
                + "questions_categories WHERE category_id IN (" + categories.stream()
                .map(tag -> "?")
                .collect(Collectors.joining(",")) +
                ") " +
                "GROUP BY QUESTION_ID " +
                "HAVING COUNT(DISTINCT category_id) = " +
                categories.size();
        return builder;
    }

    /**
     * list of question.
     *
     * @param pageNumber the page number
     * @param pageSize   the page size
     * @param locale     the locale
     * @return question list
     */
    public List<Question> list(final Integer pageNumber,
                               final Integer pageSize,
                               final Locale locale) {
        String query = """
                SELECT id,question,explanation,type,created_by,
                created_at,modified_at,answer FROM questions
                """;
        query = query + " LIMIT " + pageSize + " OFFSET " + (pageNumber - 1);
        return jdbcClient.sql(query).query(rowMapper).list();
    }

    /**
     * Validate Question.
     *
     * @param question
     * @return violations
     */
    private Set<ConstraintViolation<Question>> getViolations(final Question
                                                                     question) {
        Set<ConstraintViolation<Question>> violations = new HashSet<>(validator
                .validate(question));
        if (violations.isEmpty()) {
            final String messageTemplate = null;
            final Class<Question> rootBeanClass = Question.class;
            final Object leafBeanInstance = null;
            final Object cValue = null;
            final Path propertyPath = null;
            final ConstraintDescriptor<?> constraintDescriptor = null;
            final ElementType elementType = null;
            final Map<String, Object> messageParameters = new HashMap<>();
            final Map<String, Object> expressionVariables = new HashMap<>();
            if (question.getType().equals(QuestionType.MULTI_CHOICE)
                    || question.getType()
                    .equals(QuestionType.CHOOSE_THE_BEST)) {
                if (question.getChoices() == null
                        || question.getChoices().size() < 2) {
                    ConstraintViolation<Question> violation
                            = ConstraintViolationImpl.forBeanValidation(
                            messageTemplate, messageParameters,
                            expressionVariables,
                            "Minimun 2 choices",
                            rootBeanClass,
                            question, leafBeanInstance, cValue, propertyPath,
                            constraintDescriptor, elementType);
                    violations.add(violation);
                } else if (question.getAnswer() != null) {
                    ConstraintViolation<Question> violation
                            = ConstraintViolationImpl.forBeanValidation(
                            messageTemplate, messageParameters,
                            expressionVariables,
                            "Answer should be empty", rootBeanClass,
                            question, leafBeanInstance, cValue, propertyPath,
                            constraintDescriptor, elementType);
                    violations.add(violation);
                }
            } else {
                if (question.getAnswer() == null) {
                    ConstraintViolation<Question> violation
                            = ConstraintViolationImpl.forBeanValidation(
                            messageTemplate, messageParameters,
                            expressionVariables,
                            "Answer should not be empty",
                            rootBeanClass,
                            question, leafBeanInstance, cValue, propertyPath,
                            constraintDescriptor, elementType);
                    violations.add(violation);
                } else if (question.getChoices() != null) {
                    ConstraintViolation<Question> violation
                            = ConstraintViolationImpl.forBeanValidation(
                            messageTemplate, messageParameters,
                            expressionVariables,
                            "Choiced should not be aavailable",
                            rootBeanClass,
                            question, leafBeanInstance, cValue, propertyPath,
                            constraintDescriptor, elementType);
                    violations.add(violation);
                }
            }
        }
        return violations;
    }

    /**
     * deletes from database.
     *
     * @param id           the id
     * @param questionType the questionType
     * @return successflag boolean
     */
    @Transactional
    public Boolean deleteAQuestion(final UUID id,
                                   final QuestionType questionType) {

        deleteQuestionChoice(id);

        final String queryL =
                "DELETE FROM QUESTIONS_LOCALIZED WHERE question_id=?";

        jdbcClient.sql(queryL).param(id).update();

        final String query =
                "DELETE FROM questions WHERE ID=? and type = ?";
        int updatedRow =
                jdbcClient.sql(query)
                        .param(INDEX_1, id)
                        .param(INDEX_2, questionType.toString())
                        .update();
        return !(updatedRow == 0);

    }


    /**
     * Adds tag to question.
     *
     * @param userName
     * @param questionId the questionId
     * @param categoryId the categoryId
     * @return grade optional
     */
    private boolean attachCategories(final String userName,
                                     final UUID questionId,
                                     final String categoryId) {
        String insertQuery = """
                INSERT INTO questions_categories(question_id, category_id)
                VALUES(?, ?)
                """;

        int noOfRowsInserted = 0;

        try {
            noOfRowsInserted = jdbcClient.sql(insertQuery)
                    .param(INDEX_1, questionId)
                    .param(INDEX_2, categoryId)
                    .update();
        } catch (final DataIntegrityViolationException e) {
            // Retry with Auto Create Category
            Category category = this.categoryService.create(
                    userName, null, new Category(categoryId,
                            categoryId.toUpperCase(), null, null,
                            null, null));
            if (category != null) {
                return attachCategories(userName, questionId, category.id());
            }
        }

        // DataIntegrityViolationException

        return noOfRowsInserted == 1;
    }

    /**
     * Deletes Questions.
     */
    public void deleteAll() {

        jdbcClient.sql("DELETE FROM questions_categories").update();

        jdbcClient.sql("DELETE FROM questions_tags").update();

        jdbcClient.sql("DELETE FROM question_choices_localized").update();
        jdbcClient.sql("DELETE FROM question_choices").update();

        jdbcClient.sql("DELETE FROM QUESTIONS_LOCALIZED").update();
        jdbcClient.sql("DELETE FROM QUESTIONS").update();

    }
}
