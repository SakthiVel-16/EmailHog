# 🚀 Setup Instructions for Mail Clone

## Quick Start - 3 Simple Steps

### Step 1: Clone Repository

```bash
git clone https://github.com/yourusername/mail-clone.git
cd mail-clone/mail-clone/mail-clone
```

### Step 2: Setup Environment

```bash
# Copy the template
cp .env.example .env

# Edit .env and add your credentials
notepad .env
```

Add your actual credentials to `.env`:

```properties
# MongoDB Atlas URI (get from https://www.mongodb.com/cloud/atlas)
# Replace YOUR_MONGODB_URI_HERE with your actual connection string
MONGODB_URI=YOUR_MONGODB_URI_HERE
MONGODB_DATABASE=emailhog

# Gemini API Key (get from https://aistudio.google.com)
# Replace YOUR_GEMINI_API_KEY with your actual API key
GEMINI_API_KEY=YOUR_GEMINI_API_KEY_HERE
```

### Step 3: Run the Application

**Windows:**

```bash
mvnw.cmd spring-boot:run
```

**Mac/Linux:**

```bash
./mvnw spring-boot:run
```

That's it! 🎉 Your app will automatically read credentials from `.env` file.

---

## 🔐 Security - How It Works

### What's Protected? 🔒

- ✅ `.env` - Your actual credentials (blocked by `.gitignore`)
- ✅ `.env.local` - Local overrides (blocked by `.gitignore`)

### What's Safe to Commit? ✅

- ✅ `.env.example` - Template with placeholders ONLY
- ✅ `application.properties` - Has `${PLACEHOLDER:}` - no real secrets
- ✅ All Java source code, documentation

### Never Committed - Protected by .gitignore

```
.env                         ← Your local secrets (PRIVATE)
.env.local                   ← Local overrides (PRIVATE)
```

---

## 📁 File Structure

```
mail-clone/                          (root folder - from git clone)
├── mail-clone/                      (first level)
│   ├── mail-clone/                  ← ⭐ You are here after: cd mail-clone/mail-clone/mail-clone
│   │   ├── .env                     ← Your credentials (private, not committed)
│   │   ├── .env.example             ← Template (public, safe)
│   │   ├── application.properties   ← Placeholders ${VAR:} (public, safe)
│   │   ├── mvnw.cmd                 ← Windows: Use this to run
│   │   ├── mvnw                     ← Mac/Linux: Use this to run
│   │   ├── pom.xml                  ← Maven config with dotenv dependency
│   │   ├── SETUP_INSTRUCTIONS.md    ← This file
│   │   ├── src/
│   │   │   ├── main/java/...        ← Your Java code
│   │   │   └── resources/
│   │   │       └── static/index.html  ← Web UI
│   │   └── README.md                ← Project documentation
```

**Important:** Navigate to the **deepest `mail-clone/`** folder before running commands!

---

## 🆘 Troubleshooting

### Problem: "mvnw.cmd" or "mvn" command not found

**Solution:** Make sure you're in the correct directory:

```bash
# Verify you're in the deepest mail-clone folder
cd mail-clone/mail-clone/mail-clone

# Check that mvnw.cmd exists
dir mvnw.cmd

# Then run
mvnw.cmd spring-boot:run
```

### Problem: "API key not found" or MongoDB connection fails

**Solution:** Make sure you:

1. Copied `.env.example` to `.env`
2. Edited `.env` with YOUR actual credentials
3. Verified `.env` is NOT in `.gitignore` rules (it should be blocked)

### Problem: Changes to `.env` not taking effect

**Solution:** Restart the application after editing `.env`

### Problem: Spring Boot won't read `.env`

**Verify:** Make sure `pom.xml` has the dotenv dependency:

```xml
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>3.0.0</version>
</dependency>
```

---

## 👥 For Team Members

When team members clone your repo, they follow the same steps:

```bash
# 1. Clone
git clone <repo-url>
cd mail-clone/mail-clone/mail-clone

# 2. Setup (with THEIR credentials)
cp .env.example .env
# Edit .env with their MongoDB & API keys

# 3. Run (Windows)
mvnw.cmd spring-boot:run

# 3. Run (Mac/Linux)
./mvnw spring-boot:run
```

No manual configuration needed! Each developer has their own `.env` file locally.

---

## ✅ Prerequisites

Before getting started, you need:

- **Java 17** or higher
- **Maven 3.6+**
- **MongoDB Atlas Account** - [Create Free Tier](https://www.mongodb.com/cloud/atlas/register)
- **Google Gemini API Key** - [Get Free Access](https://aistudio.google.com)
- **Git**

---

## 🔑 Getting Your Credentials

### MongoDB Connection String

1. Go to [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
2. Create/select your cluster
3. Click "Connect"
4. Choose "Drivers"
5. Copy the connection string
6. Format: `mongodb+srv://username:password@cluster.mongodb.net/?appName=mail-clone`

### Gemini API Key

1. Go to [Google AI Studio](https://aistudio.google.com)
2. Click "Get API Key"
3. Create new API key
4. Copy and paste into `.env`

---

## 📝 Environment Variables Reference

All variables read from `.env` file:

```properties
# MongoDB Connection (required)
MONGODB_URI=mongodb+srv://USERNAME:PASSWORD@CLUSTER.mongodb.net/?appName=mail-clone
MONGODB_DATABASE=emailhog

# Google Gemini API (required)
GEMINI_API_KEY=YOUR_API_KEY
```

Spring Boot automatically injects these into:

- `spring.data.mongodb.uri`
- `spring.data.mongodb.database`
- `gemini.api.key`
  ↓

8. Spring loads: application-local.properties locally
   ↓
9. ✅ App runs with THEIR credentials (not pushed to GitHub)

````

---

## ⚠️ Critical Security Rules

1. **NEVER** hardcode credentials in `application.properties`
2. **NEVER** add actual API keys to `.env.example`
3. **ALWAYS** use `.gitignore` to block `**/application-*.properties` and `.env`
4. **ALWAYS** check git status before committing: `git status`

```bash
# Verify nothing sensitive is staged
git status

# Should show application-local.properties and .env as "untracked" (not staged)
# If they show as "modified" or "new file" - DO NOT COMMIT!

# Before committing, verify
git diff --cached | grep -i "password\|key\|token\|secret"
# Should return nothing
````

---

## 🆘 If You Accidentally Commit Credentials

```bash
# Remove from git history (IMPORTANT!)
git rm --cached src/main/resources/application-local.properties
git rm --cached .env

# Update .gitignore (if not already there)
# Then commit
git add .gitignore
git commit -m "Remove accidentally committed secrets"

# Force push (if already pushed)
git push --force-with-lease
```

---

## 📚 Reference

- **Spring Documentation:** https://spring.io/blog/2015/06/08/notes-on-spring-boot-property-resolution
- **Environment Variables Resolution Order:** Environment → System Properties → Properties Files → Defaults
- **GitHub Secret Management:** https://docs.github.com/en/actions/security-guides/encrypted-secrets
