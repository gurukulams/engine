package com.techatpark.workout.service;

import com.techatpark.workout.model.Board;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@SpringBootTest
public class BoardServiceTest {

    public static final String STATE_BOARD_IN_ENGLISH = "State Board";
    public static final String STATE_BOARD_DESCRIPTION_IN_ENGLISH = "State Board Description";
    public static final String STATE_BOARD_TITLE_IN_FRENCH = "Conseil d'État";
    public static final String STATE_BOARD_DESCRIPTION_IN_FRENCH = "Description du conseil d'État";
    @Autowired
    private BoardService boardService;

    /**
     * Before.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void before() throws IOException {
        cleanUp();
    }

    /**
     * After.
     */
    @AfterEach
    void after() {
        cleanUp();
    }

    private void cleanUp() {
        boardService.deleteAll();
    }

    @Test
    void create() {
        final Board board = boardService.create("mani", null,
                anBoard());
        Assertions.assertTrue(boardService.read("mani", null, board.id()).isPresent(),
                "Created Board");
    }

    @Test
    void read() {
        final Board board = boardService.create("mani", null,
                anBoard());
        final UUID newBoardId = board.id();
        Assertions.assertTrue(boardService.read("mani", null, newBoardId).isPresent(),
                "Board Created");
    }

    @Test
    void update() {

        final Board board = boardService.create("mani", null,
                anBoard());
        final UUID newBoardId = board.id();
        Board newBoard = new Board(null, "Board", "A " +
                "Board", null, "tom", null, null);
        Board updatedBoard = boardService
                .update(newBoardId, "mani", null, newBoard);
        Assertions.assertEquals("Board", updatedBoard.title(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            boardService
                    .update(UUID.randomUUID(), "mani", null, newBoard);
        });
    }

    @Test
    void delete() {

        final Board board = boardService.create("mani", null,
                anBoard());
        boardService.delete("mani", board.id());
        Assertions.assertFalse(boardService.read("mani", null, board.id()).isPresent(),
                "Deleted Board");

    }

    @Test
    void list() {

        final Board board = boardService.create("mani", null,
                anBoard());
        Board newBoard = new Board(null, "Board New", "A " +
                "Board", null, "tom", null, null);
        boardService.create("mani", null,
                newBoard);
        List<Board> listofboard = boardService.list("manikanta", null);
        Assertions.assertEquals(2, listofboard.size());

    }

    @Test
    void testLocalizationFromDefaultWithoutLocale() {
        // Create a Board without locale
        final Board board = boardService.create("mani", null,
                anBoard());

        testLocalization(board);

    }

    @Test
    void testLocalizationFromCreateWithLocale() {
        // Create a Board with locale
        final Board board = boardService.create("mani", Locale.GERMAN,
                anBoard());

        testLocalization(board);

    }

    void testLocalization(Board board) {

        // Update for China Language
        boardService.update(board.id(), "mani", Locale.FRENCH, anBoard(board,
                STATE_BOARD_TITLE_IN_FRENCH,
                STATE_BOARD_DESCRIPTION_IN_FRENCH));

        // Get for french Language
        Board createBoard = boardService.read("mani", Locale.FRENCH,
                board.id()).get();
        Assertions.assertEquals(STATE_BOARD_TITLE_IN_FRENCH, createBoard.title());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_FRENCH, createBoard.description());

        final UUID id = createBoard.id();
        createBoard = boardService.list("mani", Locale.FRENCH)
                .stream()
                .filter(board1 -> board1.id().equals(id))
                .findFirst().get();
        Assertions.assertEquals(STATE_BOARD_TITLE_IN_FRENCH, createBoard.title());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_FRENCH,
                createBoard.description());

        // Get for France which does not have data
        createBoard = boardService.read("mani", Locale.CHINESE,
                board.id()).get();
        Assertions.assertEquals(STATE_BOARD_IN_ENGLISH, createBoard.title());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_ENGLISH, createBoard.description());

        createBoard = boardService.list("mani", Locale.CHINESE)
                .stream()
                .filter(board1 -> board1.id().equals(id))
                .findFirst().get();

        Assertions.assertEquals(STATE_BOARD_IN_ENGLISH, createBoard.title());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_ENGLISH, createBoard.description());

    }

    /**
     * Gets board.
     *
     * @return the board
     */
    Board anBoard() {
        Board board = new Board(null, STATE_BOARD_IN_ENGLISH,
                STATE_BOARD_DESCRIPTION_IN_ENGLISH, null, null,
                null, null);
        return board;
    }

    /**
     * Gets board from reference board.
     *
     * @return the board
     */
    Board anBoard(final Board ref, final String title, final String description) {
        return new Board(ref.id(), title,
                description, ref.created_at(), ref.created_by(),
                ref.modified_at(), ref.modified_by());
    }
}
