package com.notionalexa.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.ui.SsmlOutputSpeech;
import com.notionalexa.notion.NotionService;
import com.notionalexa.notion.model.NotionNote;
import com.notionalexa.speech.SpeechService;
import com.notionalexa.speech.LanguageDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadNotesIntentHandlerTest {

    @Mock
    private NotionService notionService;

    private SpeechService speechService;
    private ReadNotesIntentHandler handler;

    @BeforeEach
    void setUp() {
        LanguageDetectionService languageDetectionService = new LanguageDetectionService();
        speechService = new SpeechService(languageDetectionService);
        handler = new ReadNotesIntentHandler(notionService, speechService);
    }

    @Test
    void canHandle_withReadNotesIntent_returnsTrue() {
        HandlerInput input = buildHandlerInput(ReadNotesIntentHandler.INTENT_NAME);
        assertThat(handler.canHandle(input)).isTrue();
    }

    @Test
    void canHandle_withOtherIntent_returnsFalse() {
        HandlerInput input = buildHandlerInput("AMAZON.HelpIntent");
        assertThat(handler.canHandle(input)).isFalse();
    }

    @Test
    void handle_withNotes_returnsSpeechWithNotes() {
        List<NotionNote> notes = List.of(
                new NotionNote("1", "My First Note", "Status: Active", "This is the full body of the first note.", null, null),
                new NotionNote("2", "My Second Note", "Status: Done", "Body of the second note.", null, null)
        );
        when(notionService.getRecentNotes()).thenReturn(notes);

        HandlerInput input = buildHandlerInput(ReadNotesIntentHandler.INTENT_NAME);
        Optional<Response> response = handler.handle(input);

        assertThat(response).isPresent();
        assertThat(response.get().getOutputSpeech()).isInstanceOf(SsmlOutputSpeech.class);
        SsmlOutputSpeech speech = (SsmlOutputSpeech) response.get().getOutputSpeech();
        assertThat(speech.getSsml()).contains("My First Note");
        assertThat(speech.getSsml()).contains("full body of the first note");
        assertThat(speech.getSsml()).contains("My Second Note");
        assertThat(speech.getSsml()).contains("Body of the second note");
    }

    @Test
    void toSentenceTaggedSsml_mixedLanguageBody_tagsEachLanguageCorrectly() {
        LanguageDetectionService lds = new LanguageDetectionService();
        SpeechService service = new SpeechService(lds);

        // English sentence followed by a clearly German sentence
        String mixed = "This is an English sentence. Das ist ein deutscher Satz. Back to English again.";
        String ssml = service.toSentenceTaggedSsml(mixed);

        // English sentences should NOT be wrapped
        assertThat(ssml).contains("This is an English sentence");
        assertThat(ssml).doesNotContain("<lang xml:lang=\"en");

        // German sentence MUST be wrapped in lang tag
        assertThat(ssml).contains("<lang xml:lang=\"de-DE\">");
        assertThat(ssml).contains("Das ist ein deutscher Satz");
    }

    @Test
    void handle_withNoNotes_returnsNotFoundMessage() {
        when(notionService.getRecentNotes()).thenReturn(List.of());

        HandlerInput input = buildHandlerInput(ReadNotesIntentHandler.INTENT_NAME);
        Optional<Response> response = handler.handle(input);

        assertThat(response).isPresent();
        SsmlOutputSpeech speech = (SsmlOutputSpeech) response.get().getOutputSpeech();
        assertThat(speech.getSsml()).contains("could not find any notes");
    }

    private HandlerInput buildHandlerInput(String intentName) {
        IntentRequest intentRequest = IntentRequest.builder()
                .withIntent(com.amazon.ask.model.Intent.builder()
                        .withName(intentName)
                        .build())
                .build();
        RequestEnvelope envelope = RequestEnvelope.builder()
                .withRequest(intentRequest)
                .build();
        return HandlerInput.builder()
                .withRequestEnvelope(envelope)
                .build();
    }
}
