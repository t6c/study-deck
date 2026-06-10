package org.fpt.studydeck.domain.deck;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "folders")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private int position = 0;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Folder() {
    }

    public static Folder create(String name, String description) {
        Folder folder = new Folder();
        folder.name = requireName(name);
        folder.description = trimToNull(description);
        folder.position = 0;
        folder.createdAt = Instant.now();
        folder.updatedAt = folder.createdAt;
        return folder;
    }

    public void rename(String name, String description) {
        this.name = requireName(name);
        this.description = trimToNull(description);
        this.updatedAt = Instant.now();
    }

    private static String requireName(String name) {
        String trimmed = trimToNull(name);
        if (trimmed == null) {
            throw new IllegalArgumentException("Folder name is required.");
        }
        return trimmed;
    }

    static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPosition() {
        return position;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
