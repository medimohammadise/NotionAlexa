#!/bin/bash

# Run NotionAlexa locally with HTTPS for testing with ngrok
# Usage: ./run-local.sh

echo "🚀 Starting NotionAlexa with HTTPS enabled..."
echo ""
echo "Make sure you have set the environment variables:"
echo "  export NOTION_API_KEY='your-api-key'"
echo "  export NOTION_DATABASE_ID='your-database-id'"
echo ""

# Build the application
echo "📦 Building application..."
./gradlew clean bootJar

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo ""
echo "✅ Build successful!"
echo ""
echo "🔐 Starting application with HTTPS on port 8080..."
echo "   Endpoint: https://localhost:8080/alexa"
echo ""
echo "📱 In another terminal, run ngrok:"
echo "   ngrok http https://localhost:8080"
echo ""
echo "Then configure your Alexa skill endpoint with the ngrok URL:"
echo "   https://xxxx-xxxx.ngrok-free.app/alexa"
echo ""

# Run with local profile
java -jar build/libs/notion-alexa-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

