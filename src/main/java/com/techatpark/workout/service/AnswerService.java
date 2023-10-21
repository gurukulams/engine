package com.techatpark.workout.service;

import com.techatpark.workout.model.Choice;
import com.techatpark.workout.model.Question;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The type Answer service.
 */
@Service
public class AnswerService {

    /**
     * question Service.
     */
    private final QuestionService questionService;

    /**
     * Constructs Answer Service.
     *
     * @param anQuestionService the an question service
     */
    AnswerService(final QuestionService anQuestionService) {
        this.questionService = anQuestionService;
    }

    /**
     * checks whether the given answer is correct.returns true if correct.
     *
     * @param questionId the question id
     * @param answer     the answer
     * @return true boolean
     */
    public final boolean answer(final UUID questionId,
                                final String answer) {
        boolean isRigntAnswer = false;
        final Optional<Question> oQuestion = questionService
                .read(questionId, null);
        if (oQuestion.isPresent()) {
            final Question question = oQuestion.get();
            switch (question.getType()) {
                case CHOOSE_THE_BEST:
                    Optional<Choice> rightChoice = question.getChoices()
                            .stream()
                            .filter(Choice::isAnswer)
                            .findFirst();
                    if (rightChoice.isPresent()) {
                        isRigntAnswer = rightChoice.get()
                                .getId()
                                .toString()
                                .equals(answer);
                    }
                    break;
                case MULTI_CHOICE:
                    List<String> rightChoiceIds = question.getChoices()
                            .stream()
                            .filter(Choice::isAnswer)
                            .map(choice -> choice.getId().toString())
                            .toList();
                    if (!rightChoiceIds.isEmpty()) {
                        Set<String> answerIds = Set.of(answer.split(","));
                        isRigntAnswer =
                                answerIds.size() == rightChoiceIds.size()
                                && answerIds.containsAll(rightChoiceIds);
                    }
                    break;
                default:
                    break;
            }

        }
        return isRigntAnswer;
    }

}
