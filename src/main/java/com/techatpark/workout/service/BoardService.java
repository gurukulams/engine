package com.techatpark.workout.service;

import com.gurukulams.core.model.Boards;
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


@Service
public class BoardService {

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
     * Logger Facade.
     */
    private final Logger logger =
            LoggerFactory.getLogger(BoardService.class);

    /**
     * this helps to execute sql queries.
     */
    private final JdbcClient jdbcClient;


    /**
     * this is the constructor.
     *
     * @param ajdbcClient jdbcClient
     */
    public BoardService(final JdbcClient ajdbcClient) {
        this.jdbcClient = ajdbcClient;
    }

    /**
     * Maps the data from and to the database.
     *
     * @param rs
     * @param rowNum
     * @return p
     * @throws SQLException
     */
    private Boards rowMapper(final ResultSet rs, final Integer rowNum) throws SQLException {
        Boards boards = new Boards();
        boards.setId((UUID) rs.getObject(INDEX_1));
        boards.setTitle(rs.getString(INDEX_2));
        boards.setDescription(rs.getString(INDEX_3));
        boards.setCreatedAt(rs.getObject(INDEX_4, LocalDateTime.class));
        boards.setCreatedBy(rs.getString(INDEX_5));
        boards.setModifiedAt(rs.getObject(INDEX_6, LocalDateTime.class));
        boards.setModifiedBy(rs.getString(INDEX_7));
return  boards;
    }

    /**
     * creates new syllabus.
     *
     * @param userName the userName
     * @param board    the syllabus
     * @param locale   the locale
     * @return board optional
     */
    public Boards create(final String userName,
                         final Locale locale,
                         final Boards board) {

        final UUID boardId = UUID.randomUUID();

        String sql =
                "INSERT INTO boards(id, title, description, "
                        + "created_by) values(?,?,?,?)";
        jdbcClient.sql(sql)
                .param(INDEX_1, boardId)
                .param(INDEX_2, board.getTitle())
                .param(INDEX_3, board.getDescription())
                .param(INDEX_4, userName)
                .update();

        if (locale != null) {
            createLocalizedBoard(boardId, board, locale);
        }

        final Optional<Boards> createdBoard =
                read(userName, locale, boardId);

        logger.info("Boards Created {}", boardId);

        return createdBoard.get();
    }

    /**
     * Create Localized Boards.
     *
     * @param board
     * @param locale
     * @param boardId
     * @return noOfBoards
     */
    private int createLocalizedBoard(final UUID boardId,
                                     final Boards board,
                                     final Locale locale) {
        String sql =
                "INSERT INTO boards_localized(board_id, locale, title, "
                        + "description) values(?, ?, ?, ?)";
        return jdbcClient.sql(sql)
                .param(INDEX_1, boardId)
                .param(INDEX_2, locale.getLanguage())
                .param(INDEX_3, board.getTitle())
                .param(INDEX_4, board.getDescription())
                .update();
    }

    /**
     * reads from syllabus.
     *
     * @param id       the id
     * @param locale   the locale
     * @param userName the userName
     * @return board optional
     */
    public Optional<Boards> read(final String userName,
                                final Locale locale, final UUID id) {

        final String query = locale == null
                ? "SELECT id,title,description,created_at,"
                + "created_by, modified_at, modified_by FROM boards "
                + "WHERE id = ?"
                : """
                SELECT
                    b.id,
                    COALESCE(bl.title, b.title) AS title,
                    COALESCE(bl.description, b.description) AS description,
                    b.created_at,
                    b.created_by,
                    b.modified_at,
                    b.modified_by
                 FROM
                    boards b
                 LEFT JOIN
                    boards_localized bl
                 ON
                    b.id = bl.board_id
                    AND bl.locale = ?
                WHERE
                    b.id = ?
                 """;


            return locale == null
                    ? jdbcClient.sql(query).param(INDEX_1, id)
                    .query(this::rowMapper).optional()
                    : jdbcClient.sql(query)
                    .param(INDEX_1, locale.getLanguage())
                    .param(INDEX_2, id)
                    .query(this::rowMapper).optional();

    }

    /**
     * update the board.
     *
     * @param id       the id
     * @param userName the userName
     * @param board    the board
     * @param locale   the locale
     * @return board optional
     */
    public Boards update(final UUID id,
                        final String userName,
                        final Locale locale,
                        final Boards board) {
        logger.debug("Entering update for Boards {}", id);
        final String query = locale == null
                ? "UPDATE boards SET title=?,"
                + "description=?,modified_by=? WHERE id=?"
                : "UPDATE boards SET modified_by=? WHERE id=?";
        List params = locale == null ? List.of(
                board.getTitle(), board.getDescription(), userName, id)
                : List.of(userName, id);
        int updatedRows = jdbcClient.sql(query).params(params).update();
        if (updatedRows == 0) {
            logger.error("Update not found {}", id);
            throw new IllegalArgumentException("Boards not found");
        } else if (locale != null) {
            updatedRows = jdbcClient.sql(
                            "UPDATE boards_localized SET title=?,locale=?,"
                                    + "description=? WHERE board_id=? AND "
                                    + "locale=?")
                    .params(List.of(board.getTitle(), locale.getLanguage(),
                            board.getDescription(), id, locale.getLanguage()))
                    .update();
            if (updatedRows == 0) {
                createLocalizedBoard(id, board, locale);
            }
        }
        return read(userName, locale, id).get();
    }

    /**
     * delete the board.
     *
     * @param id       the id
     * @param userName the userName
     * @return board optional
     */
    public Boolean delete(final String userName, final UUID id) {
        return jdbcClient.sql("DELETE FROM boards WHERE id = ?")
                .param(INDEX_1, id).update() != 0;
    }

    /**
     * list the board.
     *
     * @param userName the userName
     * @param locale   the locale
     * @return board optional
     */
    public List<Boards> list(final String userName,
                            final Locale locale) {
        final String query = locale == null
                ? "SELECT id,title,description,created_at,"
                + "created_by, modified_at, modified_by FROM boards "
                : """
                SELECT
                   b.id,
                   COALESCE(bl.title, b.title) AS title,
                   COALESCE(bl.description, b.description) AS description,
                   b.created_at,
                   b.created_by,
                   b.modified_at,
                   b.modified_by
                FROM
                   boards b
                LEFT JOIN
                   boards_localized bl
                ON
                   b.id = bl.board_id
                   AND bl.locale = ?
                   """;
        return locale == null
                ? jdbcClient.sql(query).query(this::rowMapper).list()
                : jdbcClient.sql(query)
                .param(INDEX_1, locale.getLanguage())
                .query(this::rowMapper).list();

    }

    /**
     * Adds grade to board.
     *
     * @param userName the userName
     * @param boardId  the boardId
     * @param gradeId  the gradeId
     * @return grade optional
     */
    public boolean attachGrade(final String userName, final UUID boardId,
                               final UUID gradeId) {
        String sql =
                "INSERT INTO boards_grades(board_id, grade_id)"
                        + "values(?, ?)";

        return jdbcClient.sql(sql)
                .param(INDEX_1, boardId)
                .param(INDEX_2, gradeId)
                .update() == INDEX_1;
    }

    /**
     * Adds subject to grade and board.
     *
     * @param userName  the userName
     * @param boardId   the gradeId
     * @param gradeId   the gradeId
     * @param subjectId the syllabusId
     * @return grade optional
     */
    public boolean attachSubject(final String userName,
                                 final UUID boardId,
                                 final UUID gradeId,
                                 final UUID subjectId) {
        String sql =
                "INSERT INTO boards_grades_subjects(board_id, grade_id,"
                        + " subject_id) values(?,?,?)";

        return jdbcClient.sql(sql)
                .param(INDEX_1, boardId)
                .param(INDEX_2, gradeId)
                .param(INDEX_3, subjectId)
                .update() == INDEX_1;
    }

    /**
     * Adds book to grade, board and subject.
     *
     * @param userName  the userName
     * @param boardId   the gradeId
     * @param gradeId   the gradeId
     * @param subjectId the syllabusId
     * @param bookId    the bookId
     * @return grade optional
     */
    public boolean attachBook(final String userName,
                              final UUID boardId,
                              final UUID gradeId,
                              final UUID subjectId,
                              final UUID bookId) {
        String sql =
                "INSERT INTO boards_grades_subjects_books(board_id, grade_id,"
                        + " subject_id, "
                        + "book_id) values(?,?,?,?)";

        return jdbcClient.sql(sql)
                .param(INDEX_1, boardId)
                .param(INDEX_2, gradeId)
                .param(INDEX_3, subjectId)
                .param(INDEX_4, bookId)
                .update() == INDEX_1;
    }

    /**
     * Cleaning up all boards.
     */
    public void deleteAll() {
        jdbcClient.sql("DELETE FROM boards_grades_subjects_books").update();
        jdbcClient.sql("DELETE FROM boards_grades_subjects").update();
        jdbcClient.sql("DELETE FROM boards_grades").update();
        jdbcClient.sql("DELETE FROM boards_localized").update();
        jdbcClient.sql("DELETE FROM boards").update();
    }
}
