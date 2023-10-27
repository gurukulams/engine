package com.techatpark.workout.service;

import com.gurukulams.core.GurukulamsManager;
import com.gurukulams.core.model.Category;
import com.gurukulams.core.model.QuestionChoice;
import com.gurukulams.core.model.QuestionChoiceLocalized;
import com.gurukulams.core.service.CategoryService;
import com.gurukulams.core.store.QuestionCategoryStore;
import com.gurukulams.core.store.QuestionChoiceLocalizedStore;
import com.gurukulams.core.store.QuestionChoiceStore;
import com.gurukulams.core.store.QuestionLocalizedStore;
import com.gurukulams.core.store.QuestionStore;
import com.gurukulams.core.store.QuestionTagStore;
import com.techatpark.workout.payload.Question;
import com.techatpark.workout.payload.QuestionType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
     * JdbcClient.
     */
    private final JdbcClient jdbcClient;

    /**
     * QuestionStore.
     */
    private final QuestionStore questionStore;

    /**
     * QuestionLocalized.
     */
    private final QuestionLocalizedStore questionLocalizedStore;

    /**
     * QuestionChoiceStore.
     */
    private final QuestionChoiceStore questionChoiceStore;

    /**
     * QuestionChoiceLocalized.
     */
    private final QuestionChoiceLocalizedStore questionChoiceLocalizedStore;


    /**
     * QuestionCategoryStore.
     */
    private final QuestionCategoryStore questionCategoryStore;


    /**
     * QuestionTagStore.
     */
    private final QuestionTagStore questionTagStore;


    /**
     * initializes.
     *
     * @param aJdbcClient      a jdbcClient
     * @param aCategoryService the practiceservice
     * @param aValidator       thevalidator
     * @param gurukulamsManager
     */
    public QuestionService(final CategoryService aCategoryService,
                           final Validator aValidator,
                           final JdbcClient aJdbcClient,
                           final GurukulamsManager gurukulamsManager) {
        this.categoryService = aCategoryService;
        this.validator = aValidator;
        this.jdbcClient = aJdbcClient;
        this.questionStore = gurukulamsManager
                .getQuestionStore();
        this.questionLocalizedStore = gurukulamsManager
                .getQuestionLocalizedStore();
        this.questionChoiceStore = gurukulamsManager
                .getQuestionChoiceStore();
        this.questionChoiceLocalizedStore = gurukulamsManager
                .getQuestionChoiceLocalizedStore();
        this.questionCategoryStore = gurukulamsManager
                .getQuestionCategoryStore();
        this.questionTagStore = gurukulamsManager
                .getQuestionTagStore();
    }


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


        question.setCreatedAt(rs.getDate(INDEX_7)
                .toLocalDate().atStartOfDay());
        if (rs.getDate(INDEX_8) != null) {
            question.setUpdatedAt(rs.getDate(INDEX_8)
                    .toLocalDate().atStartOfDay());
        }


        return question;
    };



    /**
     * inserts data.
     *
     * @param categories the category
     * @param type       the type
     * @param tag
     * @param locale     the locale
     * @param createdBy  the createdBy
     * @param question   the question
     * @return question optional
     */
    @Transactional
    public Optional<Question> create(
            final List<String> categories,
            final List<String> tag,
            final QuestionType type,
            final Locale locale,
            final String createdBy,
            final Question question) throws SQLException {
        question.setType(type);
        Set<ConstraintViolation<Question>> violations =
                getViolations(question);
        if (violations.isEmpty()) {
            final UUID id = UUID.randomUUID();

            question.setId(id);
            question.setType(type);
            question.setCreatedAt(LocalDateTime.now());

            this.questionStore
                    .insert()
                    .values(getQuestionModel(createdBy, question))
                    .execute();
            if (locale != null) {
                final String insertQueryLocalized = """
                        INSERT INTO question_localized(question_id, locale,
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

            categories.forEach(category -> {
                try {
                    attachCategory(createdBy,
                            id, category);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            return read(id, locale);
        } else {
            throw new ConstraintViolationException(violations);
        }

    }

    private com.gurukulams.core.model.Question
    getQuestionModel(final String createdBy, final Question question) {
        com.gurukulams.core.model.Question questionModel
                = new com.gurukulams.core.model.Question();
        questionModel.setQuestion(question.getQuestion());
        questionModel.setExplanation(question.getExplanation());
        questionModel.setId(question.getId());
        questionModel.setAnswer(question.getAnswer());
        questionModel.setType(question.getType().name());
        questionModel.setCreatedBy(createdBy);
        questionModel.setCreatedAt(question.getCreatedAt());
        return questionModel;
    }

    private Question
    getQuestion(final com.gurukulams.core.model.Question questionModel) {
        Question question
                = new Question();
        question.setQuestion(questionModel.getQuestion());
        question.setExplanation(questionModel.getExplanation());
        question.setId(questionModel.getId());
        question.setAnswer(questionModel.getAnswer());
        question.setType(QuestionType.valueOf(questionModel.getType()));
        question.setCreatedBy(questionModel.getCreatedBy());
        question.setCreatedAt(questionModel.getCreatedAt());
        question.setUpdatedAt(questionModel.getModifiedAt());
        return question;
    }

    private void createChoice(final QuestionChoice choice,
                              final Locale locale,
                              final UUID questionId) throws SQLException {
        UUID choiceId = UUID.randomUUID();

        choice.setId(choiceId);
        choice.setQuestionId(questionId);
        if (choice.getIsAnswer() == null) {
            choice.setIsAnswer(Boolean.FALSE);
        }
        this.questionChoiceStore.insert().values(choice)
                .execute();

        if (locale != null) {
            choice.setId(choiceId);
            createLocalizedChoice(locale, choice);
        }


    }

    private void createLocalizedChoice(final Locale locale,
                                       final QuestionChoice choice)
            throws SQLException {
        QuestionChoiceLocalized questionChoiceLocalized
                = new QuestionChoiceLocalized();

        questionChoiceLocalized.setChoiceId(choice.getId());
        questionChoiceLocalized.setLocale(locale.getLanguage());
        questionChoiceLocalized.setCValue(choice.getCValue());

        this.questionChoiceLocalizedStore
                .insert()
                .values(questionChoiceLocalized).execute();
    }

    private void saveLocalizedChoice(final Locale locale,
                                     final QuestionChoice choice)
            throws SQLException {

        int updatedRows = this.questionChoiceLocalizedStore
                .update()
                .set(QuestionChoiceLocalizedStore
                        .cValue(choice.getCValue()))
                .where(QuestionChoiceLocalizedStore
                        .choiceId().eq(choice.getId())
                        .and(QuestionChoiceLocalizedStore
                                .locale().eq(locale.getLanguage())))
                .execute();
        if (updatedRows == 0) {
            createLocalizedChoice(locale, choice);
        }
    }

    private void createChoices(final List<QuestionChoice> choices,
                               final Locale locale,
                               final UUID id) throws SQLException {
        if (choices != null) {
            for (QuestionChoice choice : choices) {
                createChoice(choice, locale, id);
            }
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
    private List<QuestionChoice> listQuestionChoice(final boolean isOwner,
                                            final UUID questionId,
                                            final Locale locale)
            throws SQLException {
        if (locale == null) {
            List<QuestionChoice> choices = this.questionChoiceStore
                    .select(QuestionChoiceStore.questionId().eq(questionId))
                    .execute();

            if (!isOwner) {
                choices.forEach(choice
                        -> choice.setIsAnswer(null));
            }
            return choices;
        } else {
            final String query =  "SELECT id,question_id,"
                    + "CASE WHEN qcl.LOCALE = ? "
                    + "THEN qcl.c_value "
                    + "ELSE qc.c_value "
                    + "END AS c_value, "
                    + (isOwner ? "is_answer" : "NULL")
                    + " AS is_answer"
                    + " FROM question_choice qc "
                    + "LEFT JOIN question_choice_localized qcl ON"
                    + " qc.ID = qcl.choice_id WHERE"
                    + " question_id = ? AND ( qcl.LOCALE IS NULL OR "
                    + "qcl.LOCALE = ? OR qc.ID "
                    + "NOT IN (SELECT choice_id FROM "
                    + "question_choice_localized WHERE "
                    + "choice_id=qc.ID AND LOCALE = ?))";

            return this.questionChoiceStore
                    .select()
                    .sql(query)
                    .param(QuestionChoiceLocalizedStore
                            .locale(locale.getLanguage()))
                    .param(QuestionChoiceStore
                            .questionId(questionId))
                    .param(QuestionChoiceLocalizedStore
                            .locale(locale.getLanguage()))
                    .param(QuestionChoiceLocalizedStore
                            .locale(locale.getLanguage()))
                    .list();

        }
    }

    /**
     * reads from question with given id.
     *
     * @param id     the id
     * @param locale
     * @return question optional
     */
    public Optional<Question> read(final UUID id,
                                   final Locale locale) throws SQLException {

        Optional<com.gurukulams.core.model.Question> qm;

        if (locale == null) {
            qm = this.questionStore.select(id);
        } else {
            final String query = """
                SELECT id,
                       CASE WHEN ql.LOCALE = ?
                       THEN ql.question ELSE q.question END AS question,
                       CASE WHEN ql.LOCALE = ?
                       THEN ql.explanation ELSE q.explanation
                       END AS explanation,
                       type, answer, created_at,created_by,
                       modified_at,modified_by
                FROM question q
                LEFT JOIN question_localized ql ON q.ID = ql.QUESTION_ID
                WHERE q.id = ?
                AND (ql.LOCALE IS NULL OR ql.LOCALE = ? OR q.ID NOT IN (
                    SELECT question_id
                    FROM question_localized
                    WHERE QUESTION_ID = q.ID AND LOCALE = ?
                ))
                """;

            qm = this.questionStore.select()
                    .sql(query)
                    .param(QuestionLocalizedStore.locale(locale.getLanguage()))
                    .param(QuestionLocalizedStore.locale(locale.getLanguage()))
                    .param(QuestionStore.id(id))
                    .param(QuestionLocalizedStore.locale(locale.getLanguage()))
                    .param(QuestionLocalizedStore.locale(locale.getLanguage()))
                    .optional();

        }

        if (qm.isPresent()) {
            Optional<Question> question = qm.map(this::getQuestion);
            if ((question.get().getType()
                    .equals(QuestionType.CHOOSE_THE_BEST)
                    || question.get().getType()
                    .equals(QuestionType.MULTI_CHOICE))) {
                question.get().setChoices(
                        listQuestionChoice(true,
                                question.get().getId(), locale));
            }
            return question;
        }

        return Optional.empty();
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
            final Question question) throws SQLException {
        question.setType(type);
        Set<ConstraintViolation<Question>> violations =
                getViolations(question);
        if (violations.isEmpty()) {
            final String query = locale == null
                    ? """
                    UPDATE question
                    SET question = ?, explanation = ?, answer = ?,
                    modified_at = CURRENT_TIMESTAMP
                    WHERE id = ? AND type = ?
                    """
                    : """
                    UPDATE question
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
                        UPDATE QUESTION_LOCALIZED SET question = ?,
                        explanation = ?
                            WHERE question_id = ? AND
                                    locale = ? AND
                                question_id IN
                                    ( SELECT id from question
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
                            INSERT INTO QUESTION_LOCALIZED
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
                        .map(QuestionChoice::getId)
                        .collect(Collectors.toList());

                if (!availableIds.isEmpty()) {
                    final String deletequestionChoice =
                            "DELETE FROM question_choice "
                                    + "WHERE question_id = ? AND id NOT IN ("
                                    + availableIds.stream()
                                    .map(aId -> "?")
                                    .collect(Collectors.joining(","))
                                    + ")";
                    availableIds.add(0, id);
                    jdbcClient.sql(deletequestionChoice).params(availableIds)
                            .update();
                }


                for (QuestionChoice choice : question.getChoices()) {
                    if (choice.getId() == null) {
                        createChoice(choice, locale, id);
                    } else {
                        updateChoice(choice, locale);
                    }
                }

            }
            return updatedRows == 0 ? null : read(id, locale);
        } else {
            throw new ConstraintViolationException(violations);
        }


    }

    private void updateChoice(final QuestionChoice choice,
                              final Locale locale) throws SQLException {
        final String updatequestionChoice = locale == null
                ? """
                UPDATE question_choice
                SET c_value = ?,
                    is_answer = ?
                WHERE id = ?
                """
                : """
                UPDATE question_choice
                SET is_answer = ?
                WHERE id = ?
                """;
        if (locale == null) {
            jdbcClient.sql(updatequestionChoice)
                    .param(INDEX_1, choice.getCValue())
            .param(INDEX_2,
                    choice.getIsAnswer() != null && choice.getIsAnswer())
            .param(INDEX_3, choice.getId());
        } else {
            jdbcClient.sql(updatequestionChoice)
                .param(INDEX_1,
                        choice.getIsAnswer() != null && choice.getIsAnswer())
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
                        DELETE FROM question_localized WHERE question_id = ?
                        """;
        jdbcClient.sql(queryL).param(INDEX_1, id).update();
        String query = "DELETE FROM question WHERE ID=?";

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
                        DELETE FROM question_choice_localized
                        WHERE choice_id IN
                        (SELECT id FROM question_choice WHERE question_id = ?)
                                """;
        jdbcClient.sql(queryL)
                .param(INDEX_1, questionId).update();
        final String query =
                "DELETE FROM question_choice WHERE question_id = ?";
        final Integer updatedRows = jdbcClient.sql(query)
                .param(INDEX_1, questionId).update();
        return !(updatedRows == 0);
    }


    /**
     * List question of exam.
     *
     * @param userName   the user name
     * @param category the category
     * @param locale     the locale
     * @return quetions in given exam
     */
    public List<Question> list(final String userName,
                               final Locale locale,
                               final List<String> category)
            throws SQLException {

        boolean isOwner = true;

        final String query = locale == null
                ? "SELECT id,question,explanation,type,"
                + "created_by, "
                + (isOwner ? "answer" : "NULL")
                + " AS answer,"
                + "created_at,modified_at"
                + " FROM question"
                + " where "
                + "id IN (" + getQuestionIdFilter(category) + ") "
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
                + "type, created_by,"
                + (isOwner ? "q.answer" : "NULL")
                + " AS answer"
                + ",created_at,modified_at FROM "
                + "question q LEFT JOIN question_localized ql ON "
                + "q.ID = ql.QUESTION_ID WHERE"
                + " q.ID IN (" + getQuestionIdFilter(category) + ") "
                + "  AND"
                + " (ql.LOCALE IS NULL "
                + "OR ql.LOCALE = ? OR "
                + "q.ID NOT IN "
                + "(SELECT question_id FROM question_localized "
                + "WHERE QUESTION_ID=q.ID AND LOCALE = ?))";

        List<Object> parameters = new ArrayList<>();
        if (locale == null) {
            parameters.addAll(category);
        } else {
            parameters.add(locale.getLanguage());
            parameters.add(locale.getLanguage());
            parameters.addAll(category);
            parameters.add(locale.getLanguage());
            parameters.add(locale.getLanguage());
        }


        List<Question> questions =
                jdbcClient.sql(query).params(parameters).query(rowMapper)
                        .list();

        if (!questions.isEmpty()) {
            for (Question question : questions) {
                if ((question.getType().equals(QuestionType.CHOOSE_THE_BEST)
                        || question.getType()
                        .equals(QuestionType.MULTI_CHOICE))) {
                    question.setChoices(this
                            .listQuestionChoice(isOwner,
                                    question.getId(), locale));
                }
            }
        }
        return questions;
    }

    private String getQuestionIdFilter(final List<String> category) {
        String builder = "SELECT QUESTION_ID FROM "
                + "question_category WHERE category_id IN ("
                + category.stream().map(tag -> "?")
                .collect(Collectors.joining(","))
                + ") "
                + "GROUP BY QUESTION_ID "
                + "HAVING COUNT(DISTINCT category_id) = "
                + category.size();
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
                created_at,modified_at,answer FROM question
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
                List<QuestionChoice> choices = question.getChoices();
                if (choices == null
                        || choices.size() < 2) {
                    ConstraintViolation<Question> violation
                            = ConstraintViolationImpl.forBeanValidation(
                            messageTemplate, messageParameters,
                            expressionVariables,
                            "Minimum 2 choices",
                            rootBeanClass,
                            question, leafBeanInstance, cValue, propertyPath,
                            constraintDescriptor, elementType);
                    violations.add(violation);
                } else if (choices.stream()
                        .filter(choice -> choice.getIsAnswer() != null
                                && choice.getIsAnswer())
                        .findFirst().isEmpty()) {
                    ConstraintViolation<Question> violation
                            = ConstraintViolationImpl.forBeanValidation(
                            messageTemplate, messageParameters,
                            expressionVariables,
                            "At-least One Answer should be available",
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

        jdbcClient.sql("DELETE FROM QUESTION_LOCALIZED WHERE question_id=?")
                .param(id).update();

        jdbcClient.sql("DELETE FROM QUESTION_CATEGORY WHERE question_id=?")
                .param(id).update();

        int updatedRow =
                jdbcClient.sql("DELETE FROM question WHERE ID=? and type = ?")
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
    private boolean attachCategory(final String userName,
                                     final UUID questionId,
                                     final String categoryId)
            throws SQLException {
        String insertQuery = """
                INSERT INTO question_category(question_id, category_id)
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

            Category category = new Category();
            category.setId(categoryId);
            category.setTitle(categoryId.toUpperCase());

            if (this.categoryService.create(
                    userName, null, category) != null) {
                return attachCategory(userName, questionId, categoryId);
            }
        }

        // DataIntegrityViolationException

        return noOfRowsInserted == 1;
    }

    /**
     * Deletes Questions.
     */
    public void delete() throws SQLException {
        this.questionCategoryStore.delete().execute();
        this.questionTagStore.delete().execute();

        this.questionChoiceLocalizedStore.delete().execute();
        this.questionChoiceStore.delete().execute();

        this.questionLocalizedStore.delete().execute();
        this.questionStore.delete().execute();
    }
}
