package org.fpt.studydeck.repository.learn;

import org.fpt.studydeck.domain.learn.LearnSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearnSessionRepository extends JpaRepository<LearnSession, Long> {
}
