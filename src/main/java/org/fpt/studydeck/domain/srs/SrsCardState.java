package org.fpt.studydeck.domain.srs;

import java.time.Instant;

import org.fpt.studydeck.domain.deck.Flashcard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "srs_card_states",
    uniqueConstraints = @UniqueConstraint(name = "uk_srs_card_states_flashcard_id", columnNames = "flashcard_id")
)
public class SrsCardState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Lob
    @Column(name = "fsrs_card_json", nullable = false)
    private String fsrsCardJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SrsState state;

    @Column(nullable = false)
    private Instant dueAt;

    @Column(nullable = false)
    private double stability;

    @Column(nullable = false)
    private double difficulty;

    @Column(nullable = false)
    private int scheduledDays;

    @Column(nullable = false)
    private int elapsedDays;

    @Column(nullable = false)
    private int reps;

    @Column(nullable = false)
    private int lapses;

    private Instant lastReviewedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected SrsCardState() {
    }

    public static SrsCardState createNew(Flashcard flashcard) {
        if (flashcard == null) {
            throw new IllegalArgumentException("Flashcard is required.");
        }
        SrsCardState state = new SrsCardState();
        state.flashcard = flashcard;
        state.fsrsCardJson = "{}";
        state.state = SrsState.NEW;
        state.dueAt = Instant.EPOCH;
        return state;
    }

    public void applyReview(
        SrsRating rating,
        SrsState nextState,
        Instant dueAt,
        double stability,
        double difficulty,
        int scheduledDays,
        int elapsedDays,
        int reps,
        int lapses,
        String fsrsCardJson,
        Instant reviewedAt
    ) {
        this.state = nextState;
        this.dueAt = dueAt;
        this.stability = stability;
        this.difficulty = difficulty;
        this.scheduledDays = scheduledDays;
        this.elapsedDays = elapsedDays;
        this.reps = reps;
        this.lapses = lapses;
        this.fsrsCardJson = fsrsCardJson;
        this.lastReviewedAt = reviewedAt;
        this.updatedAt = Instant.now();
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Flashcard getFlashcard() {
        return flashcard;
    }

    public String getFsrsCardJson() {
        return fsrsCardJson;
    }

    public SrsState getState() {
        return state;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public double getStability() {
        return stability;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public int getScheduledDays() {
        return scheduledDays;
    }

    public int getElapsedDays() {
        return elapsedDays;
    }

    public int getReps() {
        return reps;
    }

    public int getLapses() {
        return lapses;
    }

    public Instant getLastReviewedAt() {
        return lastReviewedAt;
    }
}
