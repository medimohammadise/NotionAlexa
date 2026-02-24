package com.notionalexa.alexa;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.ResponseEnvelope;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for local Alexa skill testing via HTTPS endpoint.
 * This is only enabled when running locally (not in Lambda).
 */
@RestController
@ConditionalOnProperty(name = "alexa.local.enabled", havingValue = "true")
public class AlexaSkillController {

    private final Skill skill;

    public AlexaSkillController(
            LaunchRequestHandler launchRequestHandler,
            ReadNotesIntentHandler readNotesIntentHandler,
            HelpIntentHandler helpIntentHandler,
            CancelAndStopIntentHandler cancelAndStopIntentHandler,
            SessionEndedRequestHandler sessionEndedRequestHandler,
            FallbackIntentHandler fallbackIntentHandler) {
        this.skill = Skills.standard()
                .addRequestHandlers(
                        launchRequestHandler,
                        readNotesIntentHandler,
                        helpIntentHandler,
                        cancelAndStopIntentHandler,
                        sessionEndedRequestHandler,
                        fallbackIntentHandler)
                .build();
    }

    // Handle Alexa requests at root path (/)
    // Alexa posts to https://YOUR-URL/ or https://YOUR-URL/alexa
    @PostMapping(
            value = "/",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEnvelope handleSkillRequest(@RequestBody RequestEnvelope requestEnvelope) {
        return skill.invoke(requestEnvelope);
    }

    // Health check endpoint
    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    // Root health check
    @GetMapping("/")
    public String healthRoot() {
        return "OK";
    }
}

