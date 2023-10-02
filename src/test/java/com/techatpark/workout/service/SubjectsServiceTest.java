package com.techatpark.workout.service;

import com.gurukulams.core.model.Boards;
import com.gurukulams.core.model.Grades;
import com.gurukulams.core.model.Subjects;
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

    public static final String STATE_SUBJECT_IN_ENGLISH = "State Boards";
    public static final String STATE_SUBJECT_DESCRIPTION_IN_ENGLISH = "State Boards Description";
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
        final Subjects subject = subjectService.create("mani", null,
                anSubject());
        Assertions.assertTrue(subjectService.read("mani", null, subject.getId()).isPresent(),
                "Created Syllabous");
    }

    @Test
    void read() {
        final Subjects subject = subjectService.create("mani", null,
                anSubject());
        final UUID newSubjectId = subject.getId();
        Assertions.assertTrue(subjectService.read("mani", null, newSubjectId).isPresent(),
                "subject Created");
    }

    @Test
    void update() {

        final Subjects subject = subjectService.create("mani", null,
                anSubject());
        final UUID newSubjectId = subject.getId();
        Subjects newSubject = new Subjects();
        newSubject.setTitle("MathsSubject");
        newSubject.setDescription("An Syllabus");
        newSubject.setCreatedBy("tom");

        Subjects updateSubject = subjectService
                .update(newSubjectId, "manikanta", null, newSubject);
        Assertions.assertEquals("MathsSubject", updateSubject.getTitle(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            subjectService
                    .update(UUID.randomUUID(), "manikanta", null, newSubject);
        });
    }

    @Test
    void delete() {

        final Subjects subject = subjectService.create("mani", null,
                anSubject());
        subjectService.delete("mani", subject.getId());
        Assertions.assertFalse(subjectService.read("mani", null, subject.getId()).isPresent(), "Deleted Subjects");

    }

    @Test
    void list() {

        final Subjects subject = subjectService.create("mani", null,
                anSubject());
        Subjects newSubject = new Subjects();
        newSubject.setTitle("Physicssubject");
        newSubject.setDescription("An Syllabus");
        newSubject.setCreatedBy("tom");
        subjectService.create("manikanta", null,
                newSubject);
        List<Subjects> listofsyllabus = subjectService.list("manikanta", null);
        Assertions.assertEquals(2, listofsyllabus.size());

    }


    @Test
    void testLocalizationFromDefaultWithoutLocale() {
        // Create a Subjects for Default Language
        final Boards board = boardService.create("mani", null,
                anBoard());
        final Grades grade = gradeService.create("tom", null, aGrade());
        final Subjects subject = subjectService.create("mani", null,
                anSubject());

        testLocalization(subject);

        listbyBoardandgrade(board, grade, subject, null);

    }

    @Test
    void testLocalizationFromCreateWithLocale() {
        // Create a Subjects for GERMAN Language
        final Boards board = boardService.create("mani", Locale.GERMAN,
                anBoard());
        final Grades grade = gradeService.create("tom", Locale.GERMAN, aGrade());
        final Subjects subject = subjectService.create("mani", Locale.GERMAN,
                anSubject());

        testLocalization(subject);

        listbyBoardandgrade(board, grade, subject, Locale.FRENCH);

    }

    void testLocalization(Subjects subject) {

        // Update for China Language
        subjectService.update(subject.getId(), "mani", Locale.FRENCH, anSubject(subject,
                STATE_SUBJECT_TITLE_IN_FRENCH,
                STATE_SUBJECT_DESCRIPTION_IN_FRENCH));

        // Get for french Language
        Subjects createSubject = subjectService.read("mani", Locale.FRENCH,
                subject.getId()).get();
        Assertions.assertEquals(STATE_SUBJECT_TITLE_IN_FRENCH, createSubject.getTitle());
        Assertions.assertEquals(STATE_SUBJECT_DESCRIPTION_IN_FRENCH, createSubject.getDescription());

        final UUID id = createSubject.getId();
        createSubject = subjectService.list("mani", Locale.FRENCH)
                .stream()
                .filter(subject1 -> subject1.getId().equals(id))
                .findFirst().get();
        Assertions.assertEquals(STATE_SUBJECT_TITLE_IN_FRENCH, createSubject.getTitle());
        Assertions.assertEquals(STATE_SUBJECT_DESCRIPTION_IN_FRENCH,
                createSubject.getDescription());

        // Get for France which does not have data
        createSubject = subjectService.read("mani", Locale.CHINESE,
                subject.getId()).get();
        Assertions.assertEquals(STATE_SUBJECT_IN_ENGLISH, createSubject.getTitle());
        Assertions.assertEquals(STATE_SUBJECT_DESCRIPTION_IN_ENGLISH, createSubject.getDescription());

        createSubject = subjectService.list("mani", Locale.CHINESE)
                .stream()
                .filter(subject1 -> subject1.getId().equals(id))
                .findFirst().get();

        Assertions.assertEquals(STATE_SUBJECT_IN_ENGLISH, createSubject.getTitle());
        Assertions.assertEquals(STATE_SUBJECT_DESCRIPTION_IN_ENGLISH, createSubject.getDescription());

    }

    void listbyBoardandgrade(Boards board, Grades grade, Subjects subject, Locale locale) {

        Assertions.assertTrue(boardService.attachSubject("tom", board.getId(), grade.getId(), subject.getId()), "Unable to add grade to board");

        final UUID id = subject.getId();
        Subjects getSubject = subjectService.list("tom", locale, board.getId(), grade.getId()).stream()
                .filter(subject1 -> subject1.getId().equals(id))
                .findFirst().get();

        if (locale == null) {

            Assertions.assertEquals(STATE_SUBJECT_IN_ENGLISH, getSubject.getTitle());
            Assertions.assertEquals(STATE_SUBJECT_DESCRIPTION_IN_ENGLISH, getSubject.getDescription());

        } else {

            Assertions.assertEquals(STATE_SUBJECT_TITLE_IN_FRENCH, getSubject.getTitle());
            Assertions.assertEquals(STATE_SUBJECT_DESCRIPTION_IN_FRENCH, getSubject.getDescription());

        }


    }

    /**
     * Get subject.
     *
     * @return the subject
     */
    Subjects anSubject() {

        Subjects subject = new Subjects();
        subject.setTitle(STATE_SUBJECT_IN_ENGLISH);
        subject.setDescription(STATE_SUBJECT_DESCRIPTION_IN_ENGLISH);
        return subject;
    }

    /**
     * Gets subject from reference subject.
     *
     * @return the subject
     */
    Subjects anSubject(final Subjects ref, final String title, final String description) {
        Subjects subject =  new Subjects();
        subject.setId(ref.getId());
        subject.setTitle(title);
        subject.setDescription(description);
        subject.setCreatedAt(ref.getCreatedAt());
        subject.setCreatedBy(ref.getCreatedBy());
        subject.setModifiedAt(ref.getModifiedAt());
        subject.setModifiedBy(ref.getModifiedBy());
return  subject;
    }

    Grades aGrade() {

        Grades grade = new Grades();
        grade.setTitle("Student Grades" + new Date().getTime());
        grade.setDescription("A Grades");
        return grade;
    }


    /**
     * Gets board.
     *
     * @return the board
     */
    Boards anBoard() {

        Boards board = new Boards();
        board.setTitle("State Boards" + new Date().getTime());
        board.setDescription("A Boards");
        return board;
    }


}
