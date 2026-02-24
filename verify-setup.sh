#!/bin/bash

# NotionAlexa - HTTPS Setup Verification Script
# This script verifies all components are correctly configured

echo "🔍 NotionAlexa HTTPS Configuration Verification"
echo "================================================"
echo ""

# Check keystore
echo "1️⃣  Checking SSL Certificate..."
if [ -f "src/main/resources/keystore.p12" ]; then
    echo "   ✅ keystore.p12 exists"
    size=$(ls -lh src/main/resources/keystore.p12 | awk '{print $5}')
    echo "   📦 Size: $size"
else
    echo "   ❌ keystore.p12 NOT FOUND"
    exit 1
fi

echo ""

# Check configuration files
echo "2️⃣  Checking Configuration Files..."
if [ -f "src/main/resources/application.yml" ]; then
    echo "   ✅ application.yml exists (Lambda mode)"
else
    echo "   ❌ application.yml NOT FOUND"
    exit 1
fi

if [ -f "src/main/resources/application-local.yml" ]; then
    echo "   ✅ application-local.yml exists (HTTPS mode)"
else
    echo "   ❌ application-local.yml NOT FOUND"
    exit 1
fi

echo ""

# Check REST controller
echo "3️⃣  Checking REST Controller..."
if [ -f "src/main/java/com/notionalexa/alexa/AlexaSkillController.java" ]; then
    echo "   ✅ AlexaSkillController.java exists"
else
    echo "   ❌ AlexaSkillController.java NOT FOUND"
    exit 1
fi

echo ""

# Check run script
echo "4️⃣  Checking Run Script..."
if [ -f "run-local.sh" ]; then
    echo "   ✅ run-local.sh exists"
    if [ -x "run-local.sh" ]; then
        echo "   ✅ run-local.sh is executable"
    else
        echo "   ⚠️  run-local.sh is not executable"
        chmod +x run-local.sh
        echo "   ✅ Made executable"
    fi
else
    echo "   ❌ run-local.sh NOT FOUND"
    exit 1
fi

echo ""

# Check documentation
echo "5️⃣  Checking Documentation..."
docs=("LOCAL_TESTING.md" "QUICK_START.md" "COMPLETE_SUMMARY.md")
for doc in "${docs[@]}"; do
    if [ -f "$doc" ]; then
        echo "   ✅ $doc exists"
    else
        echo "   ⚠️  $doc not found (recommended)"
    fi
done

echo ""

# Check .gitignore
echo "6️⃣  Checking .gitignore..."
if grep -q "*.p12" ".gitignore"; then
    echo "   ✅ SSL certificates are git-ignored"
else
    echo "   ⚠️  SSL certificates may not be git-ignored"
fi

echo ""

# Build check
echo "7️⃣  Checking Build Status..."
if [ -d "build/libs" ]; then
    jar_count=$(ls build/libs/*.jar 2>/dev/null | wc -l)
    if [ "$jar_count" -gt 0 ]; then
        echo "   ✅ Application JAR exists"
        ls -lh build/libs/*SNAPSHOT.jar | awk '{print "   📦 " $9 " (" $5 ")"}'
    else
        echo "   ⚠️  No JAR file found. Run: ./gradlew clean build"
    fi
else
    echo "   ⚠️  No build directory. Run: ./gradlew clean build"
fi

echo ""
echo "================================================"
echo "✅ Configuration Verification Complete!"
echo ""
echo "Next Steps:"
echo "1. Set environment variables:"
echo "   export NOTION_API_KEY='your-api-key'"
echo "   export NOTION_DATABASE_ID='your-database-id'"
echo ""
echo "2. Start the application:"
echo "   ./run-local.sh"
echo ""
echo "3. In another terminal, start ngrok:"
echo "   ngrok http https://localhost:8080"
echo ""
echo "4. Configure Alexa endpoint with ngrok URL"
echo ""
echo "5. Test in Alexa Developer Console"
echo ""
echo "For detailed instructions, see: QUICK_START.md"
echo "For troubleshooting, see: LOCAL_TESTING.md"

