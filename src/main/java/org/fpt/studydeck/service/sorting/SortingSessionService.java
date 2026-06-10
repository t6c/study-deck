package org.fpt.studydeck.service.sorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.Flashcard;
import org.fpt.studydeck.domain.sorting.SortingAnswer;
import org.fpt.studydeck.domain.sorting.SortingSession;
import org.fpt.studydeck.domain.sorting.SortingSessionItem;
import org.fpt.studydeck.domain.sorting.SortingSessionStatus;
import org.fpt.studydeck.dto.sorting.CreateSortingSessionRequest;
import org.fpt.studydeck.dto.sorting.SortingAnswerRequest;
import org.fpt.studydeck.dto.sorting.SortingSessionItemResponse;
import org.fpt.studydeck.dto.sorting.SortingSessionResponse;
import org.fpt.studydeck.exception.InvalidRequestException;
import org.fpt.studydeck.exception.ResourceConflictException;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.fpt.studydeck.repository.sorting.SortingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SortingSessionService {

    private static final String DECK_NOT_FOUND = "Deck was not found.";
    private static final String SESSION_NOT_FOUND = "Sorting session was not found.";
    private static final String ITEM_NOT_FOUND = "Sorting session item was not found.";
    private static final String NO_CARDS_AVAILABLE = "No cards are available for sorting.";
    private static final String SESSION_COMPLETED = "Sorting session is already completed.";

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final SortingSessionRepository sortingSessionRepository;

    public SortingSessionService(
        DeckRepository deckRepository,
        FlashcardRepository flashcardRepository,
        SortingSessionRepository sortingSessionRepository
    ) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.sortingSessionRepository = sortingSessionRepository;
    }

    public SortingSessionResponse createSession(Long deckId, CreateSortingSessionRequest request) {
        Deck deck = deckRepository.findById(deckId)
            .orElseThrow(() -> new ResourceNotFoundException(DECK_NOT_FOUND));
        boolean starredOnly = request != null && request.starredOnly();
        boolean shuffle = request != null && request.shuffle();

        List<Flashcard> flashcards = starredOnly
            ? flashcardRepository.findByDeckIdAndStarredTrueOrderByPositionAscIdAsc(deckId)
            : flashcardRepository.findByDeckIdOrderByPositionAscIdAsc(deckId);
        if (flashcards.isEmpty()) {
            throw new InvalidRequestException(NO_CARDS_AVAILABLE);
        }

        List<Flashcard> selectedCards = new ArrayList<>(flashcards);
        if (shuffle && selectedCards.size() > 1) {
            Collections.reverse(selectedCards);
        }

        SortingSession session = sortingSessionRepository.save(
            SortingSession.create(deck, starredOnly, shuffle, selectedCards)
        );
        return toResponse(session);
    }

    public SortingSessionResponse answer(Long sessionId, SortingAnswerRequest request) {
        SortingSession session = findSession(sessionId);
        if (session.getStatus() == SortingSessionStatus.COMPLETED) {
            throw new ResourceConflictException(SESSION_COMPLETED);
        }

        SortingSessionItem item = session.getItems().stream()
            .filter(candidate -> candidate.getId().equals(request.itemId()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));

        item.answer(request.answer());
        if (session.getItems().stream().allMatch(candidate -> candidate.getAnswer() != null)) {
            session.complete();
        }

        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public SortingSessionResponse getSession(Long sessionId) {
        return toResponse(findSession(sessionId));
    }

    private SortingSession findSession(Long sessionId) {
        return sortingSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException(SESSION_NOT_FOUND));
    }

    private SortingSessionResponse toResponse(SortingSession session) {
        int knownCount = countAnswers(session, SortingAnswer.KNOW);
        int doNotKnowCount = countAnswers(session, SortingAnswer.DO_NOT_KNOW);
        List<SortingSessionItemResponse> items = session.getItems().stream()
            .map(SortingSessionItemResponse::from)
            .toList();

        return new SortingSessionResponse(
            session.getId(),
            session.getStatus().name(),
            knownCount,
            doNotKnowCount,
            items
        );
    }

    private int countAnswers(SortingSession session, SortingAnswer answer) {
        return Math.toIntExact(session.getItems().stream()
            .filter(item -> item.getAnswer() == answer)
            .count());
    }
}
