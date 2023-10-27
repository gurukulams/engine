package com.techatpark.workout.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gurukulams.core.model.QuestionChoice;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * The type Question.
 */
public class Question {
    /**
     * tells the id of question.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    /**
     * this is the question.
     */
    @NotBlank(message = "question is mandatory")
    private String question;

    /**
     * this is the explanation.
     */
    private String explanation;

    /**
     * tells the answer.
     */
    private String answer;

    /**
     * created_by of question.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String createdBy;

    /**
     * created_at of question.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;


    /**
     * updated_at of question.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedAt;

    /**
     * tells the type of question being created.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private QuestionType type;

    /**
     * tells the question choices available.
     */
    private List<QuestionChoice> choices;

    /**
     * gets the type of question.
     *
     * @return type type
     */
    public QuestionType getType() {
        return type;
    }

    /***
     * sets the type of question.
     *
     * @param aType the a type
     */
    public void setType(final QuestionType aType) {
        this.type = aType;
    }

    /**
     * gets the id of question.
     *
     * @return id id
     */
    public UUID getId() {
        return id;
    }

    /**
     * sets the id of question.
     *
     * @param anId the an id
     */
    public void setId(final UUID anId) {
        this.id = anId;
    }

    /**
     * gets the question.
     *
     * @return question question
     */
    public String getQuestion() {
        return question;
    }

    /**
     * sets the question.
     *
     * @param anQuestion the an question
     */
    public void setQuestion(final String anQuestion) {
        this.question = anQuestion;
    }

    /**
     * Gets Explanation.
     *
     * @return explanation
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Sets Explanation.
     *
     * @param anExplanation
     */
    public void setExplanation(final String anExplanation) {
        this.explanation = anExplanation;
    }

    /**
     * gets the answer.
     *
     * @return answer answer
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * sets the answer.
     *
     * @param anAnswer the an answer
     */
    public void setAnswer(final String anAnswer) {
        this.answer = anAnswer;
    }

    /**
     * Gets question choice.
     *
     * @return the question choice
     */
    public List<QuestionChoice> getChoices() {
        return choices;
    }

    /**
     * Sets question choice.
     *
     * @param theChoice the question choice
     */
    public void setChoices(final List<QuestionChoice> theChoice) {
        this.choices = theChoice;
    }

    /**
     * created_by of the question.
     *
     * @return created_by created_by
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets created_by of the question.
     *
     * @param theOwner the the created_by
     */
    public void setCreatedBy(final String theOwner) {
        this.createdBy = theOwner;
    }

    /**
     * sets created at.
     *
     * @return createdAt
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * gets created At.
     *
     * @param aCreatedAt the created at
     */
    public void setCreatedAt(final LocalDateTime aCreatedAt) {
        this.createdAt = aCreatedAt;
    }

    /**
     * gets updated at.
     *
     * @return updatedAt
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets updated at.
     *
     * @param aupdatedAt
     */
    public void setUpdatedAt(final LocalDateTime aupdatedAt) {
        this.updatedAt = aupdatedAt;
    }
}
