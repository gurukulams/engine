package com.techatpark.workout.service;

import com.techatpark.workout.model.Book;
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
import java.util.UUID;

/**
 * The type Book service.
 */
@Service
public class BookService {

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
     * Index Number.
     */
    private static final int INDEX_7 = 7;
    /**
     * Index Number.
     */
    private static final int INDEX_8 = 8;

    /**
     * Logger Facade.
     */
    private final Logger logger =
            LoggerFactory.getLogger(BookService.class);

    /**
     * JdbcClient instance.
     */
    private final JdbcClient jdbcClient;

    /**
     * Instantiates a new Book service.
     *
     * @param aJdbcClient        aJdbcClient
     */
    public BookService(final JdbcClient aJdbcClient) {
        this.jdbcClient = aJdbcClient;
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
                rs.getObject(INDEX_1),
                rs.getString(INDEX_2),
                rs.getString(INDEX_3),
                rs.getString(INDEX_4),
                rs.getObject(INDEX_5, LocalDateTime.class),
                rs.getString(INDEX_6),
                rs.getObject(INDEX_7, LocalDateTime.class),
                rs.getString(INDEX_8));

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
        String sql =
                """
                        INSERT INTO
                           books(id, title, path, description, created_by)
                        VALUES
                           (
                               ? , ? , ? , ?, ?
                           )
                        """;
        final UUID bookId = UUID.randomUUID();
        jdbcClient.sql(sql)
                .param(INDEX_1, bookId)
                .param(INDEX_2, book.title())
                .param(INDEX_3, book.path())
                .param(INDEX_4, book.description())
                .param(INDEX_5, userName)
                .update();


        if (locale != null) {
            createLocalizedBook(bookId, book, locale);
        }
        final Optional<Book> createdBooks =
                read(userName, null, bookId);

        logger.info("Book Created {}", bookId);

        return createdBooks.get();
    }

    /**
     * Create Localized book.
     *
     * @param bookId
     * @param book
     * @param locale
     * @return noOfBook
     */
    private int createLocalizedBook(final UUID bookId,
                                    final Book book,
                                    final Locale locale) {
        String sql =
                """
                        INSERT INTO
                           books_localized( book_id, locale, title, description)
                        VALUES
                           (
                               ? , ? , ? , ?
                           )
                        """;
        return jdbcClient.sql(sql)
                .param(INDEX_1, bookId)
                .param(INDEX_2, locale.getLanguage())
                .param(INDEX_3, book.title())
                .param(INDEX_4, book.description())
                .update();
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
                ? """
                SELECT id, title, path, description, created_at, created_by,
                modified_at, modified_by
                FROM books
                WHERE id = ?"""
                : """
                SELECT DISTINCT b.ID,
                    CASE WHEN bl.LOCALE = ?
                        THEN bl.TITLE
                        ELSE b.TITLE
                    END AS TITLE,
                    b.PATH,
                    CASE WHEN bl.LOCALE = ?
                        THEN bl.DESCRIPTION
                        ELSE b.DESCRIPTION
                    END AS DESCRIPTION,
                    created_at, created_by, modified_at, modified_by
                FROM BOOKS b
                LEFT JOIN BOOKS_LOCALIZED bl ON b.ID = bl.BOOK_ID
                WHERE b.ID = ?
                    AND (bl.LOCALE IS NULL
                    OR bl.LOCALE = ?
                    OR b.ID NOT IN (
                        SELECT BOOK_ID
                        FROM BOOKS_LOCALIZED
                        WHERE BOOK_ID = b.ID
                            AND LOCALE = ?
                    ))""";


            return locale == null ? jdbcClient
                    .sql(query).param(INDEX_1, id).query(this::rowMapper)
                    .optional()
                    : jdbcClient.sql(query)
                    .param(INDEX_1, locale.getLanguage())
                    .param(INDEX_2, locale.getLanguage())
                    .param(INDEX_3, id)
                    .param(INDEX_4, locale.getLanguage())
                    .param(INDEX_5, locale.getLanguage())
                    .query(this::rowMapper).optional();

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
                ?
                """
                        UPDATE
                           books
                        SET
                           title =? , path =? , description =? , modified_by =?
                        WHERE
                           id =?
                        """
                : "UPDATE books SET modified_by=? WHERE id=?";
        Integer updatedRows = locale == null
                ? jdbcClient.sql(query)
                .param(INDEX_1, book.title())
                .param(INDEX_2, book.path())
                .param(INDEX_3, book.description())
                .param(INDEX_4, userName)
                .param(INDEX_5, id).update()
                : jdbcClient.sql(query)
                .param(INDEX_1, userName)
                .param(INDEX_2, id)
                .update();
        if (updatedRows == 0) {
            logger.error("Update not found", id);
            throw new IllegalArgumentException("Book not found");
        } else if (locale != null) {
            updatedRows = jdbcClient.sql(
                            """
                                    UPDATE
                                       books_localized
                                    SET
                                       title =? , locale =? , description = ?
                                    WHERE
                                       book_id =?
                                       AND locale =?
                                    """
                    )
                    .param(INDEX_1, book.title())
                    .param(INDEX_2, locale.getLanguage())
                    .param(INDEX_3, book.description())
                    .param(INDEX_4, id)
                    .param(INDEX_5, locale.getLanguage())
                    .update();
            if (updatedRows == 0) {
                createLocalizedBook(id, book, locale);
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
        return jdbcClient.sql(query).param(INDEX_1, id)
                .update() != 0;
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
                ?
                """
                        SELECT id,title,path,description,created_at,
                            created_by, modified_at, modified_by FROM books
                        """
                :
                """
                        SELECT DISTINCT b.ID,
                            CASE WHEN bl.LOCALE = ?
                                THEN bl.TITLE
                                ELSE b.TITLE
                            END AS TITLE,
                            b.PATH,
                            CASE WHEN bl.LOCALE = ?
                                THEN bl.DESCRIPTION
                                ELSE b.DESCRIPTION
                            END AS DESCRIPTION,
                            created_at, created_by, modified_at, modified_by
                        FROM BOOKS b
                        LEFT JOIN BOOKS_LOCALIZED bl ON b.ID = bl.BOOK_ID
                        WHERE bl.LOCALE IS NULL
                            OR bl.LOCALE = ?
                            OR b.ID NOT IN (
                                SELECT BOOK_ID
                                FROM BOOKS_LOCALIZED
                                WHERE BOOK_ID = b.ID
                                    AND LOCALE = ?
                            )
                        """;
        return locale == null
                ? jdbcClient.sql(query).query(this::rowMapper).list()
                : jdbcClient.sql(query)
                .param(INDEX_1, locale.getLanguage())
                .param(INDEX_2, locale.getLanguage())
                .param(INDEX_3, locale.getLanguage())
                .param(INDEX_4, locale.getLanguage())
                .query(this::rowMapper).list();

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
                ?
                """
                        SELECT id,
                               title,
                               path,
                               description,
                               created_by,
                               created_at,
                               modified_at,
                               modified_by
                        FROM   books
                               JOIN boards_grades_subjects_books
                                 ON books.id =
                                 boards_grades_subjects_books.book_id
                        WHERE  boards_grades_subjects_books.grade_id = ?
                               AND boards_grades_subjects_books.board_id = ?
                               AND boards_grades_subjects_books.book_id = ?
                        """
                :
                """
                        SELECT DISTINCT s.id,
                                        CASE
                                          WHEN sl.locale = ? THEN sl.title
                                          ELSE s.title
                                        END AS TITLE,
                                        s.path,
                                        CASE
                                          WHEN sl.locale = ? THEN sl.description
                                          ELSE s.description
                                        END AS DESCRIPTION,
                                        created_by,
                                        created_at,
                                        modified_at,
                                        modified_by
                        FROM   books s
                               LEFT JOIN books_localized sl
                                      ON s.id = sl.book_id
                               LEFT JOIN boards_grades_subjects_books bgs
                                      ON s.id = bgs.book_id
                        WHERE  bgs.grade_id = ?
                               AND bgs.board_id = ?
                               AND bgs.subject_id = ?
                        """;
        return locale == null
                ? jdbcClient.sql(query)
                .param(INDEX_1, gradeId)
                .param(INDEX_2, boardId)
                .param(INDEX_3, subjectId)
                .query(this::rowMapper).list()
                : jdbcClient.sql(query)
                .param(INDEX_1, locale.getLanguage())
                .param(INDEX_2, locale.getLanguage())
                .param(INDEX_3, gradeId)
                .param(INDEX_4, boardId)
                .param(INDEX_5, subjectId)
                .query(this::rowMapper).list();
    }

    /**
     * Cleaning up all books.
     */
    public void deleteAll() {
        jdbcClient.sql("DELETE FROM boards_grades_subjects_books").update();
        jdbcClient.sql("DELETE FROM books_localized").update();
        jdbcClient.sql("DELETE FROM books").update();

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
