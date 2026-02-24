package com.notionalexa.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.notionalexa.notion.NotionService;
import com.notionalexa.notion.model.NotionNote;
import com.notionalexa.speech.SpeechService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ReadNotesIntentHandler implements RequestHandler {

    private static final Logger log = LoggerFactory.getLogger(ReadNotesIntentHandler.class);
    static final String INTENT_NAME = "ReadNotesIntent";

    private final NotionService notionService;
    private final SpeechService speechService;

    public ReadNotesIntentHandler(NotionService notionService, SpeechService speechService) {
        this.notionService = notionService;
        this.speechService = speechService;
    }

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(Predicates.intentName(INTENT_NAME));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        log.info("Handling ReadNotesIntent");
        try {
            List<NotionNote> notes = notionService.getRecentNotes();
            String speechText = speechService.buildNotesSpeech(notes);
            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withShouldEndSession(true)
                    .build();
        } catch (Exception e) {
            log.error("Error handling ReadNotesIntent", e);
            return input.getResponseBuilder()
                    .withSpeech(speechService.buildErrorSpeech())
                    .withShouldEndSession(true)
                    .build();
        }
    }
}
