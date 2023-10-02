package com.techatpark.workout.service;

import com.techatpark.workout.model.Choice;
import com.techatpark.workout.model.Question;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
     * JdbcClient instance.
     */
    private final JdbcClient jdbcClient;

    /**
     * Constructs Answer Service.
     *
     * @param anQuestionService the an question service
     * @param aJdbcClient       the a jdbc client
     */
    AnswerService(final QuestionService anQuestionService,
                  final JdbcClient aJdbcClient) {
        this.questionService = anQuestionService;
        this.jdbcClient = aJdbcClient;
    }

    /**
     * checks whether the given answer is correct.returns true if correct.
     *
     * @param questionId the question id
     * @param answer     the answer
     * @return true boolean
     */
    public final Boolean answer(final UUID questionId,
                                final String answer) {
        Boolean isRigntAnswer = false;
        final Optional<Question> oQuestion = questionService
                .read(questionId, null);
        if (oQuestion.isPresent()) {
            final Question question = oQuestion.get();
            switch (question.getType()) {
                case CODE_SQL:
                    final String verificationSQL =
                            "SELECT COUNT(*) FROM ( " + question.getAnswer()
                                    + " except " + answer
                                    + " ) AS TOTAL_ROWS";
                    final Integer count = jdbcClient
                            .sql(verificationSQL).query(Integer.class).single();
                    isRigntAnswer = (count == 0);
                    break;
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
                            .collect(Collectors.toList());
                    if (!rightChoiceIds.isEmpty()) {
                        List<String> answerIds = List
                                .of(answer.split(","));
                        isRigntAnswer =
                                answerIds.size() == rightChoiceIds.size()
                                && answerIds.containsAll(rightChoiceIds);
                    }
                    break;
                default:
                    isRigntAnswer = answer.equalsIgnoreCase(
                            question.getAnswer().toLowerCase()
                    );
                    break;
            }

        }
        return isRigntAnswer;
    }

}
