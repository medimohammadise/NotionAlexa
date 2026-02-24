package com.notionalexa.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.ui.SsmlOutputSpeech;
import com.amazon.ask.model.Response;
import com.amazon.ask.response.ResponseBuilder;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.LaunchRequest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that ResponseBuilder.withSpeech() correctly produces non-empty SSML
 * when passed a full speak document containing nested lang and break tags.
 */
class ResponseBuilderSsmlTest {

    private HandlerInput buildInput() {
        RequestEnvelope envelope = RequestEnvelope.builder()
                .withRequest(LaunchRequest.builder().build())
                .build();
        return HandlerInput.builder().withRequestEnvelope(envelope).build();
    }

    @Test
    void withSpeech_plainSpeak_producesNonEmptySsml() {
        Optional<Response> response = buildInput().getResponseBuilder()
                .withSpeech("<speak>Hello world.</speak>")
                .build();
        assertThat(response).isPresent();
        SsmlOutputSpeech speech = (SsmlOutputSpeech) response.get().getOutputSpeech();
        assertThat(speech.getSsml()).isNotEmpty();
        assertThat(speech.getSsml()).contains("Hello world");
    }

    @Test
    void withSpeech_speakWithLangTag_producesNonEmptySsmlWithLangTag() {
        String ssmlIn = "<speak>This is English. <lang xml:lang=\"de-DE\">Guten Morgen!</lang></speak>";
        Optional<Response> response = buildInput().getResponseBuilder()
                .withSpeech(ssmlIn)
                .build();
        assertThat(response).isPresent();
        SsmlOutputSpeech speech = (SsmlOutputSpeech) response.get().getOutputSpeech();
        assertThat(speech.getSsml()).isNotEmpty();
        assertThat(speech.getSsml()).contains("Guten Morgen");
        assertThat(speech.getSsml()).contains("lang xml:lang");
    }

    @Test
    void withSpeech_speakWithBreakTag_producesNonEmptySsmlWithBreak() {
        String ssmlIn = "<speak>Note 1: Guten Morgen.<break time=\"500ms\"/></speak>";
        Optional<Response> response = buildInput().getResponseBuilder()
                .withSpeech(ssmlIn)
                .build();
        assertThat(response).isPresent();
        SsmlOutputSpeech speech = (SsmlOutputSpeech) response.get().getOutputSpeech();
        assertThat(speech.getSsml()).isNotEmpty();
        assertThat(speech.getSsml()).contains("Guten Morgen");
    }
}
