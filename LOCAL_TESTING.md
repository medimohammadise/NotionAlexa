# Local HTTPS Testing with ngrok

This guide explains how to test your NotionAlexa skill locally using HTTPS and ngrok.

## Prerequisites

- ngrok installed: `brew install ngrok` or download from [ngrok.com](https://ngrok.com)
- Environment variables set:
  ```bash
  export NOTION_API_KEY='secret_xxxxxxxxxx'
  export NOTION_DATABASE_ID='xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
  ```

## Setup

### 1. Self-Signed Certificate Generated ✅

A self-signed certificate has been generated at:
```
src/main/resources/keystore.p12
```

**Credentials:**
- Store Password: `changeit`
- Key Alias: `notionalexa`
- Validity: 365 days

### 2. Configuration Files

**application-local.yml** - Local development profile with HTTPS enabled
**application.yml** - Default profile (Lambda mode, no HTTPS)

## Running Locally

### Option 1: Using the Script

```bash
./run-local.sh
```

This script will:
1. Build the application
2. Start the server with HTTPS on port 8080
3. Display instructions for ngrok setup

### Option 2: Manual Steps

1. **Build the application:**
   ```bash
   ./gradlew clean bootJar
   ```

2. **Start the application with local profile:**
   ```bash
   java -jar build/libs/notion-alexa-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
   ```

3. **In another terminal, start ngrok:**
   ```bash
   ngrok http https://localhost:8080
   ```

   You'll see output like:
   ```
   Forwarding  https://xxxx-xxxx.ngrok-free.app -> https://localhost:8080
   ```

4. **Copy the ngrok HTTPS URL** (e.g., `https://5e16-2003-c1-739-a5df-51bc-5cdd-7190-f4cd.ngrok-free.app`)

## Configuring Alexa Skill Endpoint

1. Go to [Alexa Developer Console](https://developer.amazon.com/alexa/console/ask)
2. Select your skill → **Build** → **Endpoint**
3. Select **HTTPS**
4. Set the **Default Region** endpoint:
   ```
   https://YOUR-NGROK-URL.ngrok-free.app/alexa
   ```
   Example: `https://5e16-2003-c1-739-a5df-51bc-5cdd-7190-f4cd.ngrok-free.app/alexa`

5. Select SSL certificate type:
   - **"My development endpoint is a sub-domain of a domain that has a wildcard certificate from a certificate authority"**

6. Click **Save Endpoints**

## Testing

### 1. Health Check

Test the endpoint is working:
```bash
curl -k https://localhost:8080/alexa/health
```

Expected response: `OK`

Or via ngrok:
```bash
curl https://YOUR-NGROK-URL.ngrok-free.app/alexa/health
```

### 2. Test in Alexa Simulator

1. Go to **Test** tab in Alexa Developer Console
2. Enable testing: **Development**
3. Type or say: *"open notion notes"*
4. You should see the skill respond with your Notion notes

### 3. Monitor Logs

Watch the application logs for incoming requests:
```bash
tail -f logs/spring.log
```

Or check the console output for DEBUG logs.

### 4. Monitor ngrok Traffic

Visit the ngrok web interface at:
```
http://127.0.0.1:4040
```

This shows all HTTP/HTTPS requests passing through ngrok.

## Troubleshooting

### "There was a problem with the requested skill's response"

**Possible causes:**
1. ngrok URL is incorrect or expired (free ngrok URLs change on restart)
2. Application is not running
3. Endpoint path is wrong (must be `/alexa`, not just the root)
4. NOTION_API_KEY or NOTION_DATABASE_ID not set

**Solutions:**
- Verify the endpoint URL includes `/alexa`: `https://xxxx.ngrok-free.app/alexa`
- Check application logs for errors
- Test health endpoint: `curl https://YOUR-NGROK-URL.ngrok-free.app/alexa/health`
- Verify environment variables are set

### SSL Certificate Errors

If you see SSL errors:
- Make sure you selected the correct SSL certificate type in Alexa Console
- Ensure ngrok is forwarding to `https://localhost:8080` (not `http://`)

### ngrok Connection Failed

```bash
# Check if ngrok is running
ps aux | grep ngrok

# Restart ngrok with verbose output
ngrok http https://localhost:8080 --log=stdout
```

### Port 8080 Already in Use

```bash
# Find and kill the process
lsof -ti:8080 | xargs kill -9

# Or change the port in application-local.yml
server:
  port: 9090
```

Then update ngrok:
```bash
ngrok http https://localhost:9090
```

## Important Notes

### ⚠️ Limitations

1. **ngrok free URLs expire** - The URL changes every time you restart ngrok. You'll need to update the Alexa endpoint URL each time.
2. **Not for production** - This setup is for development/testing only. Production should use AWS Lambda.
3. **Certificate warnings** - Self-signed certificates are only for local testing.

### 🔒 Security

- The `keystore.p12` file is excluded from git (in `.gitignore`)
- Never commit SSL certificates to version control
- The self-signed certificate is only valid for local development

### 🚀 Deploying to Production

When ready for production, deploy to AWS Lambda:

```bash
./gradlew clean bootJar
sam deploy --guided
```

Then switch the Alexa endpoint back to **AWS Lambda ARN**.

## File Structure

```
NotionAlexa/
├── src/main/resources/
│   ├── keystore.p12              # Self-signed certificate (git-ignored)
│   ├── application.yml           # Default config (Lambda mode)
│   └── application-local.yml     # Local config (HTTPS enabled)
├── src/main/java/com/notionalexa/alexa/
│   └── AlexaSkillController.java # REST endpoint for local testing
└── run-local.sh                   # Convenience script
```

## Quick Reference

| Command | Description |
|---------|-------------|
| `./run-local.sh` | Build and run with HTTPS |
| `ngrok http https://localhost:8080` | Create tunnel |
| `curl -k https://localhost:8080/alexa/health` | Test health check |
| `http://127.0.0.1:4040` | ngrok web interface |
| `./gradlew bootRun --args='--spring.profiles.active=local'` | Run without building JAR |

## Next Steps

1. ✅ Certificate generated
2. ✅ HTTPS configured
3. ⏭️ Set environment variables
4. ⏭️ Run `./run-local.sh`
5. ⏭️ Start ngrok
6. ⏭️ Configure Alexa endpoint
7. ⏭️ Test in Alexa Simulator

