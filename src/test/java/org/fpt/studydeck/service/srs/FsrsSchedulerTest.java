package org.fpt.studydeck.service.srs;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.fpt.studydeck.domain.srs.SrsRating;
import org.junit.jupiter.api.Test;

class FsrsSchedulerTest {

    private final FsrsScheduler fsrsScheduler = new FsrsScheduler();

    @Test
    void firstReviewWithGoodSchedulesCardInFuture() {
        Instant reviewedAt = Instant.parse("2026-01-01T00:00:00Z");

        var scheduled = fsrsScheduler.reviewNewCard(SrsRating.GOOD, reviewedAt);

        assertThat(scheduled.dueAt()).isAfter(reviewedAt);
        assertThat(scheduled.state()).isNotNull();
        assertThat(scheduled.fsrsCardJson()).contains("\"due\"");
    }
}
