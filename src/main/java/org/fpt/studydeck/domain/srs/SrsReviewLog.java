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
import jakarta.persistence.Table;

@Entity
@Table(name = "srs_review_logs")
public class SrsReviewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SrsRating rating;

    @Column(nullable = false)
    private Instant reviewedAt;

    @Column(nullable = false)
    private long durationMs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SrsState previousState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SrsState nextState;

    private Instant dueBefore;

    @Column(nullable = false)
    private Instant dueAfter;

    @Lob
    private String reviewLogJson;

    protected SrsReviewLog() {
    }

    public static SrsReviewLog create(
        Flashcard flashcard,
        SrsRating rating,
        Instant reviewedAt,
        long durationMs,
        SrsState previousState,
        SrsState nextState,
        Instant dueBefore,
        Instant dueAfter,
        String reviewLogJson
    ) {
        SrsReviewLog log = new SrsReviewLog();
        log.flashcard = flashcard;
        log.rating = rating;
        log.reviewedAt = reviewedAt;
        log.durationMs = durationMs;
        log.previousState = previousState;
        log.nextState = nextState;
        log.dueBefore = dueBefore;
        log.dueAfter = dueAfter;
        log.reviewLogJson = reviewLogJson;
        return log;
    }

    public Long getId() {
        return id;
    }
}
