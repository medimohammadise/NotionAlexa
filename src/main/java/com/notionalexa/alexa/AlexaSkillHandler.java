package com.notionalexa.alexa;

import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;
import com.notionalexa.NotionAlexaApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.SpringApplication;

public class AlexaSkillHandler extends SkillStreamHandler {

    public AlexaSkillHandler() {
        super(buildSkill());
    }

    private static com.amazon.ask.Skill buildSkill() {
        ConfigurableApplicationContext ctx =
                SpringApplication.run(NotionAlexaApplication.class);
        return Skills.standard()
                .addRequestHandlers(
                        ctx.getBean(LaunchRequestHandler.class),
                        ctx.getBean(ReadNotesIntentHandler.class),
                        ctx.getBean(HelpIntentHandler.class),
                        ctx.getBean(CancelAndStopIntentHandler.class),
                        ctx.getBean(SessionEndedRequestHandler.class),
                        ctx.getBean(FallbackIntentHandler.class))
                .build();
    }
}
