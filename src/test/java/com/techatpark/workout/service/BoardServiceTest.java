package com.techatpark.workout.service;

import com.gurukulams.core.model.Boards;
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

    public static final String STATE_BOARD_IN_ENGLISH = "State Boards";
    public static final String STATE_BOARD_DESCRIPTION_IN_ENGLISH = "State Boards Description";
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
        final Boards board = boardService.create("mani", null,
                anBoard());
        Assertions.assertTrue(boardService.read("mani", null, board.getId()).isPresent(),
                "Created Boards");
    }

    @Test
    void read() {
        final Boards board = boardService.create("mani", null,
                anBoard());
        final UUID newBoardId = board.getId();
        Assertions.assertTrue(boardService.read("mani", null, newBoardId).isPresent(),
                "Boards Created");
    }

    @Test
    void update() {

        final Boards board = boardService.create("mani", null,
                anBoard());
        final UUID newBoardId = board.getId();
        Boards newBoard = new Boards();
        newBoard.setTitle("Boards");
        newBoard.setDescription("A Boards");
        newBoard.setCreatedBy("tom");
        Boards updatedBoard = boardService
                .update(newBoardId, "mani", null, newBoard);
        Assertions.assertEquals("Boards", updatedBoard.getTitle(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            boardService
                    .update(UUID.randomUUID(), "mani", null, newBoard);
        });
    }

    @Test
    void delete() {

        final Boards board = boardService.create("mani", null,
                anBoard());
        boardService.delete("mani", board.getId());
        Assertions.assertFalse(boardService.read("mani", null, board.getId()).isPresent(),
                "Deleted Boards");

    }

    @Test
    void list() {

        final Boards board = boardService.create("mani", null,
                anBoard());
        Boards newBoard = new Boards();
        newBoard.setTitle("Boards New");
        newBoard.setDescription("A Boards");
        newBoard.setCreatedBy("tom");
        boardService.create("mani", null,
                newBoard);
        List<Boards> listofboard = boardService.list("manikanta", null);
        Assertions.assertEquals(2, listofboard.size());

    }

    @Test
    void testLocalizationFromDefaultWithoutLocale() {
        // Create a Boards without locale
        final Boards board = boardService.create("mani", null,
                anBoard());

        testLocalization(board);

    }

    @Test
    void testLocalizationFromCreateWithLocale() {
        // Create a Boards with locale
        final Boards board = boardService.create("mani", Locale.GERMAN,
                anBoard());

        testLocalization(board);

    }

    void testLocalization(Boards board) {

        // Update for China Language
        boardService.update(board.getId(), "mani", Locale.FRENCH, anBoard(board,
                STATE_BOARD_TITLE_IN_FRENCH,
                STATE_BOARD_DESCRIPTION_IN_FRENCH));

        // Get for french Language
        Boards createBoard = boardService.read("mani", Locale.FRENCH,
                board.getId()).get();
        Assertions.assertEquals(STATE_BOARD_TITLE_IN_FRENCH, createBoard.getTitle());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_FRENCH, createBoard.getDescription());

        final UUID id = createBoard.getId();
        createBoard = boardService.list("mani", Locale.FRENCH)
                .stream()
                .filter(board1 -> board1.getId().equals(id))
                .findFirst().get();
        Assertions.assertEquals(STATE_BOARD_TITLE_IN_FRENCH, createBoard.getTitle());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_FRENCH,
                createBoard.getDescription());

        // Get for France which does not have data
        createBoard = boardService.read("mani", Locale.CHINESE,
                board.getId()).get();
        Assertions.assertEquals(STATE_BOARD_IN_ENGLISH, createBoard.getTitle());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_ENGLISH, createBoard.getDescription());

        createBoard = boardService.list("mani", Locale.CHINESE)
                .stream()
                .filter(board1 -> board1.getId().equals(id))
                .findFirst().get();

        Assertions.assertEquals(STATE_BOARD_IN_ENGLISH, createBoard.getTitle());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_ENGLISH, createBoard.getDescription());

    }

    /**
     * Gets board.
     *
     * @return the board
     */
    Boards anBoard() {
        Boards board = new Boards();
        board.setTitle(STATE_BOARD_IN_ENGLISH);
        board.setDescription(STATE_BOARD_DESCRIPTION_IN_ENGLISH);
        return board;
    }

    /**
     * Gets board from reference board.
     *
     * @return the board
     */
    Boards anBoard(final Boards ref, final String title, final String description) {
        
        Boards board = new Boards();
        board.setId(ref.getId());
        board.setTitle(title);
        board.setDescription(description);
        board.setCreatedAt(ref.getCreatedAt()); 
        board.setCreatedBy(ref.getCreatedBy());
        board.setModifiedAt(ref.getModifiedAt());
        board.setModifiedBy(ref.getModifiedBy());
        return board;
    }
}
