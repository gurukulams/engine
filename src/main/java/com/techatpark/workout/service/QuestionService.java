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
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
     * this helps to execute sql queries.
     */
    private final JdbcTemplate jdbcTemplate;

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
     * Maps the data from and to the database. return question.
     */
    private final RowMapper<Question> rowMapper = (rs, rowNum) -> {
        final Question question = new Question();
        question.setId((UUID) rs.getObject("id"));
        question.setQuestion(rs.getString("question"));
        question.setExplanation(rs.getString("explanation"));
        question.setType(QuestionType.valueOf(rs.getString("type")));
        question.setCreatedBy(rs.getString("created_by"));
        question.setAnswer(rs.getString("answer"));


        LocalDate calendarDate = rs.getDate("created_at")
                .toLocalDate();
        ZonedDateTime zdt = calendarDate.atStartOfDay(ZoneId
                .of("Europe/Paris"));


        question.setCreatedAt(zdt.toInstant());
        Date sqlDate = rs.getDate("modified_at");

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
        choice.setId((UUID) rs.getObject("id"));
        choice.setValue(rs.getString("c_value"));
        choice.setAnswer(rs.getBoolean("is_answer"));
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
     * @param aJdbcTemplate    the a jdbc template
     * @param aCategoryService the practiceservice
     * @param aValidator       thevalidator
     * @param aDataSource      the a data source
     */
    public QuestionService(final JdbcTemplate aJdbcTemplate,
                           final CategoryService aCategoryService,
                           final Validator aValidator,
                           final DataSource aDataSource) {
        this.jdbcTemplate = aJdbcTemplate;
        this.categoryService = aCategoryService;
        this.validator = aValidator;
        this.dataSource = aDataSource;
    }


    /**
     * inserts data.
     *
     * @param categories      the categories
     * @param type      the type
     * @param tags
     * @param locale    the locale
     * @param createdBy the createdBy
     * @param question  the question
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
            final SimpleJdbcInsert insert =
                    new SimpleJdbcInsert(dataSource)
                            .withTableName("questions")
                            .usingColumns("id",
                                    "question", "explanation",
                                    "type",
                                    "created_By", "answer");

            final Map<String, Object> valueMap = new HashMap<>();
            valueMap.put("question", question.getQuestion());
            valueMap.put("explanation", question.getExplanation());
            valueMap.put("type", type);
            valueMap.put("created_by", createdBy);
            valueMap.put("answer", question.getAnswer());

            final UUID id = UUID.randomUUID();
            valueMap.put("id", id);
            insert.execute(valueMap);

            if (locale != null) {
                valueMap.put("question_id", id);
                valueMap.put("locale", locale.getLanguage());
                new SimpleJdbcInsert(dataSource)
                        .withTableName("questions_localized")
                        .usingColumns("question_id",
                                "locale", "question", "explanation")
                        .execute(valueMap);
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
        final SimpleJdbcInsert insertQuestionChoice =
                new SimpleJdbcInsert(dataSource)
                        .withTableName("question_choices")
                        .usingColumns("id", "question_id",
                                "c_value", "is_answer");


        Map<String, Object> valueMapQuestionChoice =
                new HashMap<>();
        valueMapQuestionChoice.put("question_id", questionId);
        valueMapQuestionChoice.put("c_value", choice.getValue());
        valueMapQuestionChoice.put("is_answer",
                choice.isAnswer() != null && choice.isAnswer());

        UUID choiceId = UUID.randomUUID();
        valueMapQuestionChoice.put("id", choiceId);
        insertQuestionChoice
                .execute(valueMapQuestionChoice);

        if (locale != null) {
            choice.setId(choiceId);
            createLocalizedChoice(locale, choice);
        }


    }

    private void saveLocalizedChoice(final Locale locale,
                                     final Choice choice) {
        final String query = "UPDATE question_choices_localized SET "
                + "c_value = ?"
                + " WHERE choice_id = ? AND locale = ?";
        int updatedRows = jdbcTemplate.update(query, choice.getValue(),
                choice.getId(), locale.getLanguage());
        if (updatedRows == 0) {
            createLocalizedChoice(locale, choice);
        }
    }

    private void createLocalizedChoice(final Locale locale,
                                       final Choice choice) {
        final SimpleJdbcInsert insertLocalizedQuestionChoice =
                new SimpleJdbcInsert(dataSource)
                        .withTableName("question_choices_localized")
                        .usingColumns("choice_id",
                                "locale", "c_value");
        Map<String, Object> valueMapQuestionChoice =
                new HashMap<>();
        valueMapQuestionChoice.put("choice_id", choice.getId());
        valueMapQuestionChoice.put("locale", locale.getLanguage());
        valueMapQuestionChoice.put("c_value", choice.getValue());
        insertLocalizedQuestionChoice
                .execute(valueMapQuestionChoice);
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
                ? "SELECT id,question_id,c_value,"
                + (isOwner ? "is_answer" : "NULL")
                + " AS is_answer"
                + " FROM question_choices WHERE"
                + " question_id = ?"
                : "SELECT id,question_id,"
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
                ? jdbcTemplate.query(query, rowMapperQuestionChoice,
                questionId)
                : jdbcTemplate.query(query,
                rowMapperQuestionChoice, locale.getLanguage(),
                questionId, locale.getLanguage(),
                locale.getLanguage());
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
                ? "SELECT id,question,explanation"
                + ",created_by,type,"
                + "answer,created_at,modified_at FROM "
                + "questions WHERE"
                + " id = ?"
                : "SELECT id,"
                + "CASE WHEN ql.LOCALE = ? "
                + "THEN ql.question "
                + "ELSE q.question "
                + "END AS question,"
                + "CASE WHEN ql.LOCALE = ? "
                + "THEN ql.explanation "
                + "ELSE q.explanation "
                + "END AS explanation,"
                + "created_by,type,"
                + "answer,created_at,modified_at FROM "
                + "questions q LEFT JOIN questions_localized ql ON "
                + "q.ID = ql.QUESTION_ID WHERE"
                + " q.id = ? "
                + "AND (ql.LOCALE IS NULL "
                + "OR ql.LOCALE = ? OR "
                + "q.ID NOT IN "
                + "(SELECT question_id FROM questions_localized "
                + "WHERE QUESTION_ID=q.ID AND LOCALE = ?))";
        try {

            Question question = locale == null ? jdbcTemplate
                    .queryForObject(query, rowMapper, id)
                    : jdbcTemplate
                    .queryForObject(query, rowMapper, locale.getLanguage(),
                                    locale.getLanguage(),
                            id, locale.getLanguage(), locale.getLanguage());

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
                    ? "UPDATE questions SET "
                    + "question = ?,explanation = ?, answer = ?,"
                    + "modified_at=CURRENT_TIMESTAMP"
                    + " WHERE id = ? AND type = ? "
                    : "UPDATE questions SET "
                    + "answer = ?,"
                    + "modified_at=CURRENT_TIMESTAMP"
                    + " WHERE id = ? AND type = ? ";
            Integer updatedRows = locale == null
                    ? jdbcTemplate.update(query,
                    question.getQuestion(),
                    question.getExplanation(),
                    question.getAnswer(), id, type.toString())
                    : jdbcTemplate.update(query,
                    question.getAnswer(), id, type.toString());

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

                updatedRows = jdbcTemplate.update(localizedUpdateQuery,
                        question.getQuestion(), question.getExplanation(),
                        id, locale.getLanguage(),
                        type.toString());

                if (updatedRows == 0) {
                    final String localizedInsertQuery = """
                            INSERT INTO QUESTIONS_LOCALIZED
                                ( question_id, locale, question, explanation )
                                VALUES ( ?, ? , ?, ?)
                            """;
                    updatedRows = jdbcTemplate.update(localizedInsertQuery,
                            id, locale.getLanguage(),
                            question.getQuestion(), question.getExplanation());

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
                    jdbcTemplate.update(deletequestionChoice,
                            availableIds.toArray());
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
                ? "UPDATE question_choices SET c_value = ?, "
                + "is_answer = ? "
                + "WHERE id = ?"
                : "UPDATE question_choices SET "
                + "is_answer = ? "
                + "WHERE id = ?";
        if (locale == null) {
            jdbcTemplate.update(updatequestionChoice,
                    choice.getValue(),
                    choice.isAnswer() != null && choice.isAnswer(),
                    choice.getId());
        } else {
            jdbcTemplate.update(updatequestionChoice,
                    choice.isAnswer() != null && choice.isAnswer(),
                    choice.getId());

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
        jdbcTemplate.update(queryL, id);
        String query = "DELETE FROM questions WHERE ID=?";

        final Integer updatedRows = jdbcTemplate.update(query, id);
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
        jdbcTemplate.update(queryL, questionId);
        final String query =
                "DELETE FROM question_choices WHERE question_id = ?";
        final Integer updatedRows = jdbcTemplate.update(query, questionId);
        return !(updatedRows == 0);
    }


    /**
     * List questions of exam.
     *
     * @param userName the user name
     * @param categories     the categories
     * @param locale   the locale
     * @return quetions in given exam
     */
    public List<Question> list(final String userName,
                               final Locale locale,
                               final List<String> categories) {

        boolean isOwner = true;

        final String query = locale == null
                ? "SELECT id,question,explanation,type,"
                + "created_by,created_at,modified_at,"
                + (isOwner ? "answer" : "NULL")
                + " AS answer"
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
                + "created_by,type,"
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


        List<Question> questions = jdbcTemplate.query(query, rowMapper,
                parameters.toArray());

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
        StringBuilder builder =
                new StringBuilder("SELECT QUESTION_ID FROM "
                        + "questions_categories WHERE category_id IN (");
        builder.append(categories.stream()
                .map(tag -> "?")
                .collect(Collectors.joining(",")));
        builder.append(") ");
        builder.append("GROUP BY QUESTION_ID ");
        builder.append("HAVING COUNT(DISTINCT category_id) = ");
        builder.append(categories.size());
        return builder.toString();
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
        String query = "SELECT id,question,explanation,type,"
                + "created_by,created_at,modified_at,answer FROM questions";
        query = query + " LIMIT " + pageSize + " OFFSET " + (pageNumber - 1);
        return jdbcTemplate.query(query, rowMapper);
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

        jdbcTemplate.update(queryL, id);

        final String query =
                "DELETE FROM questions WHERE ID=? and type = ?";
        int updatedRow =
                jdbcTemplate.update(query, id, questionType.toString());
        return !(updatedRow == 0);

    }


    /**
     * Adds tag to question.
     * @param userName
     * @param questionId the questionId
     * @param categoryId      the categoryId
     * @return grade optional
     */
    private boolean attachCategories(final String userName,
                                    final UUID questionId,
                                    final String categoryId) {
        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("questions_categories")
                .usingColumns("question_id", "category_id");

        final Map<String, Object> valueMap = new HashMap<>();

        valueMap.put("question_id", questionId);
        valueMap.put("category_id", categoryId);

        int noOfRowsInserted = 0;

        try {
            noOfRowsInserted = insert.execute(valueMap);
        } catch (final DataIntegrityViolationException e) {
            // Retry with Auto Create Category
            Category category = new Category(categoryId,
                    categoryId.toUpperCase(), null, null,
                    null, null);
            this.categoryService.create(userName, null, category);

            noOfRowsInserted = insert.execute(valueMap);


        }

        // DataIntegrityViolationException

        return noOfRowsInserted == 1;
    }

    /**
     * Deletes Questions.
     */
    public void deleteAll() {

        jdbcTemplate.update("DELETE FROM questions_categories");
        jdbcTemplate.update("DELETE FROM categories");

        jdbcTemplate.update("DELETE FROM questions_tags");
        jdbcTemplate.update("DELETE FROM tags");

        jdbcTemplate.update("DELETE FROM question_choices_localized");
        jdbcTemplate.update("DELETE FROM question_choices");

        jdbcTemplate.update("DELETE FROM QUESTIONS_LOCALIZED");
        jdbcTemplate.update("DELETE FROM QUESTIONS");

    }
}
