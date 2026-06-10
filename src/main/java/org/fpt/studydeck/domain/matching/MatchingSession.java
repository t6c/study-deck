package org.fpt.studydeck.domain.matching;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.Flashcard;

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
@Table(name = "matching_sessions")
public class MatchingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchingSessionStatus status;

    @Column(nullable = false)
    private int cardCount;

    @Column(nullable = false)
    private boolean starredOnly;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant completedAt;

    @Column(nullable = false)
    private long durationMs;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<MatchingSessionItem> items = new ArrayList<>();

    protected MatchingSession() {
    }

    public static MatchingSession create(Deck deck, int cardCount, boolean starredOnly, List<Flashcard> flashcards) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck is required.");
        }

        MatchingSession session = new MatchingSession();
        session.deck = deck;
        session.status = MatchingSessionStatus.ACTIVE;
        session.cardCount = cardCount;
        session.starredOnly = starredOnly;
        session.startedAt = Instant.now();
        session.durationMs = 0;
        for (int position = 0; position < flashcards.size(); position++) {
            session.addItem(flashcards.get(position), position);
        }
        return session;
    }

    private void addItem(Flashcard flashcard, int position) {
        items.add(MatchingSessionItem.create(this, flashcard, position));
    }

    public void complete() {
        if (status == MatchingSessionStatus.COMPLETED) {
            return;
        }
        this.status = MatchingSessionStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.durationMs = Duration.between(startedAt, completedAt).toMillis();
    }

    public Long getId() {
        return id;
    }

    public Deck getDeck() {
        return deck;
    }

    public MatchingSessionStatus getStatus() {
        return status;
    }

    public int getCardCount() {
        return cardCount;
    }

    public boolean isStarredOnly() {
        return starredOnly;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public List<MatchingSessionItem> getItems() {
        return items;
    }
}
