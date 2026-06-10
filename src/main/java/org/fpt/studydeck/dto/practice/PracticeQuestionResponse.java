package org.fpt.studydeck.dto.practice;

import org.fpt.studydeck.domain.learn.LearnQuestionType;
import org.fpt.studydeck.domain.learn.PromptSide;
import org.fpt.studydeck.domain.practice.PracticeTestQuestion;

public record PracticeQuestionResponse(
    Long id,
    Long flashcardId,
    LearnQuestionType questionType,
    PromptSide promptSide,
    String prompt,
    String submittedAnswer,
    Boolean correct
) {

    public static PracticeQuestionResponse from(PracticeTestQuestion question) {
        var flashcard = question.getFlashcard();
        String prompt = question.getQuestionType() == LearnQuestionType.TRUE_FALSE
            ? flashcard.getTerm() + " = " + flashcard.getDefinition()
            : prompt(question);
        return new PracticeQuestionResponse(
            question.getId(),
            flashcard.getId(),
            question.getQuestionType(),
            question.getPromptSide(),
            prompt,
            question.getSubmittedAnswer(),
            question.getCorrect()
        );
    }

    private static String prompt(PracticeTestQuestion question) {
        return question.getPromptSide() == PromptSide.TERM
            ? question.getFlashcard().getTerm()
            : question.getFlashcard().getDefinition();
    }
}
