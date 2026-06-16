package org.fpt.studydeck.repository.auth;

import java.util.Optional;

import org.fpt.studydeck.domain.auth.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
