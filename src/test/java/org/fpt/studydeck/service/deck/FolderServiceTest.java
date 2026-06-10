package org.fpt.studydeck.service.deck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(FolderService.class)
class FolderServiceTest {

    @Autowired
    private FolderService folderService;

    @Test
    void createsFolderWithTrimmedName() {
        var folder = folderService.createFolder("  Languages  ", "  Korean decks  ");

        assertThat(folder.getId()).isNotNull();
        assertThat(folder.getName()).isEqualTo("Languages");
        assertThat(folder.getDescription()).isEqualTo("Korean decks");
        assertThat(folder.getPosition()).isZero();
    }

    @Test
    void rejectsBlankFolderName() {
        assertThatThrownBy(() -> folderService.createFolder(" ", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Folder name is required.");
    }

    @Test
    void throwsWhenFolderMissing() {
        assertThatThrownBy(() -> folderService.getFolder(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Folder was not found.");
    }
}
