package com.notionalexa.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.notionalexa.speech.SpeechService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CancelAndStopIntentHandler implements RequestHandler {

    private final SpeechService speechService;

    public CancelAndStopIntentHandler(SpeechService speechService) {
        this.speechService = speechService;
    }

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(Predicates.intentName("AMAZON.CancelIntent")
                .or(Predicates.intentName("AMAZON.StopIntent")));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        return input.getResponseBuilder()
                .withSpeech(speechService.buildGoodbyeSpeech())
                .withShouldEndSession(true)
                .build();
    }
}
