package org.fpt.studydeck.dto.learn;

import org.fpt.studydeck.domain.learn.LearnQuestionType;
import org.fpt.studydeck.domain.learn.LearnSessionItem;
import org.fpt.studydeck.domain.learn.PromptSide;

public record LearnSessionItemResponse(
    Long id,
    Long flashcardId,
    LearnQuestionType questionType,
    PromptSide promptSide,
    String prompt,
    String answer,
    int attempts
) {

    public static LearnSessionItemResponse from(LearnSessionItem item) {
        var flashcard = item.getFlashcard();
        boolean promptWithTerm = item.getPromptSide() == PromptSide.TERM;
        return new LearnSessionItemResponse(
            item.getId(),
            flashcard.getId(),
            item.getQuestionType(),
            item.getPromptSide(),
            promptWithTerm ? flashcard.getTerm() : flashcard.getDefinition(),
            promptWithTerm ? flashcard.getDefinition() : flashcard.getTerm(),
            item.getAttempts()
        );
    }
}
