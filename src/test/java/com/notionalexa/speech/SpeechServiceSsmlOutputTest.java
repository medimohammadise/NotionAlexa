package com.notionalexa.speech;

import com.notionalexa.notion.model.NotionNote;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the exact SSML structure produced for notes with different language combinations.
 */
class SpeechServiceSsmlOutputTest {

    private final SpeechService service = new SpeechService(new LanguageDetectionService());

    @Test
    void germanNote_ssmlContainsLangTag() {
        List<NotionNote> notes = List.of(
            new NotionNote("1", "Guten Morgen", null,
                "Das ist aber sehr wichtig. Heute ist ein guter Tag.",
                null, null)
        );
        String ssml = service.buildNotesSpeech(notes);
        assertThat(ssml).contains("<lang xml:lang=\"de-DE\">");
        assertThat(ssml).contains("Guten Morgen");
        assertThat(ssml).contains("Das ist aber sehr wichtig");
    }

    @Test
    void mixedLanguageNote_noDoublePeriods() {
        List<NotionNote> notes = List.of(
            new NotionNote("1", "Meeting Notes",
                "Status: Active",
                "This is the project overview. Guten Morgen, wir beginnen jetzt. Back to English.",
                null, null)
        );
        String ssml = service.buildNotesSpeech(notes);
        // No double periods (the old ". " separator on top of sentence-ending "." is fixed)
        assertThat(ssml).doesNotContain("..");
        // German sentence gets lang tag, English sentences do not
        assertThat(ssml).contains("<lang xml:lang=\"de-DE\">");
        assertThat(ssml).contains("Guten Morgen");
        assertThat(ssml).contains("This is the project overview");
        // Sections separated by break tags, not periods
        assertThat(ssml).contains("<break time=\"300ms\"/>");
    }

    @Test
    void englishOnlyNote_noLangTags() {
        List<NotionNote> notes = List.of(
            new NotionNote("1", "Project Update", "Status: Done",
                "The project has been completed successfully.",
                null, null)
        );
        String ssml = service.buildNotesSpeech(notes);
        assertThat(ssml).doesNotContain("<lang xml:lang=");
        assertThat(ssml).contains("Project Update");
        assertThat(ssml).contains("project has been completed");
    }
}
