package org.fpt.studydeck.domain.sorting;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "sorting_sessions")
public class SortingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SortingSessionStatus status;

    @Column(nullable = false)
    private boolean starredOnly;

    @Column(nullable = false)
    private boolean shuffle;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant completedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SortingSessionItem> items = new ArrayList<>();

    protected SortingSession() {
    }

    public static SortingSession create(Deck deck, boolean starredOnly, boolean shuffle, List<Flashcard> flashcards) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck is required.");
        }

        SortingSession session = new SortingSession();
        session.deck = deck;
        session.status = SortingSessionStatus.ACTIVE;
        session.starredOnly = starredOnly;
        session.shuffle = shuffle;
        session.startedAt = Instant.now();
        flashcards.forEach(session::addItem);
        return session;
    }

    private void addItem(Flashcard flashcard) {
        items.add(SortingSessionItem.create(this, flashcard));
    }

    public void complete() {
        this.status = SortingSessionStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Deck getDeck() {
        return deck;
    }

    public SortingSessionStatus getStatus() {
        return status;
    }

    public boolean isStarredOnly() {
        return starredOnly;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public List<SortingSessionItem> getItems() {
        return items;
    }
}
