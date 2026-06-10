package org.fpt.studydeck.domain.matching;

import java.time.Instant;

import org.fpt.studydeck.domain.deck.Flashcard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "matching_session_items")
public class MatchingSessionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private MatchingSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Column(nullable = false)
    private boolean matched;

    private Instant matchedAt;

    @Column(nullable = false)
    private int position;

    protected MatchingSessionItem() {
    }

    public static MatchingSessionItem create(MatchingSession session, Flashcard flashcard, int position) {
        if (flashcard == null) {
            throw new IllegalArgumentException("Flashcard is required.");
        }

        MatchingSessionItem item = new MatchingSessionItem();
        item.session = session;
        item.flashcard = flashcard;
        item.matched = false;
        item.position = position;
        return item;
    }

    public void match() {
        if (!matched) {
            this.matched = true;
            this.matchedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public MatchingSession getSession() {
        return session;
    }

    public Flashcard getFlashcard() {
        return flashcard;
    }

    public boolean isMatched() {
        return matched;
    }

    public Instant getMatchedAt() {
        return matchedAt;
    }

    public int getPosition() {
        return position;
    }
}
