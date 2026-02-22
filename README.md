# NotionAlexa

A serverless **Spring Modulith** application that powers an **Amazon Alexa skill** to read your Notion notes aloud. Built with Spring Boot 4.1.0-M2, deployed on AWS Lambda via Spring Cloud Function.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        AWS Lambda                           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Spring Boot Application                  │  │
│  │                                                      │  │
│  │  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  │  │
│  │  │    alexa/   │  │   notion/    │  │  speech/   │  │  │
│  │  │             │  │              │  │            │  │  │
│  │  │ Skill       │─▶│ NotionService│  │ Speech     │  │  │
│  │  │ Handlers    │  │ NotionConfig │  │ Service    │  │  │
│  │  │             │◀─│ NotionNote   │◀─│            │  │  │
│  │  └─────────────┘  └──────────────┘  └────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
         ▲                          │
         │ Alexa Requests           │ REST API Calls
         │                          ▼
    Alexa Device               Notion API v1
```

## Technology Stack

- **Java 17-23** (Java 24 may work, Java 25 is not yet supported by Gradle 8.14.1)
- **Spring Boot 4.1.0-M2** with **Spring Modulith 1.4.1**
- **Spring Cloud Function** + **AWS Lambda adapter**
- **Notion API v1** via Spring `RestClient`
- **Alexa Skills Kit SDK for Java** (`ask-sdk 2.86.0`)
- **Gradle 8.14.1**
- **AWS SAM** for deployment

## Prerequisites

- Java 17, 21, or 23 (recommended: Java 17 for AWS Lambda compatibility)
- Gradle 8.14+ (or use the included `./gradlew` wrapper)
- AWS CLI configured with appropriate permissions
- AWS SAM CLI installed
- A [Notion integration](https://developers.notion.com/docs/create-a-notion-integration) with an API key
- A Notion database shared with your integration
- An [Alexa Developer Console](https://developer.amazon.com/alexa/console/ask) account

## Project Structure

```
com.notionalexa
├── alexa/                         # Alexa skill request handling module
│   ├── AlexaSkillHandler.java     # Main Lambda handler (extends SkillStreamHandler)
│   ├── LaunchRequestHandler.java
│   ├── ReadNotesIntentHandler.java
│   ├── HelpIntentHandler.java
│   ├── CancelAndStopIntentHandler.java
│   ├── SessionEndedRequestHandler.java
│   └── FallbackIntentHandler.java
├── notion/                        # Notion API integration module
│   ├── NotionService.java
│   ├── NotionConfig.java
│   └── model/
│       └── NotionNote.java
├── speech/                        # Speech synthesis module
│   └── SpeechService.java
└── NotionAlexaApplication.java
```

## Configuration

### Environment Variables

| Variable             | Description                                      |
|----------------------|--------------------------------------------------|
| `NOTION_API_KEY`     | Your Notion integration token                    |
| `NOTION_DATABASE_ID` | The ID of the Notion database containing notes   |

### Notion Setup

1. Go to [https://www.notion.so/my-integrations](https://www.notion.so/my-integrations)
2. Create a new integration and copy the **Internal Integration Token** — this is your `NOTION_API_KEY`.
3. Open the Notion database you want to use and click **Share** → **Invite** your integration.
4. Copy the database ID from the URL:
   `https://www.notion.so/{workspace}/{DATABASE_ID}?v=...`

## Build

```bash
./gradlew clean bootJar
```

The fat JAR will be at `build/libs/notion-alexa-1.0.0-SNAPSHOT.jar`.

## Running Tests

```bash
./gradlew test
```

## Local Development & Testing

For local testing with ngrok and HTTPS, see the detailed guide: **[LOCAL_TESTING.md](LOCAL_TESTING.md)**

Quick start:
```bash
# Set environment variables
export NOTION_API_KEY='your-api-key'
export NOTION_DATABASE_ID='your-database-id'

# Run with HTTPS
./run-local.sh

# In another terminal, start ngrok
ngrok http https://localhost:8080

# Configure Alexa endpoint with: https://YOUR-NGROK-URL.ngrok-free.app/alexa
```

## Deployment (AWS SAM)

1. **Store secrets in AWS SSM Parameter Store:**

   ```bash
   aws ssm put-parameter --name /notionalexa/notion-api-key \
       --value "secret_xxxx" --type SecureString
   aws ssm put-parameter --name /notionalexa/notion-database-id \
       --value "your-database-id" --type SecureString
   aws ssm put-parameter --name /notionalexa/alexa-skill-id \
       --value "amzn1.ask.skill.xxxx" --type String
   ```

2. **Build and deploy:**

   ```bash
   ./gradlew clean bootJar
   sam deploy --guided
   ```

3. Note the **Lambda Function ARN** from the output.

## Alexa Skill Setup

1. Go to the [Alexa Developer Console](https://developer.amazon.com/alexa/console/ask).
2. Create a new custom skill with invocation name **"notion notes"**.
3. Upload `interaction-model/en-US.json` as the interaction model.
4. Under **Endpoint**, set the Lambda ARN from the deployment step.
5. Enable the skill and test it with: *"Alexa, open notion notes"*.

## Sample Utterances

| User says                               | Handler               |
|-----------------------------------------|-----------------------|
| "Alexa, open notion notes"              | LaunchRequestHandler  |
| "read my notes"                         | ReadNotesIntentHandler|
| "tell me my notes"                      | ReadNotesIntentHandler|
| "help"                                  | HelpIntentHandler     |
| "stop" / "cancel"                       | CancelAndStopIntent   |

## License

MIT
