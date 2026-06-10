package org.fpt.studydeck.service.matching;

import java.util.Comparator;
import java.util.List;

import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.Flashcard;
import org.fpt.studydeck.domain.matching.MatchingSession;
import org.fpt.studydeck.domain.matching.MatchingSessionItem;
import org.fpt.studydeck.domain.matching.MatchingSessionStatus;
import org.fpt.studydeck.dto.matching.CreateMatchingSessionRequest;
import org.fpt.studydeck.dto.matching.MatchingAnswerRequest;
import org.fpt.studydeck.dto.matching.MatchingSessionItemResponse;
import org.fpt.studydeck.dto.matching.MatchingSessionResponse;
import org.fpt.studydeck.exception.InvalidRequestException;
import org.fpt.studydeck.exception.ResourceConflictException;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.fpt.studydeck.repository.matching.MatchingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MatchingSessionService {

    private static final String DECK_NOT_FOUND = "Deck was not found.";
    private static final String SESSION_NOT_FOUND = "Matching session was not found.";
    private static final String ITEM_NOT_FOUND = "Matching session item was not found.";
    private static final String NOT_ENOUGH_CARDS = "Not enough cards are available for matching.";
    private static final String SESSION_COMPLETED = "Matching session is already completed.";

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final MatchingSessionRepository matchingSessionRepository;

    public MatchingSessionService(
        DeckRepository deckRepository,
        FlashcardRepository flashcardRepository,
        MatchingSessionRepository matchingSessionRepository
    ) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.matchingSessionRepository = matchingSessionRepository;
    }

    public MatchingSessionResponse createSession(Long deckId, CreateMatchingSessionRequest request) {
        Deck deck = deckRepository.findById(deckId)
            .orElseThrow(() -> new ResourceNotFoundException(DECK_NOT_FOUND));
        int cardCount = request == null ? 0 : request.cardCount();
        if (cardCount < 1) {
            throw new InvalidRequestException(NOT_ENOUGH_CARDS);
        }

        boolean starredOnly = request.starredOnly();
        List<Flashcard> flashcards = starredOnly
            ? flashcardRepository.findByDeckIdAndStarredTrueOrderByPositionAscIdAsc(deckId)
            : flashcardRepository.findByDeckIdOrderByPositionAscIdAsc(deckId);
        if (flashcards.size() < cardCount) {
            throw new InvalidRequestException(NOT_ENOUGH_CARDS);
        }

        MatchingSession session = matchingSessionRepository.save(
            MatchingSession.create(deck, cardCount, starredOnly, flashcards.stream().limit(cardCount).toList())
        );
        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public MatchingSessionResponse getSession(Long sessionId) {
        return toResponse(findSession(sessionId));
    }

    public MatchingSessionResponse match(Long sessionId, MatchingAnswerRequest request) {
        MatchingSession session = findSessionForUpdate(sessionId);
        if (session.getStatus() == MatchingSessionStatus.COMPLETED) {
            throw new ResourceConflictException(SESSION_COMPLETED);
        }

        MatchingSessionItem item = session.getItems().stream()
            .filter(candidate -> candidate.getId().equals(request.itemId()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));

        item.match();
        matchingSessionRepository.flush();
        if (matchingSessionRepository.countUnmatchedItemsBySessionId(sessionId) == 0) {
            session.complete();
        }

        return toResponse(session);
    }

    public MatchingSessionResponse complete(Long sessionId) {
        MatchingSession session = findSessionForUpdate(sessionId);
        session.complete();
        return toResponse(session);
    }

    private MatchingSession findSession(Long sessionId) {
        return matchingSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException(SESSION_NOT_FOUND));
    }

    private MatchingSession findSessionForUpdate(Long sessionId) {
        return matchingSessionRepository.findByIdForUpdate(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException(SESSION_NOT_FOUND));
    }

    private MatchingSessionResponse toResponse(MatchingSession session) {
        List<MatchingSessionItemResponse> items = orderedItems(session).stream()
            .map(MatchingSessionItemResponse::from)
            .toList();

        return new MatchingSessionResponse(
            session.getId(),
            session.getStatus().name(),
            session.getCardCount(),
            matchedCount(session),
            session.getDurationMs(),
            items
        );
    }

    private int matchedCount(MatchingSession session) {
        return Math.toIntExact(session.getItems().stream()
            .filter(MatchingSessionItem::isMatched)
            .count());
    }

    private List<MatchingSessionItem> orderedItems(MatchingSession session) {
        return session.getItems().stream()
            .sorted(Comparator.comparingInt(MatchingSessionItem::getPosition))
            .toList();
    }
}
