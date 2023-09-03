package com.techatpark.workout.model;

import java.util.UUID;

/**
 * The type Answer.
 */
public class Answer {

    /**
     * tells the id of studentanswer.
     */
    private UUID id;
    /**
     * Tells the exam id.
     */
    private UUID examId;
    /**
     * tells the question id.
     */
    private UUID questionId;
    /**
     * tells the student answer.
     */
    private String studentAnswer;

    /**
     * gets student answer id.
     *
     * @return id id
     */
    public UUID getId() {
        return id;
    }

    /**
     * sets student answer id.
     *
     * @param anId the an id
     */
    public void setId(final UUID anId) {
        this.id = anId;
    }

    /**
     * tells exam id.
     *
     * @return exam id
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * sets exam id.
     *
     * @param anExamId the an exam id
     */
    public void setExamId(final UUID anExamId) {
        this.examId = anExamId;
    }

    /**
     * gets question id.
     *
     * @return question id
     */
    public UUID getQuestionId() {
        return questionId;
    }

    /**
     * sets question id.
     *
     * @param anQuestionId the an question id
     */
    public void setQuestionId(final UUID anQuestionId) {
        this.questionId = anQuestionId;
    }

    /**
     * gets student answer.
     *
     * @return studentAnswer student answer
     */
    public String getStudentAnswer() {
        return studentAnswer;
    }

    /**
     * sets student answer.
     *
     * @param aStudentAnswer the a student answer
     */
    public void setStudentAnswer(final String aStudentAnswer) {
        this.studentAnswer = aStudentAnswer;
    }

}
