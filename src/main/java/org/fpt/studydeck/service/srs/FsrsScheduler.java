package org.fpt.studydeck.service.srs;

import java.time.Duration;
import java.time.Instant;

import org.fpt.studydeck.domain.srs.SrsRating;
import org.fpt.studydeck.domain.srs.SrsState;
import org.springframework.stereotype.Component;

import io.github.openspacedrepetition.Card;
import io.github.openspacedrepetition.Rating;
import io.github.openspacedrepetition.Scheduler;
import io.github.openspacedrepetition.State;

@Component
public class FsrsScheduler {

    private final Scheduler scheduler;

    public FsrsScheduler() {
        this.scheduler = Scheduler.builder()
            .enableFuzzing(false)
            .build();
    }

    public ScheduledReview reviewNewCard(SrsRating rating, Instant reviewedAt) {
        Card card = Card.builder().cardId(0).build();
        return review(card, rating, reviewedAt);
    }

    public ScheduledReview reviewExistingCard(String fsrsCardJson, SrsRating rating, Instant reviewedAt) {
        return review(Card.fromJson(fsrsCardJson), rating, reviewedAt);
    }

    private ScheduledReview review(Card card, SrsRating rating, Instant reviewedAt) {
        var result = scheduler.reviewCard(card, toFsrsRating(rating), reviewedAt);
        Card scheduledCard = result.card();
        return new ScheduledReview(
            toSrsState(scheduledCard.getState()),
            scheduledCard.getDue(),
            scheduledCard.getStability() == null ? 0 : scheduledCard.getStability(),
            scheduledCard.getDifficulty() == null ? 0 : scheduledCard.getDifficulty(),
            scheduledDays(reviewedAt, scheduledCard.getDue()),
            0,
            1,
            rating == SrsRating.AGAIN ? 1 : 0,
            scheduledCard.toJson()
        );
    }

    private static Rating toFsrsRating(SrsRating rating) {
        return switch (rating) {
            case AGAIN -> Rating.AGAIN;
            case HARD -> Rating.HARD;
            case GOOD -> Rating.GOOD;
            case EASY -> Rating.EASY;
        };
    }

    private static SrsState toSrsState(State state) {
        if (state == null) {
            return SrsState.NEW;
        }
        return switch (state) {
            case LEARNING -> SrsState.LEARNING;
            case REVIEW -> SrsState.REVIEW;
            case RELEARNING -> SrsState.RELEARNING;
        };
    }

    private static int scheduledDays(Instant reviewedAt, Instant dueAt) {
        long days = Duration.between(reviewedAt, dueAt).toDays();
        return Math.toIntExact(Math.max(0, days));
    }

    public record ScheduledReview(
        SrsState state,
        Instant dueAt,
        double stability,
        double difficulty,
        int scheduledDays,
        int elapsedDays,
        int reps,
        int lapses,
        String fsrsCardJson
    ) {
    }
}
