package org.fpt.studydeck.repository.matching;

import org.fpt.studydeck.domain.matching.MatchingSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingSessionRepository extends JpaRepository<MatchingSession, Long> {
}
