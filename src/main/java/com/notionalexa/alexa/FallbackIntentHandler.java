package com.notionalexa.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.notionalexa.speech.SpeechService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FallbackIntentHandler implements RequestHandler {

    private final SpeechService speechService;

    public FallbackIntentHandler(SpeechService speechService) {
        this.speechService = speechService;
    }

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(Predicates.intentName("AMAZON.FallbackIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        return input.getResponseBuilder()
                .withSpeech(speechService.buildFallbackSpeech())
                .withReprompt(speechService.buildFallbackSpeech())
                .withShouldEndSession(false)
                .build();
    }
}
