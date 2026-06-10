package org.fpt.studydeck.domain.practice;

import java.time.Instant;

import org.fpt.studydeck.domain.deck.Flashcard;
import org.fpt.studydeck.domain.learn.LearnQuestionType;
import org.fpt.studydeck.domain.learn.PromptSide;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "practice_test_questions")
public class PracticeTestQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practice_test_id", nullable = false)
    private PracticeTest practiceTest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LearnQuestionType questionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PromptSide promptSide;

    @Column(nullable = false, length = 1000)
    private String correctAnswer;

    @Column(length = 1000)
    private String submittedAnswer;

    private Boolean correct;

    private Instant answeredAt;

    @Column(nullable = false)
    private int position;

    protected PracticeTestQuestion() {
    }

    public static PracticeTestQuestion create(
        PracticeTest practiceTest,
        Flashcard flashcard,
        LearnQuestionType questionType,
        PromptSide promptSide,
        int position
    ) {
        if (flashcard == null) {
            throw new IllegalArgumentException("Flashcard is required.");
        }

        PracticeTestQuestion question = new PracticeTestQuestion();
        question.practiceTest = practiceTest;
        question.flashcard = flashcard;
        question.questionType = questionType;
        question.promptSide = promptSide;
        question.correctAnswer = correctAnswer(flashcard, questionType, promptSide);
        question.position = position;
        return question;
    }

    public void answer(String submittedAnswer, boolean correct) {
        this.submittedAnswer = submittedAnswer;
        this.correct = correct;
        this.answeredAt = Instant.now();
    }

    private static String correctAnswer(
        Flashcard flashcard,
        LearnQuestionType questionType,
        PromptSide promptSide
    ) {
        if (questionType == LearnQuestionType.TRUE_FALSE) {
            return "true";
        }
        return promptSide == PromptSide.TERM ? flashcard.getDefinition() : flashcard.getTerm();
    }

    public Long getId() {
        return id;
    }

    public PracticeTest getPracticeTest() {
        return practiceTest;
    }

    public Flashcard getFlashcard() {
        return flashcard;
    }

    public LearnQuestionType getQuestionType() {
        return questionType;
    }

    public PromptSide getPromptSide() {
        return promptSide;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getSubmittedAnswer() {
        return submittedAnswer;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public Instant getAnsweredAt() {
        return answeredAt;
    }

    public int getPosition() {
        return position;
    }
}
