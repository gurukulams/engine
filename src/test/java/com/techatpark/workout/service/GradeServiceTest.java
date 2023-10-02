package com.techatpark.workout.service;

import com.gurukulams.core.model.Boards;
import com.gurukulams.core.model.Grades;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@SpringBootTest
public class GradeServiceTest {

    public static final String STATE_BOARD_IN_ENGLISH = "State Boards";
    public static final String STATE_BOARD_DESCRIPTION_IN_ENGLISH = "State Boards Description";
    public static final String STATE_BOARD_TITLE_IN_FRENCH = "Conseil d'État";
    public static final String STATE_BOARD_DESCRIPTION_IN_FRENCH = "Description du conseil d'État";

    @Autowired
    private GradeService gradeService;

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
        gradeService.deleteAll();
    }

    @Test
    void create() {
        final Boards board = boardService.create("mani", null,
                aBoard());
        final Grades grade = gradeService.create("mani", null,
                aGrade());
        Assertions.assertTrue(gradeService.read("mani", null, grade.getId()).isPresent(),
                "Created Grades");
    }

    @Test
    void read() {
        final Boards board = boardService.create("mani", null,
                aBoard());
        final Grades grade = gradeService.create("mani", null,
                aGrade());
        final UUID newGradeId = grade.getId();
        Assertions.assertTrue(gradeService.read("mani", null,
                        newGradeId).isPresent(),
                "Grades Created");
    }

    @Test
    void update() {
        final Boards board = boardService.create("mani", null,
                aBoard());
        final Grades grade = gradeService.create("mani", null,
                aGrade());
        final UUID newGradeId = grade.getId();
        Grades newGrade = new Grades();
        newGrade.setTitle("Grades");
        newGrade.setDescription( "An Grades");
        Grades updatedGrade = gradeService
                .update(newGradeId, "manikanta", null, newGrade);
        Assertions.assertEquals("Grades", updatedGrade.getTitle(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            gradeService
                    .update(UUID.randomUUID(), "manikanta", null, newGrade);
        });
    }

    @Test
    void delete() {

        final Boards board = boardService.create("mani", null,
                aBoard());
        final Grades grade = gradeService.create("mani", null,
                aGrade());
        gradeService.delete("mani", grade.getId());
        Assertions.assertFalse(gradeService.read("mani", null, grade.getId()).isPresent(),
                "Deleted Grades");

    }

    @Test
    void list() {

        final Boards board = boardService.create("mani", null,
                aBoard());
        final Grades grade = gradeService.create("manikanta", null,
                aGrade());
        Grades newGrade = new Grades();
        newGrade.setTitle("Grades New");
        newGrade.setDescription( "A Grades");
        newGrade.setCreatedBy("tom");
        gradeService.create("manikanta", null,
                newGrade);
        List<Grades> listofgrade = gradeService.list("manikanta", null);
        Assertions.assertEquals(2, listofgrade.size());

    }


    @Test
    void testLocalizationFromDefaultWithoutLocale() {
        // Create a Grades without locale
        final Boards board = boardService.create("mani", null,
                aBoard());
        final Grades grade = gradeService.create("mani", null,
                aGrade());

        testLocalization(grade);
        listByBoard(board, grade, null);

    }

    @Test
    void testLocalizationFromCreateWithLocale() {
        // Create a Grades with locale

        final Boards board = boardService.create("mani", Locale.FRENCH,
                aBoard());

        final Grades grade = gradeService.create("mani", Locale.GERMAN,
                aGrade());

        testLocalization(grade);
        listByBoard(board, grade, Locale.FRENCH);


    }

    void testLocalization(Grades grade) {

        // Update for French Language
        gradeService.update(grade.getId(), "mani", Locale.FRENCH, aGrade(grade,
                STATE_BOARD_TITLE_IN_FRENCH,
                STATE_BOARD_DESCRIPTION_IN_FRENCH));

        // Get for french Language
        Grades createGrade = gradeService.read("mani", Locale.FRENCH,
                grade.getId()).get();
        Assertions.assertEquals(STATE_BOARD_TITLE_IN_FRENCH, createGrade.getTitle());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_FRENCH, createGrade.getDescription());

        final UUID id = createGrade.getId();
        createGrade = gradeService.list("mani", Locale.FRENCH)
                .stream()
                .filter(grade1 -> grade1.getId().equals(id))
                .findFirst().get();
        Assertions.assertEquals(STATE_BOARD_TITLE_IN_FRENCH, createGrade.getTitle());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_FRENCH,
                createGrade.getDescription());

        // Get for Chinese which does not have data
        createGrade = gradeService.read("mani", Locale.CHINESE,
                grade.getId()).get();
        Assertions.assertEquals(STATE_BOARD_IN_ENGLISH, createGrade.getTitle());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_ENGLISH, createGrade.getDescription());

        createGrade = gradeService.list("mani", Locale.CHINESE)
                .stream()
                .filter(grade1 -> grade1.getId().equals(id))
                .findFirst().get();

        Assertions.assertEquals(STATE_BOARD_IN_ENGLISH, createGrade.getTitle());
        Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_ENGLISH, createGrade.getDescription());

    }

    void listByBoard(Boards board, Grades grade, Locale locale) {

        Assertions.assertTrue(boardService.attachGrade("tom", board.getId(), grade.getId()), "Unable to add grade to board");
        final UUID id = grade.getId();
        Grades getGrade = gradeService.list("tom", locale, board.getId()).stream()
                .filter(grade1 -> grade1.getId().equals(id))
                .findFirst().get();

        if (locale == null) {

            Assertions.assertEquals(STATE_BOARD_IN_ENGLISH, getGrade.getTitle());
            Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_ENGLISH, getGrade.getDescription());

        } else {

            Assertions.assertEquals(STATE_BOARD_TITLE_IN_FRENCH, getGrade.getTitle());
            Assertions.assertEquals(STATE_BOARD_DESCRIPTION_IN_FRENCH, getGrade.getDescription());

        }

    }

    /**
     * Gets grade.
     *
     * @return the grade
     */
    Grades aGrade() {

        Grades grade = new Grades();
        grade.setTitle(STATE_BOARD_IN_ENGLISH);
        grade.setDescription(STATE_BOARD_DESCRIPTION_IN_ENGLISH);
        return grade;
    }

    /**
     * Gets board from reference board.
     *
     * @return the board
     */
    Grades aGrade(final Grades ref, final String title, final String description) {
        Grades grade =  new Grades();
        grade.setId(ref.getId());
        grade.setTitle(title);
        grade.setDescription(description);
        grade.setCreatedAt(ref.getCreatedAt());
        grade.setCreatedBy(ref.getCreatedBy());
        grade.setModifiedAt(ref.getModifiedAt());
        grade.setModifiedBy(ref.getModifiedBy());

        return grade;
    }

    /**
     * Gets board.
     *
     * @return the board
     */
    Boards aBoard() {

        Boards board = new Boards();
        board.setTitle("State Boards"+ new Date().getTime());
        board.setDescription("A Boards");
        return board;
    }
}
