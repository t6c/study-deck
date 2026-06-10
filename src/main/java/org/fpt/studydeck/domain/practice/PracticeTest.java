package org.fpt.studydeck.domain.practice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.Flashcard;
import org.fpt.studydeck.domain.learn.LearnQuestionType;
import org.fpt.studydeck.domain.learn.PromptSide;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "practice_tests")
public class PracticeTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PracticeTestStatus status;

    @Column(columnDefinition = "text")
    private String settingsJson;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant submittedAt;

    @Column(nullable = false)
    private double scorePercent;

    @OneToMany(mappedBy = "practiceTest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<PracticeTestQuestion> questions = new ArrayList<>();

    protected PracticeTest() {
    }

    public static PracticeTest create(
        Deck deck,
        String settingsJson,
        List<Flashcard> flashcards,
        List<LearnQuestionType> questionTypes,
        List<PromptSide> promptSides
    ) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck is required.");
        }

        PracticeTest practiceTest = new PracticeTest();
        practiceTest.deck = deck;
        practiceTest.status = PracticeTestStatus.ACTIVE;
        practiceTest.settingsJson = settingsJson;
        practiceTest.startedAt = Instant.now();
        practiceTest.scorePercent = 0.0;
        for (int position = 0; position < flashcards.size(); position++) {
            LearnQuestionType questionType = questionTypes.get(position % questionTypes.size());
            PromptSide promptSide = promptSides.get(position % promptSides.size());
            practiceTest.addQuestion(flashcards.get(position), questionType, promptSide, position);
        }
        return practiceTest;
    }

    private void addQuestion(
        Flashcard flashcard,
        LearnQuestionType questionType,
        PromptSide promptSide,
        int position
    ) {
        questions.add(PracticeTestQuestion.create(this, flashcard, questionType, promptSide, position));
    }

    public void submit() {
        this.status = PracticeTestStatus.SUBMITTED;
        this.submittedAt = Instant.now();
        long correctAnswered = questions.stream()
            .filter(question -> Boolean.TRUE.equals(question.getCorrect()))
            .count();
        this.scorePercent = questions.isEmpty() ? 0.0 : (correctAnswered * 100.0) / questions.size();
    }

    public Long getId() {
        return id;
    }

    public Deck getDeck() {
        return deck;
    }

    public PracticeTestStatus getStatus() {
        return status;
    }

    public String getSettingsJson() {
        return settingsJson;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public double getScorePercent() {
        return scorePercent;
    }

    public List<PracticeTestQuestion> getQuestions() {
        return questions;
    }
}
