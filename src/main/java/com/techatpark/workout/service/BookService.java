package com.techatpark.workout.service;

import com.techatpark.workout.model.Book;
import com.techatpark.workout.model.Question;
import com.techatpark.workout.model.QuestionType;
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
import java.util.UUID;

/**
 * The type Book service.
 */
@Service
public class BookService {

    /**
     * Logger Facade.
     */
    private final Logger logger =
            LoggerFactory.getLogger(BookService.class);

    /**
     * this helps to execute sql queries.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * this is the connection for the database.
     */
    private final DataSource dataSource;

    /**
     * Service for Practices.
     */
    private final QuestionService questionService;


    /**
     * this helps to practiceService.
     */
    private final PracticeService practiceService;

    /**
     * Instantiates a new Book service.
     *
     * @param ajdbcTemplate       a jdbcTemplate
     * @param adataSource         a dataSource
     * @param theQuestionService  the question service
     * @param thePracticeService  the practice service
     */
    public BookService(final JdbcTemplate ajdbcTemplate,
                       final DataSource adataSource,
                       final QuestionService theQuestionService,
                       final PracticeService thePracticeService) {
        this.jdbcTemplate = ajdbcTemplate;
        this.dataSource = adataSource;
        this.questionService = theQuestionService;
        this.practiceService = thePracticeService;
    }

    /**
     * Maps the data from and to the database.
     *
     * @param rs
     * @param rowNum
     * @return p
     * @throws SQLException
     */
    private Book rowMapper(final ResultSet rs,
                           final Integer rowNum)
            throws SQLException {
        Book book = new Book((UUID)
                rs.getObject("id"),
                rs.getString("title"),
                rs.getString("path"),
                rs.getString("description"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getString("created_by"),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getString("modified_by"));

        return book;
    }

    /**
     * creates new book.
     *
     * @param userName the userName
     * @param locale   the locale
     * @param book     the syllabus
     * @return syllabus optional
     */
    public Book create(final String userName,
                       final Locale locale,
                       final Book book) {
        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("books")
                .usingColumns("id", "title",
                        "path", "description", "created_by");

        final Map<String, Object> valueMap = new HashMap<>();

        valueMap.put("title", book.title());
        valueMap.put("path", book.path());
        valueMap.put("description", book.description());
        valueMap.put("created_by", userName);

        final UUID bookId = UUID.randomUUID();
        valueMap.put("id", bookId);
        insert.execute(valueMap);


        if (locale != null) {
            valueMap.put("book_id", bookId);
            valueMap.put("locale", locale.getLanguage());
            createLocalizedBook(valueMap);
        }
        final Optional<Book> createdBooks =
                read(userName, null, bookId);

        logger.info("Book Created {}", bookId);

        return createdBooks.get();
    }

    /**
     * Create Localized book.
     *
     * @param valueMap
     * @return noOfBook
     */
    private int createLocalizedBook(final Map<String, Object> valueMap) {
        return new SimpleJdbcInsert(dataSource)
                .withTableName("books_localized")
                .usingColumns("book_id", "locale", "title", "description")
                .execute(valueMap);
    }

    /**
     * reads from syllabus.
     *
     * @param id       the id
     * @param userName the userName
     * @param locale   the locale
     * @return question optional
     */
    public Optional<Book> read(final String userName,
                               final Locale locale,
                               final UUID id) {
        final String query = locale == null
                ? "SELECT id,title,path,description,created_by,"
                + "created_at, modified_at, modified_by FROM books "
                + "WHERE id = ?"
                : "SELECT DISTINCT b.ID, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.TITLE "
                + "ELSE b.TITLE "
                + "END AS TITLE, "
                + "b.PATH, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.DESCRIPTION "
                + "ELSE b.DESCRIPTION "
                + "END AS DESCRIPTION,"
                + "created_by,created_at, modified_at, modified_by "
                + "FROM BOOKS b "
                + "LEFT JOIN BOOKS_LOCALIZED bl "
                + "ON b.ID = bl.BOOK_ID "
                + "WHERE b.ID = ? "
                + "AND (bl.LOCALE IS NULL "
                + "OR bl.LOCALE = ? OR "
                + "b.ID NOT IN "
                + "(SELECT BOOK_ID FROM BOOKS_LOCALIZED "
                + "WHERE BOOK_ID=b.ID AND LOCALE = ?))";

        try {
            final Book p = locale == null ? jdbcTemplate
                    .queryForObject(query, this::rowMapper, id)
                    : jdbcTemplate
                    .queryForObject(query, this::rowMapper,
                                    locale.getLanguage(),
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
     * update the books.
     *
     * @param id       the id
     * @param userName the userName
     * @param locale   the locale
     * @param book     the books
     * @return question optional
     */
    public Book update(final UUID id,
                       final String userName,
                       final Locale locale,
                       final Book book) {
        logger.debug("Entering update for Book {}", id);
        final String query = locale == null
                ? "UPDATE books SET title=?,path=?,"
                + "description=?,modified_by=? WHERE id=?"
                : "UPDATE books SET modified_by=? WHERE id=?";
        Integer updatedRows = locale == null
                ? jdbcTemplate.update(query, book.title(),
                book.path(), book.description(), userName, id)
                : jdbcTemplate.update(query, userName, id);
        if (updatedRows == 0) {
            logger.error("Update not found", id);
            throw new IllegalArgumentException("Book not found");
        } else if (locale != null) {
            updatedRows = jdbcTemplate.update(
                    "UPDATE books_localized SET title=?,locale=?,"
                            + "description=? WHERE book_id=? AND locale=?",
                    book.title(), locale.getLanguage(),
                    book.description(), id, locale.getLanguage());
            if (updatedRows == 0) {
                final Map<String, Object> valueMap = new HashMap<>(4);
                valueMap.put("book_id", id);
                valueMap.put("locale", locale.getLanguage());
                valueMap.put("title", book.title());
                valueMap.put("description", book.description());
                createLocalizedBook(valueMap);
            }
        }
        return read(userName, locale, id).get();
    }

    /**
     * delete the book.
     *
     * @param id       the id
     * @param userName the userName
     * @return question optional
     */
    public Boolean delete(final String userName, final UUID id) {
        final String query = "DELETE FROM books WHERE id = ?";
        final Integer updatedRows = jdbcTemplate.update(query, id);
        return !(updatedRows == 0);
    }

    /**
     * list the book.
     *
     * @param userName the userName
     * @param locale   the locale
     * @return question optional
     */
    public List<Book> list(final String userName,
                           final Locale locale) {
        final String query = locale == null
                ? "SELECT id,title,path,description,created_by,"
                + "created_at, modified_at, modified_by FROM books"
                : "SELECT DISTINCT b.ID, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.TITLE "
                + "ELSE b.TITLE "
                + "END AS TITLE, "
                + "b.PATH, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.DESCRIPTION "
                + "ELSE b.DESCRIPTION "
                + "END AS DESCRIPTION,"
                + "created_by,created_at, modified_at, modified_by "
                + "FROM BOOKS b "
                + "LEFT JOIN BOOKS_LOCALIZED bl "
                + "ON b.ID = bl.BOOK_ID "
                + "WHERE bl.LOCALE IS NULL "
                + "OR bl.LOCALE = ? OR "
                + "b.ID NOT IN "
                + "(SELECT BOOK_ID FROM BOOKS_LOCALIZED "
                + "WHERE BOOK_ID=b.ID AND LOCALE = ?)";
        return locale == null
                ? jdbcTemplate.query(query, this::rowMapper)
                : jdbcTemplate
                .query(query, this::rowMapper,
                                locale.getLanguage(),
                                locale.getLanguage(),
                                locale.getLanguage(),
                                locale.getLanguage());

    }


    /**
     * list the subject by grade and board.
     *
     * @param userName  the userName
     * @param locale    the locale
     * @param boardId   the grade
     * @param gradeId   the grade
     * @param subjectId the syllabusId
     * @return books optional
     */
    public List<Book> list(final String userName,
                           final Locale locale,
                           final UUID boardId,
                           final UUID gradeId,
                           final UUID subjectId) {
        final String query = locale == null
                ? "SELECT id,title,path,description,created_by,"
                + "created_at,modified_at,modified_by FROM books "
                + "JOIN boards_grades_subjects_books ON books.id "
                + "= boards_grades_subjects_books.book_id "
                + " where boards_grades_subjects_books.grade_id = ? "
                + "AND "
                + " boards_grades_subjects_books.board_id = ? "
                + "AND "
                + " boards_grades_subjects_books.book_id = ? "
                : "SELECT DISTINCT s.ID, "
                + "CASE WHEN sl.LOCALE = ? "
                + "THEN sl.TITLE "
                + "ELSE s.TITLE "
                + "END AS TITLE, "
                + "s.path, "
                + "CASE WHEN sl.LOCALE = ? "
                + "THEN sl.DESCRIPTION "
                + "ELSE s.DESCRIPTION "
                + "END AS DESCRIPTION,"
                + "created_by,created_at, modified_at, modified_by "
                + "FROM books s "
                + "LEFT JOIN BOOKS_LOCALIZED sl "
                + "ON s.id = sl.book_id "
                + "LEFT JOIN boards_grades_subjects_books bgs "
                + "ON s.id = bgs.book_id where bgs.grade_id = ? "
                + "AND bgs.board_id = ? "
                + "AND bgs.subject_id = ? ";
        return locale == null
                ? jdbcTemplate.query(query, this::rowMapper,
                gradeId, boardId, subjectId)
                : jdbcTemplate
                .query(query, this::rowMapper,
                                locale.getLanguage(),
                                locale.getLanguage(),
                                gradeId,
                                boardId,
                                subjectId);
    }

    /**
     * Cleaning up all books.
     */
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM boards_grades_subjects_books");
        jdbcTemplate.update("DELETE FROM books_localized");
        jdbcTemplate.update("DELETE FROM books");

    }

    /**
     * Read annotation optional.
     *
     * @param userName the username
     * @param locale   the locale
     * @param categories the categoriesPath
     * @return the optional
     */
    public List<Question> listAllQuestions(final String userName,
                                           final Locale locale,
                                           final List<String> categories) {
        return questionService.list(userName, locale, categories);

    }

    /**
     * create the question.
     *
     * @param questionType the questionType
     * @param question     question
     * @param categories         categories
     * @param tags
     * @param locale       the locale
     * @param createdBy    createdBy
     * @return successflag boolean
     */
    public Optional<Question> createAQuestion(
                                              final QuestionType questionType,
                                              final Locale locale,
                                              final String createdBy,
                                              final Question question,
                                              final List<String> categories,
                                              final List<String> tags) {


        return questionService.create(categories, tags,
                questionType, locale,
                createdBy, question);
    }

    //create a function to delete, it must done inside question service

    /**
     * delete the question.
     *
     * @param id           the id
     * @param questionType the questionType
     * @return successflag boolean
     */
    public Boolean deleteAQuestion(final UUID id,
                                   final QuestionType questionType) {

        return questionService.deleteAQuestion(id, questionType);
    }

    /**
     * delete the questions of a book.
     *
     * @param bookPath the id
     * @return successflag boolean
     */
    public Boolean deleteQuestionBank(final String bookPath) {

        return practiceService.deleteQuestionBank(bookPath);
    }

    /**
     * update the question.
     *
     * @param id           the id
     * @param questionType the questionType
     * @param question     question
     * @param locale       the locale
     * @return successflag boolean
     */
    public Optional<Question> updateQuestion(final UUID id,
                                             final Locale locale,
                                             final QuestionType questionType,
                                             final Question question) {
        return questionService.update(questionType, id, locale,
                question);
    }

    //learner create method

    /**
     * @param bookName
     * @param createdBy
     * @param chapterPath
     * @return 0
     */
    public Object learner(final String bookName, final String createdBy,
                          final String chapterPath) {
        return 0;
    }
}
