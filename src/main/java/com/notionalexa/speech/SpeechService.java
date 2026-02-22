package com.notionalexa.speech;

import com.notionalexa.notion.model.NotionNote;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SpeechService {

    private static final int MAX_SSML_LENGTH = 8000;
    /** Pause between notes — longer to signal a new note is starting. */
    private static final String BREAK_TAG = "<break time=\"500ms\"/>";
    /** Pause between sections within a note (title / properties / body). */
    private static final String SECTION_BREAK = "<break time=\"300ms\"/>";

    /**
     * Sentence boundary pattern: split after '. ', '! ', '? ', newlines, or
     * pipe-delimited property lists.  The delimiter is kept as part of the
     * preceding segment by using a look-ahead so no text is lost.
     */
    private static final Pattern SENTENCE_BOUNDARY =
            Pattern.compile("(?<=[.!?])\\s+|\\n+|(?<=\\|)\\s*");

    /** Associates a detected language code with the accumulated text for that language run. */
    private record LangSegment(String lang, StringBuilder text) {}

    private final LanguageDetectionService languageDetectionService;

    public SpeechService(LanguageDetectionService languageDetectionService) {
        this.languageDetectionService = languageDetectionService;
    }

    public String buildNotesSpeech(List<NotionNote> notes) {
        if (notes == null || notes.isEmpty()) {
            return "<speak>I could not find any notes in your Notion database.</speak>";
        }

        StringBuilder ssml = new StringBuilder("<speak>");

        for (int i = 0; i < notes.size(); i++) {
            NotionNote note = notes.get(i);
            String noteText = buildNoteSpeech(note);

            if (ssml.length() + noteText.length() + "</speak>".length() > MAX_SSML_LENGTH) {
                ssml.append("There are more notes, but I've reached the limit. ");
                break;
            }

            ssml.append(noteText);
        }

        ssml.append("</speak>");
        return ssml.toString();
    }

    private String buildNoteSpeech(NotionNote note) {
        StringBuilder sb = new StringBuilder();

        // Title — per-sentence language tagging
        sb.append(toSentenceTaggedSsml(note.getTitle())).append(SECTION_BREAK);

        // Properties (status, tags, dates, etc.) — per-sentence language tagging
        String content = note.getContent();
        if (content != null && !content.isBlank()) {
            sb.append(toSentenceTaggedSsml(content)).append(SECTION_BREAK);
        }

        // Full body text — per-sentence language tagging
        String body = note.getBody();
        if (body != null && !body.isBlank()) {
            sb.append(toSentenceTaggedSsml(body)).append(SECTION_BREAK);
        }

        sb.append(BREAK_TAG);
        return sb.toString();
    }

    /**
     * Splits {@code text} into sentences, detects the language of each sentence
     * individually, and wraps consecutive same-language (non-English) runs in a
     * single {@code <lang xml:lang="...">} tag.
     *
     * <p>This ensures that every sentence is pronounced with the correct Alexa
     * TTS voice, even when a note mixes several languages.</p>
     */
    public String toSentenceTaggedSsml(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        // Split into sentences while keeping whitespace context minimal
        String[] rawSegments = SENTENCE_BOUNDARY.split(text.trim());

        // Build (lang, text) pairs, merging adjacent segments with the same language
        List<LangSegment> segments = new ArrayList<>();

        for (String raw : rawSegments) {
            String segment = raw.strip();
            if (segment.isEmpty()) {
                continue;
            }
            String lang = languageDetectionService.detectLanguage(segment);
            if (!segments.isEmpty()) {
                LangSegment last = segments.get(segments.size() - 1);
                if (last.lang().equals(lang)) {
                    last.text().append(' ').append(segment);
                    continue;
                }
            }
            segments.add(new LangSegment(lang, new StringBuilder(segment)));
        }

        // Build SSML: wrap non-English runs in <lang> tags, joined by single spaces
        StringBuilder ssml = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            if (i > 0) {
                ssml.append(' ');
            }
            LangSegment seg = segments.get(i);
            String escaped = escapeXml(seg.text().toString());
            if (!"en".equalsIgnoreCase(seg.lang())) {
                String ssmlLang = languageDetectionService.getSsmlLanguageCode(seg.lang());
                ssml.append(String.format("<lang xml:lang=\"%s\">%s</lang>", ssmlLang, escaped));
            } else {
                ssml.append(escaped);
            }
        }
        return ssml.toString();
    }

    public String buildWelcomeSpeech() {
        return "<speak>Welcome to Notion Notes! You can say, read my notes, to hear your recent Notion notes. "
                + "How can I help you?</speak>";
    }

    public String buildHelpSpeech() {
        return "<speak>You can ask me to read your notes by saying, read my notes. "
                + "This will fetch your most recent notes from Notion and read them aloud. "
                + "What would you like to do?</speak>";
    }

    public String buildGoodbyeSpeech() {
        return "<speak>Goodbye! Your notes are always just a question away.</speak>";
    }

    public String buildFallbackSpeech() {
        return "<speak>I'm sorry, I didn't understand that. "
                + "You can say, read my notes, to hear your Notion notes. "
                + "How can I help you?</speak>";
    }

    public String buildErrorSpeech() {
        return "<speak>I'm sorry, I had trouble fetching your notes from Notion. "
                + "Please check your Notion configuration and try again.</speak>";
    }

    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
