package org.fpt.studydeck.domain.sorting;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sorting_session_items")
public class SortingSessionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private SortingSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Column(nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SortingAnswer answer;

    private Instant answeredAt;

    protected SortingSessionItem() {
    }

    public static SortingSessionItem create(SortingSession session, Flashcard flashcard, int position) {
        if (flashcard == null) {
            throw new IllegalArgumentException("Flashcard is required.");
        }

        SortingSessionItem item = new SortingSessionItem();
        item.session = session;
        item.flashcard = flashcard;
        item.position = position;
        return item;
    }

    public void answer(SortingAnswer answer) {
        this.answer = answer;
        this.answeredAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public SortingSession getSession() {
        return session;
    }

    public Flashcard getFlashcard() {
        return flashcard;
    }

    public int getPosition() {
        return position;
    }

    public SortingAnswer getAnswer() {
        return answer;
    }

    public Instant getAnsweredAt() {
        return answeredAt;
    }
}
