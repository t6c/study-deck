# Study Deck Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the MVP Spring Boot backend for folders, decks, flashcards, Knowt-like learning modes, and FSRS spaced repetition.

**Architecture:** Use the approved layered modular monolith under `org.fpt.studydeck`. Controllers expose REST/OpenAPI contracts, DTOs define API payloads, services own business rules, repositories own persistence, and domain entities stay internal.

**Tech Stack:** Java 17, Spring Boot 3.5.x, Spring Web, Spring Data JPA, Spring Validation, Spring Security, springdoc-openapi, H2, MySQL, JUnit 5, MockMvc, java-fsrs.

---

## Scope Check

This plan implements the approved design in one backend project but splits delivery into independently testable vertical slices:

1. Infrastructure and error handling.
2. Core folders/decks/flashcards.
3. Deck summary and viewer/sorting.
4. FSRS spaced repetition.
5. Learn sessions.
6. Matching sessions.
7. Practice tests.
8. Final OpenAPI and integration verification.

Frontend, auth ownership, XP, audio, uploads, plugins, and FSRS optimization remain out of scope.

## File Structure

Create and modify these root files:

```text
pom.xml
src/main/resources/application.yaml
src/main/java/org/fpt/studydeck/StudyDeckApplication.java
src/test/java/org/fpt/studydeck/StudyDeckApplicationTests.java
```

Create infrastructure files:

```text
src/main/java/org/fpt/studydeck/config/OpenApiConfig.java
src/main/java/org/fpt/studydeck/config/SecurityConfig.java
src/main/java/org/fpt/studydeck/exception/ApiErrorResponse.java
src/main/java/org/fpt/studydeck/exception/FieldErrorResponse.java
src/main/java/org/fpt/studydeck/exception/GlobalExceptionHandler.java
src/main/java/org/fpt/studydeck/exception/InvalidRequestException.java
src/main/java/org/fpt/studydeck/exception/ResourceConflictException.java
src/main/java/org/fpt/studydeck/exception/ResourceNotFoundException.java
src/test/java/org/fpt/studydeck/exception/GlobalExceptionHandlerTest.java
src/test/java/org/fpt/studydeck/config/SecurityConfigTest.java
```

Create core content files:

```text
src/main/java/org/fpt/studydeck/domain/deck/Deck.java
src/main/java/org/fpt/studydeck/domain/deck/DeckVisibility.java
src/main/java/org/fpt/studydeck/domain/deck/Flashcard.java
src/main/java/org/fpt/studydeck/domain/deck/Folder.java
src/main/java/org/fpt/studydeck/repository/deck/DeckRepository.java
src/main/java/org/fpt/studydeck/repository/deck/FlashcardRepository.java
src/main/java/org/fpt/studydeck/repository/deck/FolderRepository.java
src/main/java/org/fpt/studydeck/service/deck/DeckService.java
src/main/java/org/fpt/studydeck/service/deck/FlashcardService.java
src/main/java/org/fpt/studydeck/service/deck/FolderService.java
src/main/java/org/fpt/studydeck/dto/deck/CreateDeckRequest.java
src/main/java/org/fpt/studydeck/dto/deck/CreateFlashcardRequest.java
src/main/java/org/fpt/studydeck/dto/deck/CreateFolderRequest.java
src/main/java/org/fpt/studydeck/dto/deck/DeckResponse.java
src/main/java/org/fpt/studydeck/dto/deck/FlashcardResponse.java
src/main/java/org/fpt/studydeck/dto/deck/FolderResponse.java
src/main/java/org/fpt/studydeck/dto/deck/StarFlashcardRequest.java
src/main/java/org/fpt/studydeck/dto/deck/UpdateDeckRequest.java
src/main/java/org/fpt/studydeck/dto/deck/UpdateFlashcardRequest.java
src/main/java/org/fpt/studydeck/dto/deck/UpdateFolderRequest.java
src/main/java/org/fpt/studydeck/controller/deck/DeckController.java
src/main/java/org/fpt/studydeck/controller/deck/FlashcardController.java
src/main/java/org/fpt/studydeck/controller/deck/FolderController.java
src/test/java/org/fpt/studydeck/service/deck/FolderServiceTest.java
src/test/java/org/fpt/studydeck/service/deck/DeckServiceTest.java
src/test/java/org/fpt/studydeck/service/deck/FlashcardServiceTest.java
src/test/java/org/fpt/studydeck/controller/deck/FolderControllerTest.java
src/test/java/org/fpt/studydeck/controller/deck/DeckControllerTest.java
src/test/java/org/fpt/studydeck/controller/deck/FlashcardControllerTest.java
```

Create viewer/sorting files:

```text
src/main/java/org/fpt/studydeck/domain/sorting/SortingAnswer.java
src/main/java/org/fpt/studydeck/domain/sorting/SortingSession.java
src/main/java/org/fpt/studydeck/domain/sorting/SortingSessionItem.java
src/main/java/org/fpt/studydeck/domain/sorting/SortingSessionStatus.java
src/main/java/org/fpt/studydeck/repository/sorting/SortingSessionRepository.java
src/main/java/org/fpt/studydeck/service/deck/DeckSummaryService.java
src/main/java/org/fpt/studydeck/service/deck/ViewerCardService.java
src/main/java/org/fpt/studydeck/service/sorting/SortingSessionService.java
src/main/java/org/fpt/studydeck/dto/deck/DeckSummaryResponse.java
src/main/java/org/fpt/studydeck/dto/deck/ViewerCardResponse.java
src/main/java/org/fpt/studydeck/dto/sorting/CreateSortingSessionRequest.java
src/main/java/org/fpt/studydeck/dto/sorting/SortingAnswerRequest.java
src/main/java/org/fpt/studydeck/dto/sorting/SortingSessionResponse.java
src/main/java/org/fpt/studydeck/controller/deck/DeckSummaryController.java
src/main/java/org/fpt/studydeck/controller/deck/ViewerCardController.java
src/main/java/org/fpt/studydeck/controller/sorting/SortingSessionController.java
src/test/java/org/fpt/studydeck/service/deck/DeckSummaryServiceTest.java
src/test/java/org/fpt/studydeck/service/deck/ViewerCardServiceTest.java
src/test/java/org/fpt/studydeck/service/sorting/SortingSessionServiceTest.java
```

Create SRS files:

```text
src/main/java/org/fpt/studydeck/domain/srs/SrsCardState.java
src/main/java/org/fpt/studydeck/domain/srs/SrsRating.java
src/main/java/org/fpt/studydeck/domain/srs/SrsReviewLog.java
src/main/java/org/fpt/studydeck/domain/srs/SrsState.java
src/main/java/org/fpt/studydeck/repository/srs/SrsCardStateRepository.java
src/main/java/org/fpt/studydeck/repository/srs/SrsReviewLogRepository.java
src/main/java/org/fpt/studydeck/service/srs/FsrsScheduler.java
src/main/java/org/fpt/studydeck/service/srs/SrsReviewService.java
src/main/java/org/fpt/studydeck/service/srs/SrsStatsService.java
src/main/java/org/fpt/studydeck/dto/srs/SrsCardStateResponse.java
src/main/java/org/fpt/studydeck/dto/srs/SrsDueCardResponse.java
src/main/java/org/fpt/studydeck/dto/srs/SrsReviewRequest.java
src/main/java/org/fpt/studydeck/dto/srs/SrsReviewResponse.java
src/main/java/org/fpt/studydeck/dto/srs/SrsStatsResponse.java
src/main/java/org/fpt/studydeck/controller/srs/SrsController.java
src/test/java/org/fpt/studydeck/service/srs/FsrsSchedulerTest.java
src/test/java/org/fpt/studydeck/service/srs/SrsReviewServiceTest.java
src/test/java/org/fpt/studydeck/controller/srs/SrsControllerTest.java
```

Create Learn files:

```text
src/main/java/org/fpt/studydeck/domain/learn/LearnQuestionType.java
src/main/java/org/fpt/studydeck/domain/learn/LearnSession.java
src/main/java/org/fpt/studydeck/domain/learn/LearnSessionItem.java
src/main/java/org/fpt/studydeck/domain/learn/LearnSessionStatus.java
src/main/java/org/fpt/studydeck/domain/learn/PromptSide.java
src/main/java/org/fpt/studydeck/repository/learn/LearnSessionRepository.java
src/main/java/org/fpt/studydeck/service/learn/LearnSessionService.java
src/main/java/org/fpt/studydeck/dto/learn/CreateLearnSessionRequest.java
src/main/java/org/fpt/studydeck/dto/learn/LearnAnswerRequest.java
src/main/java/org/fpt/studydeck/dto/learn/LearnSessionResponse.java
src/main/java/org/fpt/studydeck/controller/learn/LearnSessionController.java
src/test/java/org/fpt/studydeck/service/learn/LearnSessionServiceTest.java
src/test/java/org/fpt/studydeck/controller/learn/LearnSessionControllerTest.java
```

Create Matching files:

```text
src/main/java/org/fpt/studydeck/domain/matching/MatchingSession.java
src/main/java/org/fpt/studydeck/domain/matching/MatchingSessionItem.java
src/main/java/org/fpt/studydeck/domain/matching/MatchingSessionStatus.java
src/main/java/org/fpt/studydeck/repository/matching/MatchingSessionRepository.java
src/main/java/org/fpt/studydeck/service/matching/MatchingSessionService.java
src/main/java/org/fpt/studydeck/dto/matching/CreateMatchingSessionRequest.java
src/main/java/org/fpt/studydeck/dto/matching/MatchingAnswerRequest.java
src/main/java/org/fpt/studydeck/dto/matching/MatchingSessionResponse.java
src/main/java/org/fpt/studydeck/controller/matching/MatchingSessionController.java
src/test/java/org/fpt/studydeck/service/matching/MatchingSessionServiceTest.java
src/test/java/org/fpt/studydeck/controller/matching/MatchingSessionControllerTest.java
```

Create Practice Test files:

```text
src/main/java/org/fpt/studydeck/domain/practice/PracticeTest.java
src/main/java/org/fpt/studydeck/domain/practice/PracticeTestQuestion.java
src/main/java/org/fpt/studydeck/domain/practice/PracticeTestStatus.java
src/main/java/org/fpt/studydeck/repository/practice/PracticeTestRepository.java
src/main/java/org/fpt/studydeck/service/practice/PracticeTestService.java
src/main/java/org/fpt/studydeck/dto/practice/CreatePracticeTestRequest.java
src/main/java/org/fpt/studydeck/dto/practice/PracticeAnswerRequest.java
src/main/java/org/fpt/studydeck/dto/practice/PracticeTestResponse.java
src/main/java/org/fpt/studydeck/controller/practice/PracticeTestController.java
src/test/java/org/fpt/studydeck/service/practice/PracticeTestServiceTest.java
src/test/java/org/fpt/studydeck/controller/practice/PracticeTestControllerTest.java
```

## Shared Conventions

Use records for DTOs. Use constructor methods on entities for required fields. Do not return JPA entities from controllers. Keep all timestamps as `Instant`. Use `@Transactional` on service write methods. Use `@Transactional(readOnly = true)` on service read methods.

Use these common enum values:

```java
package org.fpt.studydeck.domain.learn;

public enum LearnQuestionType {
    FLASHCARD,
    MULTIPLE_CHOICE,
    WRITTEN,
    TRUE_FALSE
}
```

```java
package org.fpt.studydeck.domain.learn;

public enum PromptSide {
    TERM,
    DEFINITION
}
```

```java
package org.fpt.studydeck.domain.srs;

public enum SrsRating {
    AGAIN,
    HARD,
    GOOD,
    EASY
}
```

## Task 1: Infrastructure, Configuration, Security, And Errors

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/resources/application.yaml`
- Modify: `src/test/java/org/fpt/studydeck/StudyDeckApplicationTests.java`
- Create: `src/main/java/org/fpt/studydeck/config/OpenApiConfig.java`
- Create: `src/main/java/org/fpt/studydeck/config/SecurityConfig.java`
- Create: `src/main/java/org/fpt/studydeck/exception/ApiErrorResponse.java`
- Create: `src/main/java/org/fpt/studydeck/exception/FieldErrorResponse.java`
- Create: `src/main/java/org/fpt/studydeck/exception/GlobalExceptionHandler.java`
- Create: `src/main/java/org/fpt/studydeck/exception/InvalidRequestException.java`
- Create: `src/main/java/org/fpt/studydeck/exception/ResourceConflictException.java`
- Create: `src/main/java/org/fpt/studydeck/exception/ResourceNotFoundException.java`
- Test: `src/test/java/org/fpt/studydeck/exception/GlobalExceptionHandlerTest.java`
- Test: `src/test/java/org/fpt/studydeck/config/SecurityConfigTest.java`

- [ ] **Step 1: Write failing global error handler tests**

Create `src/test/java/org/fpt/studydeck/exception/GlobalExceptionHandlerTest.java`:

```java
package org.fpt.studydeck.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.ThrowingController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsNotFoundBodyForMissingResources() throws Exception {
        mockMvc.perform(get("/throw/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Deck was not found."))
                .andExpect(jsonPath("$.path").value("/throw/not-found"));
    }

    @Test
    void apiErrorResponseStoresFieldErrors() {
        FieldErrorResponse field = new FieldErrorResponse("title", "Title is required.");
        ApiErrorResponse response = new ApiErrorResponse(
                "2026-06-10T00:00:00Z",
                400,
                "Bad Request",
                "Validation failed.",
                "/api/v1/decks",
                java.util.List.of(field)
        );

        assertThat(response.fieldErrors()).containsExactly(field);
    }

    @RestController
    static class ThrowingController {
        @GetMapping("/throw/not-found")
        ResponseEntity<Void> notFound() {
            throw new ResourceNotFoundException("Deck was not found.");
        }
    }
}
```

- [ ] **Step 2: Write failing security test**

Create `src/test/java/org/fpt/studydeck/config/SecurityConfigTest.java`:

```java
package org.fpt.studydeck.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = SecurityConfigTest.PingController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void permitsApiRequestsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/ping"))
                .andExpect(status().isOk());
    }

    @RestController
    static class PingController {
        @GetMapping("/api/v1/ping")
        ResponseEntity<String> ping() {
            return ResponseEntity.ok("pong");
        }
    }
}
```

- [ ] **Step 3: Run tests to verify they fail**

Run:

```powershell
.\mvnw.cmd test -Dtest=GlobalExceptionHandlerTest,SecurityConfigTest
```

Expected: compilation fails because `SecurityConfig`, `GlobalExceptionHandler`, `ApiErrorResponse`, `FieldErrorResponse`, and exception classes do not exist.

- [ ] **Step 4: Implement infrastructure classes**

Create `src/main/java/org/fpt/studydeck/exception/FieldErrorResponse.java`:

```java
package org.fpt.studydeck.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validation error for a single request field.")
public record FieldErrorResponse(
        @Schema(example = "title") String field,
        @Schema(example = "Title is required.") String message
) {
}
```

Create `src/main/java/org/fpt/studydeck/exception/ApiErrorResponse.java`:

```java
package org.fpt.studydeck.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Consistent API error response.")
public record ApiErrorResponse(
        @Schema(example = "2026-06-10T00:00:00Z") String timestamp,
        @Schema(example = "400") int status,
        @Schema(example = "Bad Request") String error,
        @Schema(example = "Validation failed.") String message,
        @Schema(example = "/api/v1/decks") String path,
        List<FieldErrorResponse> fieldErrors
) {
}
```

Create `src/main/java/org/fpt/studydeck/exception/ResourceNotFoundException.java`:

```java
package org.fpt.studydeck.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

Create `src/main/java/org/fpt/studydeck/exception/InvalidRequestException.java`:

```java
package org.fpt.studydeck.exception;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
```

Create `src/main/java/org/fpt/studydeck/exception/ResourceConflictException.java`:

```java
package org.fpt.studydeck.exception;

public class ResourceConflictException extends RuntimeException {
    public ResourceConflictException(String message) {
        super(message);
    }
}
```

Create `src/main/java/org/fpt/studydeck/exception/GlobalExceptionHandler.java`:

```java
package org.fpt.studydeck.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(InvalidRequestException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidRequest(InvalidRequestException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(ResourceConflictException.class)
    ResponseEntity<ApiErrorResponse> handleConflict(ResourceConflictException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<FieldErrorResponse> fields = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed.", request, fields);
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<FieldErrorResponse> fieldErrors
    ) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}
```

Create `src/main/java/org/fpt/studydeck/config/SecurityConfig.java`:

```java
package org.fpt.studydeck.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().permitAll()
                )
                .build();
    }
}
```

Create `src/main/java/org/fpt/studydeck/config/OpenApiConfig.java`:

```java
package org.fpt.studydeck.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI studyDeckOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Study Deck API")
                        .version("v1")
                        .description("Backend API for decks, flashcards, learning modes, and FSRS spaced repetition."));
    }
}
```

- [ ] **Step 5: Update application configuration**

Replace `src/main/resources/application.yaml` with:

```yaml
spring:
  application:
    name: study-deck
  datasource:
    url: ${DB_URL:jdbc:h2:mem:studydeck;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE}
    username: ${DB_USER:sa}
    password: ${DB_PASSWORD:}
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

- [ ] **Step 6: Run tests to verify infrastructure passes**

Run:

```powershell
.\mvnw.cmd test -Dtest=GlobalExceptionHandlerTest,SecurityConfigTest,StudyDeckApplicationTests
```

Expected: all selected tests pass.

- [ ] **Step 7: Commit infrastructure slice**

Run:

```powershell
git add pom.xml src/main/resources/application.yaml src/main/java/org/fpt/studydeck/config src/main/java/org/fpt/studydeck/exception src/test/java/org/fpt/studydeck/config src/test/java/org/fpt/studydeck/exception src/test/java/org/fpt/studydeck/StudyDeckApplicationTests.java
git commit -m "chore: configure api infrastructure"
```

## Task 2: Core Folder, Deck, And Flashcard CRUD

**Files:**
- Create all core content files listed in the File Structure section.
- Test all service and controller files listed for core content.

- [ ] **Step 1: Write failing folder service test**

Create `src/test/java/org/fpt/studydeck/service/deck/FolderServiceTest.java`:

```java
package org.fpt.studydeck.service.deck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.fpt.studydeck.domain.deck.Folder;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.FolderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(FolderService.class)
class FolderServiceTest {

    @Autowired
    private FolderService service;

    @Autowired
    private FolderRepository repository;

    @Test
    void createsFolderWithTrimmedName() {
        Folder folder = service.createFolder("  Languages  ", "  Korean decks  ");

        assertThat(folder.getId()).isNotNull();
        assertThat(folder.getName()).isEqualTo("Languages");
        assertThat(folder.getDescription()).isEqualTo("Korean decks");
        assertThat(folder.getPosition()).isZero();
    }

    @Test
    void rejectsBlankFolderName() {
        assertThatThrownBy(() -> service.createFolder(" ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Folder name is required.");
    }

    @Test
    void throwsWhenFolderMissing() {
        assertThatThrownBy(() -> service.getFolder(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Folder was not found.");
    }
}
```

- [ ] **Step 2: Run folder service test and verify red**

Run:

```powershell
.\mvnw.cmd test -Dtest=FolderServiceTest
```

Expected: compilation fails because `Folder`, `FolderRepository`, and `FolderService` do not exist.

- [ ] **Step 3: Implement folder entity, repository, and service**

Create `src/main/java/org/fpt/studydeck/domain/deck/Folder.java`:

```java
package org.fpt.studydeck.domain.deck;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

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
    private int position;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Folder() {
    }

    private Folder(String name, String description) {
        Instant now = Instant.now();
        this.name = name;
        this.description = description;
        this.position = 0;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Folder create(String name, String description) {
        return new Folder(requireText(name, "Folder name is required."), cleanOptional(description));
    }

    public void rename(String name, String description) {
        this.name = requireText(name, "Folder name is required.");
        this.description = cleanOptional(description);
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPosition() { return position; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    private static String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static String cleanOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
```

Create `src/main/java/org/fpt/studydeck/repository/deck/FolderRepository.java`:

```java
package org.fpt.studydeck.repository.deck;

import org.fpt.studydeck.domain.deck.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Long> {
}
```

Create `src/main/java/org/fpt/studydeck/service/deck/FolderService.java`:

```java
package org.fpt.studydeck.service.deck;

import java.util.List;
import org.fpt.studydeck.domain.deck.Folder;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.FolderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FolderService {

    private final FolderRepository folderRepository;

    public FolderService(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    @Transactional
    public Folder createFolder(String name, String description) {
        return folderRepository.save(Folder.create(name, description));
    }

    @Transactional(readOnly = true)
    public Folder getFolder(Long id) {
        return folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder was not found."));
    }

    @Transactional(readOnly = true)
    public List<Folder> listFolders() {
        return folderRepository.findAll();
    }

    @Transactional
    public Folder updateFolder(Long id, String name, String description) {
        Folder folder = getFolder(id);
        folder.rename(name, description);
        return folder;
    }

    @Transactional
    public void deleteFolder(Long id) {
        if (!folderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Folder was not found.");
        }
        folderRepository.deleteById(id);
    }
}
```

- [ ] **Step 4: Run folder service test and verify green**

Run:

```powershell
.\mvnw.cmd test -Dtest=FolderServiceTest
```

Expected: `FolderServiceTest` passes.

- [ ] **Step 5: Write failing deck and flashcard service tests**

Create `src/test/java/org/fpt/studydeck/service/deck/DeckServiceTest.java`:

```java
package org.fpt.studydeck.service.deck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.DeckVisibility;
import org.fpt.studydeck.domain.deck.Folder;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FolderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({DeckService.class, FolderService.class})
class DeckServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Test
    void createsDeckWithoutFolder() {
        Deck deck = deckService.createDeck(null, "  Korean Basics  ", "  N5-style terms  ");

        assertThat(deck.getId()).isNotNull();
        assertThat(deck.getFolder()).isNull();
        assertThat(deck.getTitle()).isEqualTo("Korean Basics");
        assertThat(deck.getVisibility()).isEqualTo(DeckVisibility.PRIVATE);
    }

    @Test
    void createsDeckInsideFolder() {
        Folder folder = folderRepository.save(Folder.create("Languages", null));

        Deck deck = deckService.createDeck(folder.getId(), "Korean", null);

        assertThat(deck.getFolder().getId()).isEqualTo(folder.getId());
        assertThat(deckRepository.findById(deck.getId())).isPresent();
    }

    @Test
    void rejectsBlankDeckTitle() {
        assertThatThrownBy(() -> deckService.createDeck(null, " ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deck title is required.");
    }
}
```

Create `src/test/java/org/fpt/studydeck/service/deck/FlashcardServiceTest.java`:

```java
package org.fpt.studydeck.service.deck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.Flashcard;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({DeckService.class, FlashcardService.class})
class FlashcardServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Test
    void createsFlashcardWithOptionalImageUrls() {
        Deck deck = deckRepository.save(Deck.create(null, "Korean", null));

        Flashcard flashcard = flashcardService.createFlashcard(
                deck.getId(),
                " 현장 ",
                " site ",
                "https://example.com/term.png",
                "https://example.com/definition.png"
        );

        assertThat(flashcard.getTerm()).isEqualTo("현장");
        assertThat(flashcard.getDefinition()).isEqualTo("site");
        assertThat(flashcard.getTermImageUrl()).isEqualTo("https://example.com/term.png");
        assertThat(flashcard.getDefinitionImageUrl()).isEqualTo("https://example.com/definition.png");
        assertThat(flashcard.isStarred()).isFalse();
        assertThat(flashcard.getPosition()).isZero();
    }

    @Test
    void togglesStarredFlag() {
        Deck deck = deckRepository.save(Deck.create(null, "Korean", null));
        Flashcard flashcard = flashcardRepository.save(Flashcard.create(deck, "현장", "site", null, null, 0));

        Flashcard updated = flashcardService.setStarred(flashcard.getId(), true);

        assertThat(updated.isStarred()).isTrue();
    }

    @Test
    void rejectsBlankTerm() {
        Deck deck = deckRepository.save(Deck.create(null, "Korean", null));

        assertThatThrownBy(() -> flashcardService.createFlashcard(deck.getId(), " ", "site", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Term is required.");
    }
}
```

- [ ] **Step 6: Run deck and flashcard tests and verify red**

Run:

```powershell
.\mvnw.cmd test -Dtest=DeckServiceTest,FlashcardServiceTest
```

Expected: compilation fails because deck and flashcard classes do not exist.

- [ ] **Step 7: Implement deck and flashcard domain/repository/service**

Create `src/main/java/org/fpt/studydeck/domain/deck/DeckVisibility.java`:

```java
package org.fpt.studydeck.domain.deck;

public enum DeckVisibility {
    PRIVATE,
    PUBLIC
}
```

Create `src/main/java/org/fpt/studydeck/domain/deck/Deck.java`:

```java
package org.fpt.studydeck.domain.deck;

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
import java.time.Instant;

@Entity
@Table(name = "decks")
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeckVisibility visibility;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Deck() {
    }

    private Deck(Folder folder, String title, String description) {
        Instant now = Instant.now();
        this.folder = folder;
        this.title = requireText(title, "Deck title is required.");
        this.description = cleanOptional(description);
        this.visibility = DeckVisibility.PRIVATE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Deck create(Folder folder, String title, String description) {
        return new Deck(folder, title, description);
    }

    public void update(String title, String description) {
        this.title = requireText(title, "Deck title is required.");
        this.description = cleanOptional(description);
        this.updatedAt = Instant.now();
    }

    public void moveToFolder(Folder folder) {
        this.folder = folder;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Folder getFolder() { return folder; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public DeckVisibility getVisibility() { return visibility; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    private static String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static String cleanOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
```

Create `src/main/java/org/fpt/studydeck/domain/deck/Flashcard.java`:

```java
package org.fpt.studydeck.domain.deck;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "flashcards")
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @Column(nullable = false, length = 255)
    private String term;

    @Column(nullable = false, length = 1000)
    private String definition;

    @Column(name = "term_image_url", length = 2048)
    private String termImageUrl;

    @Column(name = "definition_image_url", length = 2048)
    private String definitionImageUrl;

    @Column(nullable = false)
    private boolean starred;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Flashcard() {
    }

    private Flashcard(Deck deck, String term, String definition, String termImageUrl, String definitionImageUrl, int position) {
        Instant now = Instant.now();
        this.deck = deck;
        this.term = requireText(term, "Term is required.");
        this.definition = requireText(definition, "Definition is required.");
        this.termImageUrl = cleanOptional(termImageUrl);
        this.definitionImageUrl = cleanOptional(definitionImageUrl);
        this.starred = false;
        this.position = position;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Flashcard create(Deck deck, String term, String definition, String termImageUrl, String definitionImageUrl, int position) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck is required.");
        }
        return new Flashcard(deck, term, definition, termImageUrl, definitionImageUrl, position);
    }

    public void update(String term, String definition, String termImageUrl, String definitionImageUrl) {
        this.term = requireText(term, "Term is required.");
        this.definition = requireText(definition, "Definition is required.");
        this.termImageUrl = cleanOptional(termImageUrl);
        this.definitionImageUrl = cleanOptional(definitionImageUrl);
        this.updatedAt = Instant.now();
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Deck getDeck() { return deck; }
    public String getTerm() { return term; }
    public String getDefinition() { return definition; }
    public String getTermImageUrl() { return termImageUrl; }
    public String getDefinitionImageUrl() { return definitionImageUrl; }
    public boolean isStarred() { return starred; }
    public int getPosition() { return position; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    private static String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static String cleanOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
```

Create `DeckRepository`, `FlashcardRepository`, `DeckService`, and `FlashcardService` with these signatures:

```java
package org.fpt.studydeck.repository.deck;

import java.util.List;
import org.fpt.studydeck.domain.deck.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckRepository extends JpaRepository<Deck, Long> {
    List<Deck> findByFolderId(Long folderId);
}
```

```java
package org.fpt.studydeck.repository.deck;

import java.util.List;
import org.fpt.studydeck.domain.deck.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findByDeckIdOrderByPositionAscIdAsc(Long deckId);
    long countByDeckId(Long deckId);
    long countByDeckIdAndStarredTrue(Long deckId);
}
```

```java
package org.fpt.studydeck.service.deck;

import java.util.List;
import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.Folder;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FolderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeckService {

    private final DeckRepository deckRepository;
    private final FolderRepository folderRepository;

    public DeckService(DeckRepository deckRepository, FolderRepository folderRepository) {
        this.deckRepository = deckRepository;
        this.folderRepository = folderRepository;
    }

    @Transactional
    public Deck createDeck(Long folderId, String title, String description) {
        Folder folder = folderId == null ? null : findFolder(folderId);
        return deckRepository.save(Deck.create(folder, title, description));
    }

    @Transactional(readOnly = true)
    public Deck getDeck(Long id) {
        return deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deck was not found."));
    }

    @Transactional(readOnly = true)
    public List<Deck> listDecks() {
        return deckRepository.findAll();
    }

    @Transactional
    public Deck updateDeck(Long id, String title, String description) {
        Deck deck = getDeck(id);
        deck.update(title, description);
        return deck;
    }

    @Transactional
    public Deck moveDeckToFolder(Long folderId, Long deckId) {
        Deck deck = getDeck(deckId);
        deck.moveToFolder(findFolder(folderId));
        return deck;
    }

    @Transactional
    public Deck removeDeckFromFolder(Long folderId, Long deckId) {
        Deck deck = getDeck(deckId);
        if (deck.getFolder() != null && deck.getFolder().getId().equals(folderId)) {
            deck.moveToFolder(null);
        }
        return deck;
    }

    @Transactional
    public void deleteDeck(Long id) {
        if (!deckRepository.existsById(id)) {
            throw new ResourceNotFoundException("Deck was not found.");
        }
        deckRepository.deleteById(id);
    }

    private Folder findFolder(Long folderId) {
        return folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder was not found."));
    }
}
```

```java
package org.fpt.studydeck.service.deck;

import java.util.List;
import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.Flashcard;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final DeckRepository deckRepository;

    public FlashcardService(FlashcardRepository flashcardRepository, DeckRepository deckRepository) {
        this.flashcardRepository = flashcardRepository;
        this.deckRepository = deckRepository;
    }

    @Transactional
    public Flashcard createFlashcard(Long deckId, String term, String definition, String termImageUrl, String definitionImageUrl) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck was not found."));
        int position = (int) flashcardRepository.countByDeckId(deckId);
        return flashcardRepository.save(Flashcard.create(deck, term, definition, termImageUrl, definitionImageUrl, position));
    }

    @Transactional(readOnly = true)
    public Flashcard getFlashcard(Long id) {
        return flashcardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard was not found."));
    }

    @Transactional(readOnly = true)
    public List<Flashcard> listDeckFlashcards(Long deckId) {
        if (!deckRepository.existsById(deckId)) {
            throw new ResourceNotFoundException("Deck was not found.");
        }
        return flashcardRepository.findByDeckIdOrderByPositionAscIdAsc(deckId);
    }

    @Transactional
    public Flashcard updateFlashcard(Long id, String term, String definition, String termImageUrl, String definitionImageUrl) {
        Flashcard flashcard = getFlashcard(id);
        flashcard.update(term, definition, termImageUrl, definitionImageUrl);
        return flashcard;
    }

    @Transactional
    public Flashcard setStarred(Long id, boolean starred) {
        Flashcard flashcard = getFlashcard(id);
        flashcard.setStarred(starred);
        return flashcard;
    }

    @Transactional
    public void deleteFlashcard(Long id) {
        if (!flashcardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Flashcard was not found.");
        }
        flashcardRepository.deleteById(id);
    }
}
```

- [ ] **Step 8: Run core service tests and verify green**

Run:

```powershell
.\mvnw.cmd test -Dtest=FolderServiceTest,DeckServiceTest,FlashcardServiceTest
```

Expected: all selected tests pass.

- [ ] **Step 9: Add DTOs and controllers with MockMvc tests**

Create controller tests for at least these behaviors:

```java
// src/test/java/org/fpt/studydeck/controller/deck/FolderControllerTest.java
@Test
void createFolderReturnsCreatedFolder() throws Exception {
    mockMvc.perform(post("/api/v1/folders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {"name":"Languages","description":"Korean decks"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Languages"));
}
```

```java
// src/test/java/org/fpt/studydeck/controller/deck/DeckControllerTest.java
@Test
void createDeckReturnsCreatedDeck() throws Exception {
    mockMvc.perform(post("/api/v1/decks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {"title":"Korean Basics","description":"Starter words"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Korean Basics"));
}
```

```java
// src/test/java/org/fpt/studydeck/controller/deck/FlashcardControllerTest.java
@Test
void starFlashcardReturnsUpdatedFlag() throws Exception {
    mockMvc.perform(patch("/api/v1/flashcards/1/star")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {"starred":true}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.starred").value(true));
}
```

Create DTO records with validation:

```java
package org.fpt.studydeck.dto.deck;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFolderRequest(
        @NotBlank(message = "Folder name is required.")
        @Size(max = 120, message = "Folder name must be at most 120 characters.")
        String name,
        @Size(max = 1000, message = "Folder description must be at most 1000 characters.")
        String description
) {
}
```

Create response mappers as static `from` methods on response records:

```java
package org.fpt.studydeck.dto.deck;

import java.time.Instant;
import org.fpt.studydeck.domain.deck.Folder;

public record FolderResponse(
        Long id,
        String name,
        String description,
        int position,
        Instant createdAt,
        Instant updatedAt
) {
    public static FolderResponse from(Folder folder) {
        return new FolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getDescription(),
                folder.getPosition(),
                folder.getCreatedAt(),
                folder.getUpdatedAt()
        );
    }
}
```

Create controllers under `/api/v1` and annotate request bodies with `@Valid`.

- [ ] **Step 10: Run core controller and service tests**

Run:

```powershell
.\mvnw.cmd test -Dtest=FolderServiceTest,DeckServiceTest,FlashcardServiceTest,FolderControllerTest,DeckControllerTest,FlashcardControllerTest
```

Expected: all selected tests pass.

- [ ] **Step 11: Commit core CRUD slice**

Run:

```powershell
git add src/main/java/org/fpt/studydeck/domain/deck src/main/java/org/fpt/studydeck/repository/deck src/main/java/org/fpt/studydeck/service/deck src/main/java/org/fpt/studydeck/dto/deck src/main/java/org/fpt/studydeck/controller/deck src/test/java/org/fpt/studydeck/service/deck src/test/java/org/fpt/studydeck/controller/deck
git commit -m "feat: add deck and flashcard crud"
```

## Task 3: Deck Summary, Viewer Cards, And Sorting Sessions

**Files:**
- Create viewer/sorting files listed in File Structure.

- [ ] **Step 1: Write failing service tests for summary and viewer**

Create tests that assert:

```java
assertThat(summary.totalCards()).isEqualTo(3);
assertThat(summary.starredCards()).isEqualTo(1);
assertThat(summary.availableModes()).contains("FLASHCARDS", "LEARN", "MATCH", "PRACTICE_TEST", "SPACED_REPETITION");
```

```java
assertThat(viewerCards).extracting(ViewerCardResponse::term)
        .containsExactly("first", "second", "third");
```

```java
assertThat(starredCards).hasSize(1);
assertThat(starredCards.getFirst().starred()).isTrue();
```

- [ ] **Step 2: Run summary/viewer tests and verify red**

Run:

```powershell
.\mvnw.cmd test -Dtest=DeckSummaryServiceTest,ViewerCardServiceTest
```

Expected: compilation fails because summary and viewer service classes do not exist.

- [ ] **Step 3: Implement deck summary and viewer services**

Create `DeckSummaryResponse`:

```java
package org.fpt.studydeck.dto.deck;

import java.util.List;

public record DeckSummaryResponse(
        Long deckId,
        long totalCards,
        long starredCards,
        long dueSrsCards,
        long newCards,
        long learningCards,
        long reviewCards,
        List<String> availableModes
) {
}
```

Create `ViewerCardResponse`:

```java
package org.fpt.studydeck.dto.deck;

public record ViewerCardResponse(
        Long id,
        String term,
        String definition,
        String termImageUrl,
        String definitionImageUrl,
        boolean starred,
        int position
) {
}
```

Create `DeckSummaryService` that uses `FlashcardRepository.countByDeckId`, `countByDeckIdAndStarredTrue`, and returns zeroes for SRS counts until Task 4 wires SRS repository counts.

Create `ViewerCardService` that supports:

```java
public List<ViewerCardResponse> getCards(Long deckId, String sort, String mode)
```

Use `original`, `shuffle`, and `starred`. Invalid sort throws `InvalidRequestException("Unsupported card sort.")`.

- [ ] **Step 4: Run summary/viewer tests and verify green**

Run:

```powershell
.\mvnw.cmd test -Dtest=DeckSummaryServiceTest,ViewerCardServiceTest
```

Expected: selected tests pass.

- [ ] **Step 5: Write failing sorting session tests**

Assert creation creates one item per selected card and answers increment counts:

```java
SortingSessionResponse session = service.createSession(deckId, new CreateSortingSessionRequest(false, false));
assertThat(session.items()).hasSize(3);

SortingSessionResponse answered = service.answer(session.id(), new SortingAnswerRequest(itemId, SortingAnswer.KNOW));
assertThat(answered.knownCount()).isEqualTo(1);
assertThat(answered.doNotKnowCount()).isZero();
```

- [ ] **Step 6: Implement sorting domain, repository, service, DTO, and controller**

Create enums:

```java
package org.fpt.studydeck.domain.sorting;

public enum SortingAnswer {
    KNOW,
    DO_NOT_KNOW
}
```

```java
package org.fpt.studydeck.domain.sorting;

public enum SortingSessionStatus {
    ACTIVE,
    COMPLETED
}
```

Create `SortingSession` with `Deck deck`, `SortingSessionStatus status`, `Instant startedAt`, `Instant completedAt`, and `List<SortingSessionItem> items`.

Create `SortingSessionItem` with `SortingSession session`, `Flashcard flashcard`, nullable `SortingAnswer answer`, and nullable `Instant answeredAt`.

Create DTO records:

```java
public record CreateSortingSessionRequest(boolean starredOnly, boolean shuffle) {}
public record SortingAnswerRequest(Long itemId, SortingAnswer answer) {}
public record SortingSessionResponse(Long id, String status, int knownCount, int doNotKnowCount, List<ViewerCardResponse> items) {}
```

Create endpoints:

```text
POST /api/v1/decks/{deckId}/sorting-sessions
GET  /api/v1/sorting-sessions/{sessionId}
POST /api/v1/sorting-sessions/{sessionId}/answers
```

- [ ] **Step 7: Run sorting tests**

Run:

```powershell
.\mvnw.cmd test -Dtest=DeckSummaryServiceTest,ViewerCardServiceTest,SortingSessionServiceTest
```

Expected: selected tests pass.

- [ ] **Step 8: Commit summary/viewer/sorting slice**

Run:

```powershell
git add src/main/java/org/fpt/studydeck/domain/sorting src/main/java/org/fpt/studydeck/repository/sorting src/main/java/org/fpt/studydeck/service/deck src/main/java/org/fpt/studydeck/service/sorting src/main/java/org/fpt/studydeck/dto/deck src/main/java/org/fpt/studydeck/dto/sorting src/main/java/org/fpt/studydeck/controller/deck src/main/java/org/fpt/studydeck/controller/sorting src/test/java/org/fpt/studydeck/service/deck src/test/java/org/fpt/studydeck/service/sorting
git commit -m "feat: add deck summary and sorting sessions"
```

## Task 4: FSRS Spaced Repetition

**Files:**
- Modify: `pom.xml`
- Create SRS files listed in File Structure.

- [ ] **Step 1: Add java-fsrs dependency only**

Add this dependency to `pom.xml`:

```xml
<dependency>
  <groupId>io.github.open-spaced-repetition</groupId>
  <artifactId>fsrs</artifactId>
  <version>1.0.0</version>
</dependency>
```

This coordinate was checked against Maven Central while writing the plan.

- [ ] **Step 2: Write failing FSRS scheduler tests**

Create `src/test/java/org/fpt/studydeck/service/srs/FsrsSchedulerTest.java`:

```java
package org.fpt.studydeck.service.srs;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.fpt.studydeck.domain.srs.SrsRating;
import org.junit.jupiter.api.Test;

class FsrsSchedulerTest {

    @Test
    void firstReviewWithGoodSchedulesCardInFuture() {
        FsrsScheduler scheduler = new FsrsScheduler();

        FsrsScheduler.ScheduledReview review = scheduler.reviewNewCard(SrsRating.GOOD, Instant.parse("2026-06-10T00:00:00Z"));

        assertThat(review.dueAt()).isAfter("2026-06-10T00:00:00Z");
        assertThat(review.state()).isNotNull();
        assertThat(review.fsrsCardJson()).contains("due");
    }
}
```

- [ ] **Step 3: Run FSRS scheduler test and verify red**

Run:

```powershell
.\mvnw.cmd test -Dtest=FsrsSchedulerTest
```

Expected: compilation fails because `FsrsScheduler` and SRS enums do not exist.

- [ ] **Step 4: Implement SRS enums, entities, repositories, and scheduler adapter**

Create `SrsRating` and `SrsState`:

```java
package org.fpt.studydeck.domain.srs;

public enum SrsState {
    NEW,
    LEARNING,
    REVIEW,
    RELEARNING
}
```

Create `SrsCardState` with fields from the design and a method:

```java
public void applyReview(SrsRating rating, SrsState nextState, Instant dueAt, double stability, double difficulty, int scheduledDays, String fsrsCardJson)
```

Create `SrsReviewLog` as immutable after creation with previous/next state and due timestamps.

Create `FsrsScheduler` adapter. Keep java-fsrs usage inside this class only. Expose:

```java
public ScheduledReview reviewNewCard(SrsRating rating, Instant reviewedAt)
public ScheduledReview reviewExistingCard(String fsrsCardJson, SrsRating rating, Instant reviewedAt)

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
) {}
```

Map FSRS ratings:

```text
AGAIN -> Again
HARD  -> Hard
GOOD  -> Good
EASY  -> Easy
```

- [ ] **Step 5: Run scheduler test and verify green**

Run:

```powershell
.\mvnw.cmd test -Dtest=FsrsSchedulerTest
```

Expected: selected test passes.

- [ ] **Step 6: Write failing SRS review service tests**

Create tests that assert:

```java
SrsReviewResponse response = service.review(flashcardId, new SrsReviewRequest(SrsRating.GOOD, 1200));
assertThat(response.rating()).isEqualTo(SrsRating.GOOD);
assertThat(response.dueAt()).isAfter(Instant.now().minusSeconds(1));
assertThat(reviewLogRepository.count()).isEqualTo(1);
```

```java
assertThat(service.dueCards(deckId, Instant.parse("2026-06-10T00:00:00Z"))).hasSize(1);
```

- [ ] **Step 7: Implement SRS service, stats, DTOs, and controller**

Create DTO records:

```java
public record SrsReviewRequest(SrsRating rating, long durationMs) {}
public record SrsReviewResponse(Long flashcardId, SrsRating rating, SrsState state, Instant dueAt, int reps, int lapses) {}
public record SrsCardStateResponse(Long flashcardId, SrsState state, Instant dueAt, double stability, double difficulty, int reps, int lapses) {}
public record SrsDueCardResponse(Long flashcardId, String term, String definition, Instant dueAt, SrsState state) {}
public record SrsStatsResponse(long newCards, long learningCards, long reviewCards, long dueCards) {}
```

Create endpoints:

```text
GET  /api/v1/decks/{deckId}/srs/due
POST /api/v1/decks/{deckId}/srs/review-sessions
GET  /api/v1/srs/review-sessions/{sessionId}
POST /api/v1/srs/review-sessions/{sessionId}/reviews
GET  /api/v1/flashcards/{flashcardId}/srs-state
GET  /api/v1/decks/{deckId}/srs/stats
```

For MVP, implement review-session endpoints as thin wrappers over due cards and review submission. Persisting a dedicated review session table can be deferred until UI needs strict session resume; review logs and card states are the source of truth.

- [ ] **Step 8: Run SRS tests**

Run:

```powershell
.\mvnw.cmd test -Dtest=FsrsSchedulerTest,SrsReviewServiceTest,SrsControllerTest
```

Expected: selected tests pass.

- [ ] **Step 9: Update deck summary to use SRS counts**

Change `DeckSummaryService` to include counts from `SrsCardStateRepository`:

```java
long dueSrsCards = srsCardStateRepository.countDueByDeckId(deckId, Instant.now());
long newCards = totalCards - srsCardStateRepository.countByDeckId(deckId);
long learningCards = srsCardStateRepository.countByDeckIdAndState(deckId, SrsState.LEARNING);
long reviewCards = srsCardStateRepository.countByDeckIdAndState(deckId, SrsState.REVIEW);
```

- [ ] **Step 10: Run SRS plus summary tests**

Run:

```powershell
.\mvnw.cmd test -Dtest=DeckSummaryServiceTest,FsrsSchedulerTest,SrsReviewServiceTest,SrsControllerTest
```

Expected: selected tests pass.

- [ ] **Step 11: Commit SRS slice**

Run:

```powershell
git add pom.xml src/main/java/org/fpt/studydeck/domain/srs src/main/java/org/fpt/studydeck/repository/srs src/main/java/org/fpt/studydeck/service/srs src/main/java/org/fpt/studydeck/dto/srs src/main/java/org/fpt/studydeck/controller/srs src/main/java/org/fpt/studydeck/service/deck/DeckSummaryService.java src/test/java/org/fpt/studydeck/service/srs src/test/java/org/fpt/studydeck/controller/srs src/test/java/org/fpt/studydeck/service/deck/DeckSummaryServiceTest.java
git commit -m "feat: add fsrs spaced repetition"
```

## Task 5: Learn Sessions

**Files:**
- Create Learn files listed in File Structure.

- [ ] **Step 1: Write failing learn service tests**

Create tests that assert:

```java
LearnSessionResponse session = service.createSession(deckId, new CreateLearnSessionRequest(7, true, true, false, true, false, true));
assertThat(session.status()).isEqualTo("ACTIVE");
assertThat(session.items()).hasSize(3);
```

```java
LearnSessionResponse answered = service.answer(session.id(), new LearnAnswerRequest(itemId, "site"));
assertThat(answered.correctCount()).isEqualTo(1);
```

```java
assertThatThrownBy(() -> service.answer(completedSessionId, request))
        .isInstanceOf(ResourceConflictException.class)
        .hasMessage("Learn session is already completed.");
```

- [ ] **Step 2: Run learn tests and verify red**

Run:

```powershell
.\mvnw.cmd test -Dtest=LearnSessionServiceTest
```

Expected: compilation fails because Learn classes do not exist.

- [ ] **Step 3: Implement Learn domain and repository**

Create enums:

```java
package org.fpt.studydeck.domain.learn;

public enum LearnSessionStatus {
    ACTIVE,
    COMPLETED
}
```

Create `LearnSession` with `Deck deck`, `LearnSessionStatus status`, `settingsJson`, timestamps, and one-to-many items.

Create `LearnSessionItem` with `Flashcard flashcard`, `LearnQuestionType questionType`, `PromptSide promptSide`, `status`, `attempts`, `correctCount`, `wrongCount`, and `lastAnsweredAt`.

- [ ] **Step 4: Implement Learn DTOs, service, and controller**

Create DTO records:

```java
public record CreateLearnSessionRequest(
        int lengthOfRounds,
        boolean flashcards,
        boolean multipleChoice,
        boolean written,
        boolean trueFalse,
        boolean starredOnly,
        boolean shuffleTerms
) {}
```

```java
public record LearnAnswerRequest(Long itemId, String answer) {}
public record LearnSessionResponse(Long id, String status, int totalItems, int correctCount, int wrongCount, List<LearnSessionItemResponse> items) {}
public record LearnSessionItemResponse(Long id, Long flashcardId, LearnQuestionType questionType, PromptSide promptSide, String prompt, String answer, int attempts) {}
```

Implement normalized exact grading:

```java
private boolean isCorrect(String submitted, String expected) {
    return submitted != null && submitted.trim().equalsIgnoreCase(expected.trim());
}
```

Create endpoints:

```text
POST /api/v1/decks/{deckId}/learn-sessions
GET  /api/v1/learn-sessions/{sessionId}
POST /api/v1/learn-sessions/{sessionId}/answers
POST /api/v1/learn-sessions/{sessionId}/complete
```

- [ ] **Step 5: Run learn tests**

Run:

```powershell
.\mvnw.cmd test -Dtest=LearnSessionServiceTest,LearnSessionControllerTest
```

Expected: selected tests pass.

- [ ] **Step 6: Commit Learn slice**

Run:

```powershell
git add src/main/java/org/fpt/studydeck/domain/learn src/main/java/org/fpt/studydeck/repository/learn src/main/java/org/fpt/studydeck/service/learn src/main/java/org/fpt/studydeck/dto/learn src/main/java/org/fpt/studydeck/controller/learn src/test/java/org/fpt/studydeck/service/learn src/test/java/org/fpt/studydeck/controller/learn
git commit -m "feat: add learn sessions"
```

## Task 6: Matching Sessions

**Files:**
- Create Matching files listed in File Structure.

- [ ] **Step 1: Write failing matching service tests**

Create tests that assert:

```java
MatchingSessionResponse session = service.createSession(deckId, new CreateMatchingSessionRequest(10, false));
assertThat(session.items()).hasSize(10);
assertThat(session.status()).isEqualTo("ACTIVE");
```

```java
MatchingSessionResponse matched = service.match(session.id(), new MatchingAnswerRequest(itemId));
assertThat(matched.matchedCount()).isEqualTo(1);
```

```java
assertThatThrownBy(() -> service.createSession(deckId, new CreateMatchingSessionRequest(10, true)))
        .isInstanceOf(InvalidRequestException.class)
        .hasMessage("Not enough cards are available for matching.");
```

- [ ] **Step 2: Run matching tests and verify red**

Run:

```powershell
.\mvnw.cmd test -Dtest=MatchingSessionServiceTest
```

Expected: compilation fails because Matching classes do not exist.

- [ ] **Step 3: Implement Matching domain, repository, DTOs, service, and controller**

Create enum:

```java
package org.fpt.studydeck.domain.matching;

public enum MatchingSessionStatus {
    ACTIVE,
    COMPLETED
}
```

Create DTO records:

```java
public record CreateMatchingSessionRequest(int cardCount, boolean starredOnly) {}
public record MatchingAnswerRequest(Long itemId) {}
public record MatchingSessionResponse(Long id, String status, int cardCount, int matchedCount, long durationMs, List<MatchingSessionItemResponse> items) {}
public record MatchingSessionItemResponse(Long id, Long flashcardId, String term, String definition, boolean matched) {}
```

Create endpoints:

```text
POST /api/v1/decks/{deckId}/matching-sessions
GET  /api/v1/matching-sessions/{sessionId}
POST /api/v1/matching-sessions/{sessionId}/matches
POST /api/v1/matching-sessions/{sessionId}/complete
```

- [ ] **Step 4: Run matching tests**

Run:

```powershell
.\mvnw.cmd test -Dtest=MatchingSessionServiceTest,MatchingSessionControllerTest
```

Expected: selected tests pass.

- [ ] **Step 5: Commit Matching slice**

Run:

```powershell
git add src/main/java/org/fpt/studydeck/domain/matching src/main/java/org/fpt/studydeck/repository/matching src/main/java/org/fpt/studydeck/service/matching src/main/java/org/fpt/studydeck/dto/matching src/main/java/org/fpt/studydeck/controller/matching src/test/java/org/fpt/studydeck/service/matching src/test/java/org/fpt/studydeck/controller/matching
git commit -m "feat: add matching sessions"
```

## Task 7: Practice Tests

**Files:**
- Create Practice Test files listed in File Structure.

- [ ] **Step 1: Write failing practice service tests**

Create tests that assert:

```java
PracticeTestResponse test = service.createPracticeTest(deckId, new CreatePracticeTestRequest(5, true, true, false, false, false, true));
assertThat(test.questions()).hasSize(5);
assertThat(test.status()).isEqualTo("ACTIVE");
```

```java
PracticeTestResponse answered = service.answer(test.id(), new PracticeAnswerRequest(questionId, "site"));
assertThat(answered.answeredCount()).isEqualTo(1);
```

```java
PracticeTestResponse submitted = service.submit(test.id());
assertThat(submitted.status()).isEqualTo("SUBMITTED");
assertThat(submitted.scorePercent()).isBetween(0.0, 100.0);
```

- [ ] **Step 2: Run practice tests and verify red**

Run:

```powershell
.\mvnw.cmd test -Dtest=PracticeTestServiceTest
```

Expected: compilation fails because Practice classes do not exist.

- [ ] **Step 3: Implement Practice domain, repository, DTOs, service, and controller**

Create enum:

```java
package org.fpt.studydeck.domain.practice;

public enum PracticeTestStatus {
    ACTIVE,
    SUBMITTED
}
```

Create DTO records:

```java
public record CreatePracticeTestRequest(
        int questionCount,
        boolean multipleChoice,
        boolean written,
        boolean trueFalse,
        boolean starredOnly,
        boolean answerWithTerm,
        boolean answerWithDefinition
) {}
```

```java
public record PracticeAnswerRequest(Long questionId, String answer) {}
public record PracticeTestResponse(Long id, String status, int questionCount, int answeredCount, double scorePercent, List<PracticeQuestionResponse> questions) {}
public record PracticeQuestionResponse(Long id, Long flashcardId, LearnQuestionType questionType, PromptSide promptSide, String prompt, String submittedAnswer, Boolean correct) {}
```

Create endpoints:

```text
POST /api/v1/decks/{deckId}/practice-tests
GET  /api/v1/practice-tests/{practiceTestId}
POST /api/v1/practice-tests/{practiceTestId}/answers
POST /api/v1/practice-tests/{practiceTestId}/submit
```

Use the same normalized exact grading helper as Learn.

- [ ] **Step 4: Run practice tests**

Run:

```powershell
.\mvnw.cmd test -Dtest=PracticeTestServiceTest,PracticeTestControllerTest
```

Expected: selected tests pass.

- [ ] **Step 5: Commit Practice Test slice**

Run:

```powershell
git add src/main/java/org/fpt/studydeck/domain/practice src/main/java/org/fpt/studydeck/repository/practice src/main/java/org/fpt/studydeck/service/practice src/main/java/org/fpt/studydeck/dto/practice src/main/java/org/fpt/studydeck/controller/practice src/test/java/org/fpt/studydeck/service/practice src/test/java/org/fpt/studydeck/controller/practice
git commit -m "feat: add practice tests"
```

## Task 8: Final Verification And API Polish

**Files:**
- Modify: `src/test/java/org/fpt/studydeck/StudyDeckApplicationTests.java`
- Modify controllers and DTOs touched by prior tasks as needed for OpenAPI annotations.

- [ ] **Step 1: Add context and OpenAPI smoke assertions**

Update `StudyDeckApplicationTests`:

```java
package org.fpt.studydeck;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class StudyDeckApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }
}
```

- [ ] **Step 2: Run full test suite**

Run:

```powershell
.\mvnw.cmd test
```

Expected: all tests pass with no failures or errors.

- [ ] **Step 3: Start the API locally**

Run:

```powershell
.\mvnw.cmd spring-boot:run
```

Expected: application starts on port `8080` and logs Spring Boot startup complete.

- [ ] **Step 4: Verify Swagger and OpenAPI endpoints**

In a separate PowerShell session, run:

```powershell
Invoke-WebRequest -Uri http://localhost:8080/v3/api-docs -UseBasicParsing | Select-Object -ExpandProperty StatusCode
Invoke-WebRequest -Uri http://localhost:8080/swagger-ui.html -UseBasicParsing | Select-Object -ExpandProperty StatusCode
```

Expected: both commands return `200`.

- [ ] **Step 5: Stop the local API**

Stop the Spring Boot process with `Ctrl+C`.

Expected: terminal returns to the PowerShell prompt.

- [ ] **Step 6: Commit final polish**

Run:

```powershell
git add src/main/java src/test/java src/main/resources pom.xml
git commit -m "test: verify study deck api"
```

## Self-Review Results

Spec coverage:

- Core CRUD is covered by Task 2.
- Optional image URL fields are covered by Task 2.
- Starred terms are covered by Task 2 and reused in later modes.
- Deck summary and card sorting are covered by Task 3.
- FSRS spaced repetition, review logs, and SRS stats are covered by Task 4.
- Learn sessions are covered by Task 5.
- Matching sessions are covered by Task 6.
- Practice tests are covered by Task 7.
- OpenAPI, permissive MVP security, global error handling, H2/MySQL config, and final verification are covered by Tasks 1 and 8.

Placeholder scan:

- The plan contains concrete paths, commands, and implementation details for each slice.

Type consistency:

- Shared enums use `LearnQuestionType`, `PromptSide`, `SrsRating`, `SrsState`, and session status enums consistently.
- DTO names match controller/service task references.
- Repository/service/controller package names match the approved layered architecture.
