package com.notionalexa.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.notionalexa.speech.SpeechService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class HelpIntentHandler implements RequestHandler {

    private final SpeechService speechService;

    public HelpIntentHandler(SpeechService speechService) {
        this.speechService = speechService;
    }

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(Predicates.intentName("AMAZON.HelpIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        String speechText = speechService.buildHelpSpeech();
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .withShouldEndSession(false)
                .build();
    }
}
