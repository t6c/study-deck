package org.fpt.studydeck.domain.auth;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 160)
    private String displayName;

    @Column(nullable = false, length = 30)
    private String role;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected AppUser() {
    }

    public static AppUser create(String email, String passwordHash, String displayName) {
        Instant now = Instant.now();
        AppUser user = new AppUser();
        user.email = normalizeEmail(email);
        user.passwordHash = passwordHash;
        user.displayName = trimToNull(displayName);
        user.role = "USER";
        user.createdAt = now;
        user.updatedAt = now;
        return user;
    }

    public static String normalizeEmail(String email) {
        String trimmed = trimToNull(email);
        if (trimmed == null) {
            throw new IllegalArgumentException("Email is required.");
        }
        return trimmed.toLowerCase();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
