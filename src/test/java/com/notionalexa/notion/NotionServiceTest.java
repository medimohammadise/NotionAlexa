package com.notionalexa.notion;

import com.notionalexa.notion.model.NotionNote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotionServiceTest {

    private NotionConfig notionConfig;
    private NotionService notionService;

    @BeforeEach
    void setUp() {
        notionConfig = new NotionConfig();
        notionConfig.setApiKey("test-api-key");
        notionConfig.setDatabaseId("test-database-id");
        notionConfig.setMaxResults(5);
        notionService = new NotionService(notionConfig);
    }

    @Test
    void getRecentNotes_withEmptyDatabaseId_returnsEmptyList() {
        notionConfig.setDatabaseId("");
        List<NotionNote> notes = notionService.getRecentNotes();
        assertThat(notes).isEmpty();
    }

    @Test
    void getRecentNotes_withNullDatabaseId_returnsEmptyList() {
        notionConfig.setDatabaseId(null);
        List<NotionNote> notes = notionService.getRecentNotes();
        assertThat(notes).isEmpty();
    }

    @Test
    void getRecentNotes_withInvalidApiKey_returnsEmptyList() {
        // Uses invalid API key — the SDK will fail with an auth error
        // The service should catch the exception and return an empty list
        List<NotionNote> notes = notionService.getRecentNotes();
        assertThat(notes).isEmpty();
    }
}
