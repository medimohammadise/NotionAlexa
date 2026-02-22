# 🚀 Quick Start Guide - NotionAlexa with HTTPS

## ✅ Setup Complete!

Everything is configured and ready to test locally with ngrok.

## 1️⃣ Prepare Environment

```bash
# Set your Notion credentials
export NOTION_API_KEY='secret_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
export NOTION_DATABASE_ID='xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'

# Verify they're set
echo $NOTION_API_KEY
echo $NOTION_DATABASE_ID
```

## 2️⃣ Start the Application (Terminal 1)

```bash
./run-local.sh
```

Wait for the output:
```
Starting application with HTTPS on port 8080...
Endpoint: https://localhost:8080/alexa
```

## 3️⃣ Start ngrok (Terminal 2)

```bash
ngrok http https://localhost:8080
```

You'll see:
```
Forwarding  https://5e16-2003-c1-739-a5df-51bc-5cdd-7190-f4cd.ngrok-free.app -> https://localhost:8080
```

**Copy the ngrok URL** (the `https://...ngrok-free.app` part)

## 4️⃣ Configure Alexa Endpoint

1. Go to [Alexa Developer Console](https://developer.amazon.com/alexa/console/ask)
2. Select your skill → **Build** → **Endpoint**
3. Choose **HTTPS**
4. Paste your ngrok URL with `/alexa` at the end:
   ```
   https://5e16-2003-c1-739-a5df-51bc-5cdd-7190-f4cd.ngrok-free.app/alexa
   ```
5. **SSL Certificate Type:** Select the option for "wildcard certificate from certificate authority"
6. Click **Save Endpoints**

## 5️⃣ Test Your Skill

1. Go to **Test** tab in Alexa Console
2. Enable **Development** mode
3. Click the microphone and say:
   ```
   "open notion notes"
   ```
   OR type it in the text field

4. Listen for your notes to be read back!

## 📊 Monitor What's Happening

### Health Check
```bash
# Terminal 3
curl -k https://localhost:8080/alexa/health
# Should return: OK
```

### View ngrok Traffic
Open in browser:
```
http://127.0.0.1:4040
```
See all incoming requests and responses in real-time.

### View Application Logs
Check Terminal 1 where you ran `./run-local.sh` for DEBUG logs showing each request.

## 🔍 Troubleshooting

| Issue | Solution |
|-------|----------|
| "Problem with skill" | Make sure endpoint URL ends with `/alexa` |
| Can't connect to endpoint | Check application is running: `ps aux \| grep java` |
| Port 8080 in use | Kill it: `lsof -ti:8080 \| xargs kill -9` |
| SSL certificate error | Select correct certificate type in Alexa Console |
| ngrok not working | Restart ngrok and update Alexa endpoint URL |

## 📝 Certificate Details

```
File: src/main/resources/keystore.p12
Password: changeit
Alias: notionalexa
Valid Until: February 23, 2027
```

## 🌐 What Each URL Does

| URL | Purpose |
|-----|---------|
| `https://localhost:8080/alexa` | Local HTTPS endpoint |
| `https://localhost:8080/alexa/health` | Health check |
| `https://YOUR-NGROK-URL/alexa` | Public endpoint (via ngrok tunnel) |
| `http://127.0.0.1:4040` | ngrok web dashboard |

## 🎯 Success Indicators

- ✅ Application starts with `./run-local.sh` without errors
- ✅ ngrok shows "Forwarding" URL
- ✅ Health check returns "OK"
- ✅ ngrok web dashboard shows POST requests to `/alexa`
- ✅ Alexa reads your Notion notes

## 📚 More Details

For detailed information, see:
- **LOCAL_TESTING.md** - Complete guide with troubleshooting
- **README.md** - Architecture and deployment info

## 🚀 Next: Production Deployment

When ready to deploy to production:

```bash
./gradlew clean bootJar
sam deploy --guided
```

Then switch Alexa endpoint to **AWS Lambda ARN** (no HTTPS needed).

---

**Need help?** Check the Terminal 1 logs for detailed error messages!

