package com.techatpark.workout.service;

import com.techatpark.workout.model.Board;
import com.techatpark.workout.model.Grade;
import com.techatpark.workout.model.Subject;
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
public class SubjectsServiceTest {

    public static final String STATE_SUBJECT_IN_ENGLISH = "State Board";
    public static final String STATE_SUBJECT_DESCRIPTION_IN_ENGLISH = "State Board Description";
    public static final String STATE_SUBJECT_TITLE_IN_FRENCH = "Conseil d'État";
    public static final String STATE_SUBJECT_DESCRIPTION_IN_FRENCH = "Description du conseil d'État";


    @Autowired
    private SubjectService subjectService;

    @Autowired
    private BoardService boardService;

    @Autowired
    private GradeService gradeService;

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
        gradeService.deleteAll();
        subjectService.deleteAll();
    }

    @Test
    void create() {
        final Subject subject = subjectService.create("mani", null,
                anSubject());
        Assertions.assertTrue(subjectService.read("mani", null, subject.id()).isPresent(),
                "Created Syllabous");
    }

    @Test
    void read() {
        final Subject subject = subjectService.create("mani", null,
                anSubject());
        final UUID newSubjectId = subject.id();
        Assertions.assertTrue(subjectService.read("mani", null, newSubjectId).isPresent(),
                "subject Created");
    }

    @Test
    void update() {

        final Subject subject = subjectService.create("mani", null,
                anSubject());
        final UUID newSubjectId = subject.id();
        Subject newSubject = new Subject(null, "MathsSubject", "An " +
                "Syllabus", null, "tom", null, null);
        Subject updateSubject = subjectService
                .update(newSubjectId, "manikanta", null, newSubject);
        Assertions.assertEquals("MathsSubject", updateSubject.title(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            subjectService
                    .update(UUID.randomUUID(), "manikanta", null, newSubject);
        });
    }

    @Test
    void delete() {

        final Subject subject = subjectService.create("mani", null,
                anSubject());
        subjectService.delete("mani", subject.id());
        Assertions.assertFalse(subjectService.read("mani", null, subject.id()).isPresent(), "Deleted Subject");

    }

    @Test
    void list() {

        final Subject subject = subjectService.create("mani", null,
                anSubject());
        Subject newSubject = new Subject(null, "Physicssubject", "An " +
                "Syllabus", null, "tom", null, null);
        subjectService.create("manikanta", null,
                newSubject);
        List<Subject> listofsyllabus = subjectService.list("manikanta", null);
        Assertions.assertEquals(2, listofsyllabus.size());

    }


    @Test
    void testLocalizationFromDefaultWithoutLocale() {
        // Create a Subject for Default Language
        final Board board = boardService.create("mani", null,
                anBoard());
        final Grade grade = gradeService.create("tom", null, aGrade());
        final Subject subject = subjectService.create("mani", null,
                anSubject());

        testLocalization(subject);

        listbyBoardandgrade(board, grade, subject, null);

    }

    @Test
    void testLocalizationFromCreateWithLocale() {
        // Create a Subject for GERMAN Language
        final Board board = boardService.create("mani", Locale.GERMAN,
                anBoard());
        final Grade grade = gradeService.create("tom", Locale.GERMAN, aGrade());
        final Subject subject = subjectService.create("mani", Locale.GERMAN,
                anSubject());

        testLocalization(subject);

        listbyBoardandgrade(board, grade, subject, Locale.FRENCH);

    }

    void testLocalization(Subject subject) {

        // Update for China Language
        subjectService.update(subject.id(), "mani", Locale.FRENCH, anSubject(subject,
                STATE_SUBJECT_TITLE_IN_FRENCH,
                STATE_SUBJECT_DESCRIPTION_IN_FRENCH));

        // Get for french Language
        Subject createSubject = subjectService.read("mani", Locale.FRENCH,
                subject.id()).get();
        Assertions.assertEquals(STATE_SUBJECT_TITLE_IN_FRENCH, createSubject.title());
        Assertions.assertEquals(STATE_SUBJECT_DESCRIPTION_IN_FRENCH, createSubject.description());

        final UUID id = createSubject.id();
        createSubject = subjectService.list("mani", Locale.FRENCH)
                .stream()
                .filter(subject1 -> subject1.id().equals(id))
                .findFirst().get();
        Assertions.assertEquals(STATE_SUBJECT_TITLE_IN_FRENCH, createSubject.title());
        Assertions.assertEquals(STATE_SUBJECT_DESCRIPTION_IN_FRENCH,
                createSubject.description());

        // Get for France which does not have data
        createSubject = subjectService.read("mani", Locale.CHINESE,
                subject.id()).get();
        Assertions.assertEquals(STATE_SUBJECT_IN_ENGLISH, createSubject.title());
        Assertions.assertEquals(STATE_SUBJECT_DESCRIPTION_IN_ENGLISH, createSubject.description());

        createSubject = subjectService.list("mani", Locale.CHINESE)
                .stream()
                .filter(subject1 -> subject1.id().equals(id))
                .findFirst().get();

        Assertions.assertEquals(STATE_SUBJECT_IN_ENGLISH, createSubject.title());
        Assertions.assertEquals(STATE_SUBJECT_DESCRIPTION_IN_ENGLISH, createSubject.description());

    }

    void listbyBoardandgrade(Board board, Grade grade, Subject subject, Locale locale) {

        Assertions.assertTrue(boardService.attachSubject("tom", board.id(), grade.id(), subject.id()), "Unable to add grade to board");

        final UUID id = subject.id();
        Subject getSubject = subjectService.list("tom", locale, board.id(), grade.id()).stream()
                .filter(subject1 -> subject1.id().equals(id))
                .findFirst().get();

        if (locale == null) {

            Assertions.assertEquals(STATE_SUBJECT_IN_ENGLISH, getSubject.title());
            Assertions.assertEquals(STATE_SUBJECT_DESCRIPTION_IN_ENGLISH, getSubject.description());

        } else {

            Assertions.assertEquals(STATE_SUBJECT_TITLE_IN_FRENCH, getSubject.title());
            Assertions.assertEquals(STATE_SUBJECT_DESCRIPTION_IN_FRENCH, getSubject.description());

        }


    }

    /**
     * Get subject.
     *
     * @return the subject
     */
    Subject anSubject() {

        Subject subject = new Subject(null, STATE_SUBJECT_IN_ENGLISH,
                STATE_SUBJECT_DESCRIPTION_IN_ENGLISH, null, null,
                null, null);
        return subject;
    }

    /**
     * Gets subject from reference subject.
     *
     * @return the subject
     */
    Subject anSubject(final Subject ref, final String title, final String description) {
        return new Subject(ref.id(), title,
                description, ref.created_at(), ref.created_by(),
                ref.modified_at(), ref.modified_by());
    }

    Grade aGrade() {

        Grade grade = new Grade(null, "Student Grade" + new Date().getTime(),
                "A " + "Grade", null, null,
                null, null);
        return grade;
    }


    /**
     * Gets board.
     *
     * @return the board
     */
    Board anBoard() {

        Board board = new Board(null, "State Board" + new Date().getTime(),
                "A " + "Board", null, null,
                null, null);
        return board;
    }


}
