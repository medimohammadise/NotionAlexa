# 🎯 Complete HTTPS Setup - At a Glance

## ✅ What's Been Done

| Component | Status | Location |
|-----------|--------|----------|
| Self-Signed Certificate | ✅ Generated | `src/main/resources/keystore.p12` |
| Spring Boot HTTPS Config | ✅ Created | `src/main/resources/application-local.yml` |
| REST Endpoint | ✅ Created | `src/main/java/com/notionalexa/alexa/AlexaSkillController.java` |
| Run Script | ✅ Created | `run-local.sh` |
| Documentation | ✅ Complete | `LOCAL_TESTING.md`, `QUICK_START.md` |
| Verification Script | ✅ Created | `verify-setup.sh` |
| Build Status | ✅ Success | `build/libs/notion-alexa-1.0.0-SNAPSHOT.jar` |

## 🚀 Quick Setup (5 Minutes)

### Terminal 1 - Start Application
```bash
# Set your Notion credentials
export NOTION_API_KEY='secret_xxxxx'
export NOTION_DATABASE_ID='xxxxx'

# Run with HTTPS
./run-local.sh
```

**Expected Output:**
```
🚀 Starting NotionAlexa with HTTPS enabled...
📦 Building application...
✅ Build successful!
🔐 Starting application with HTTPS on port 8080...
   Endpoint: https://localhost:8080/alexa
```

### Terminal 2 - Start ngrok Tunnel
```bash
ngrok http https://localhost:8080
```

**Expected Output:**
```
Forwarding  https://5e16-2003-c1-739-a5df-51bc-5cdd-7190-f4cd.ngrok-free.app -> https://localhost:8080
```

### Configure Alexa Endpoint

1. Open [Alexa Developer Console](https://developer.amazon.com/alexa/console/ask)
2. Select your skill
3. Go to **Build** → **Endpoint**
4. Select **HTTPS** endpoint type
5. Set Default Region endpoint:
   ```
   https://5e16-2003-c1-739-a5df-51bc-5cdd-7190-f4cd.ngrok-free.app/alexa
   ```
6. Choose SSL certificate: **"My development endpoint is a sub-domain of a domain that has a wildcard certificate from a certificate authority"**
7. Click **Save Endpoints**

### Test in Alexa Simulator

1. Click **Test** tab
2. Enable **Development** mode
3. Say/type: **"open notion notes"**
4. 🎉 Listen to your Notion notes!

## 📊 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     Your Computer                           │
│                                                             │
│  ┌────────────────────────────────────────────────────┐   │
│  │ Terminal 1: Spring Boot Application                │   │
│  │ https://localhost:8080/alexa                       │   │
│  │                                                    │   │
│  │ ┌──────────────────────────────────────────────┐  │   │
│  │ │ AlexaSkillController.java (POST /alexa)      │  │   │
│  │ │ ↓                                            │  │   │
│  │ │ Notion Database → Your Notes → Alexa Skill  │  │   │
│  │ └──────────────────────────────────────────────┘  │   │
│  └────────────────────────────────────────────────────┘   │
│                           ▲                                 │
│                           │ HTTPS                           │
│                           │ (self-signed cert)              │
│                           │                                 │
│  ┌────────────────────────────────────────────────────┐   │
│  │ Terminal 2: ngrok Tunnel                           │   │
│  │ Forwarding: https://xxxx.ngrok-free.app -> Local  │   │
│  └────────────────────────────────────────────────────┘   │
│                           ▲                                 │
│                           │ HTTPS                           │
│                           │ (ngrok certificate)             │
│                           │                                 │
└───────────────────────────┼─────────────────────────────────┘
                            │
                            │
                    ┌───────▼────────┐
                    │  Alexa Device  │
                    │  & Simulator   │
                    └────────────────┘
```

## 🔐 Certificate Details

```bash
# View certificate details
keytool -list -v -keystore src/main/resources/keystore.p12 -storepass changeit
```

**Certificate Info:**
```
Alias: notionalexa
Type: PKCS12
Size: 2.7 KB
Algorithm: 2048-bit RSA with SHA384
Validity: Feb 23, 2026 → Feb 23, 2027
Subject: CN=localhost, OU=NotionAlexa
```

## 🧪 Testing Commands

### 1. Health Check
```bash
# Local health check (with cert warning - that's OK)
curl -k https://localhost:8080/alexa/health
# Response: OK

# Via ngrok (no warnings)
curl https://YOUR-NGROK-URL/alexa/health
# Response: OK
```

### 2. Monitor Requests
```bash
# ngrok Web Dashboard - See all traffic in real-time
open http://127.0.0.1:4040
```

### 3. Check Running Processes
```bash
# Verify Spring Boot is running
ps aux | grep "notion-alexa"

# Verify ngrok is running
ps aux | grep ngrok
```

## 🚨 Troubleshooting Checklist

| Problem | Check | Fix |
|---------|-------|-----|
| "There was a problem with the requested skill's response" | URL includes `/alexa` | Add `/alexa` to end of URL |
| Connection refused | App running | `ps aux \| grep java` → `./run-local.sh` |
| SSL certificate error | Cert type selected | Select "wildcard certificate" option |
| ngrok not tunneling | ngrok running | `ps aux \| grep ngrok` → `ngrok http https://localhost:8080` |
| Port 8080 in use | Find process | `lsof -ti:8080 \| xargs kill -9` |
| Endpoint not responding | Health check | `curl -k https://localhost:8080/alexa/health` |

## 📁 New Files Created

```
NotionAlexa/
├── 🔐 src/main/resources/
│   ├── keystore.p12                    # Self-signed certificate
│   ├── application-local.yml           # Local HTTPS config
│   └── application.yml                 # (updated)
│
├── 🌐 src/main/java/com/notionalexa/alexa/
│   └── AlexaSkillController.java       # REST endpoint
│
├── 📚 Documentation
│   ├── QUICK_START.md                  # This guide
│   ├── LOCAL_TESTING.md                # Detailed guide
│   ├── COMPLETE_SUMMARY.md             # Full summary
│   └── README.md                       # (updated)
│
├── 🚀 Scripts
│   ├── run-local.sh                    # Run with HTTPS
│   └── verify-setup.sh                 # Verify setup
│
└── 🔒 .gitignore                       # (updated - ignores *.p12)
```

## 🎯 Success Checklist

- [ ] Set `NOTION_API_KEY` environment variable
- [ ] Set `NOTION_DATABASE_ID` environment variable
- [ ] Run `./run-local.sh` (application starts on port 8080)
- [ ] Run `ngrok http https://localhost:8080` (in another terminal)
- [ ] Copy ngrok URL from ngrok terminal
- [ ] Open Alexa Developer Console
- [ ] Select your skill → Build → Endpoint
- [ ] Configure endpoint: `https://YOUR-NGROK-URL/alexa`
- [ ] Select correct SSL certificate type
- [ ] Click Save Endpoints
- [ ] Go to Test tab → Enable Development
- [ ] Test with "open notion notes"
- [ ] 🎉 Hear your Notion notes!

## 📝 Common Commands Reference

```bash
# Verify setup is correct
./verify-setup.sh

# Build application
./gradlew clean build

# Run locally with HTTPS
./run-local.sh

# Test health check
curl -k https://localhost:8080/alexa/health

# Kill process on port 8080
lsof -ti:8080 | xargs kill -9

# View certificate details
keytool -list -v -keystore src/main/resources/keystore.p12 -storepass changeit

# Check running processes
ps aux | grep java
ps aux | grep ngrok

# Deploy to AWS Lambda (when ready)
./gradlew clean bootJar
sam deploy --guided
```

## ⚠️ Important Notes

### ngrok Free Tier
- **Free URLs expire** - Change every time you restart ngrok
- Update Alexa endpoint after each restart
- Consider upgrading to ngrok Pro for static URLs

### Security
- Certificate is **git-ignored** - Never committed to version control
- Self-signed cert is **development only**
- Production uses **AWS Lambda** - No certificate needed

### Production Switch
When ready to deploy to production:
```bash
./gradlew clean bootJar
sam deploy --guided
```
Then switch Alexa endpoint from HTTPS to **AWS Lambda ARN**.

## 📞 Need Help?

1. **Run verification script:**
   ```bash
   ./verify-setup.sh
   ```

2. **Check application logs** - Terminal 1 shows DEBUG logs

3. **Check ngrok traffic** - Open http://127.0.0.1:4040

4. **Read full guide:**
   - `QUICK_START.md` - Step-by-step instructions
   - `LOCAL_TESTING.md` - Detailed troubleshooting

5. **Common Issues:**
   - Missing `/alexa` in endpoint URL
   - Wrong SSL certificate type selected
   - Environment variables not set
   - Port 8080 already in use
   - ngrok tunnel not forwarding

---

**You're all set! 🚀 Just set your Notion credentials and run `./run-local.sh`**

