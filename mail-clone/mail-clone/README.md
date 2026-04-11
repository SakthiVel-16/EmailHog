# Mail Clone - Email Sandbox Application

A full-featured email sandbox application built with **Spring Boot 3.4.1** that simulates a Gmail-like inbox interface. It captures incoming emails, stores them in MongoDB Atlas, and provides AI-powered spam analysis using Google's Gemini API.

## 🎯 Features

- **Email Inbox Management** - View, compose, and manage emails like Gmail
- **Real-Time Updates** - WebSocket support for live inbox updates
- **Spam Detection** - AI-powered spam analysis using Gemini API
- **Email Attachments** - Support for file uploads with emails (up to 50MB)
- **SMTP Server** - Custom SMTP server to intercept and store emails
- **Link Checking** - Verify URLs and detect malicious links
- **MongoDB Storage** - Cloud-based storage with MongoDB Atlas
- **Responsive UI** - Modern web interface with real-time WebSocket communication

## 🛠️ Tech Stack

| Component           | Technology              |
| ------------------- | ----------------------- |
| **Framework**       | Spring Boot 3.4.1       |
| **Language**        | Java 17                 |
| **Database**        | MongoDB Atlas           |
| **API Integration** | Google Gemini AI        |
| **Real-Time**       | WebSocket               |
| **Build Tool**      | Maven                   |
| **Frontend**        | HTML5, CSS3, JavaScript |

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **MongoDB Atlas Account** (Free tier available)
- **Google Gemini API Key** (Get from [AI Studio](https://aistudio.google.com))
- **Git**

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/mail-clone.git
cd mail-clone/mail-clone
```

### 2. Setup Environment Variables

```bash
# Copy the template
cp .env.example .env

# Edit .env with your actual credentials
notepad .env
```

Edit `.env` and add your credentials:

```properties
# MongoDB Atlas Configuration (https://www.mongodb.com/cloud/atlas)
MONGODB_URI=mongodb+srv://YOUR_USERNAME:YOUR_PASSWORD@YOUR_CLUSTER.mongodb.net/?appName=mail-clone
MONGODB_DATABASE=emailhog

# Gemini AI Configuration (https://aistudio.google.com)
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The app will automatically read credentials from your `.env` file.

### 5. Access the Application

Open your browser and navigate to:

```
http://localhost:8080
```

## 📁 Project Structure

```
mail-clone/
├── .env                                       # Your credentials (private, not committed)
├── .env.example                               # Template with placeholders (public)
├── application.properties                     # Configuration with placeholders (public)
├── pom.xml                                    # Maven configuration
├── SETUP_INSTRUCTIONS.md                      # Setup guide for new developers
├── src/
│   ├── main/
│   │   ├── java/com/javahog/mail_clone/
│   │   │   ├── AttachmentController.java      # Handles file uploads
│   │   │   ├── InboxController.java           # Email inbox endpoints
│   │   │   ├── ComposeService.java            # Email composition logic
│   │   │   ├── InboxService.java              # Email retrieval & storage
│   │   │   ├── SpamAnalyserService.java       # AI-powered spam detection
│   │   │   ├── LinkCheckerService.java        # URL validation
│   │   │   ├── SmtpServerStarter.java         # SMTP server initialization
│   │   │   ├── WebSocketConfig.java           # WebSocket configuration
│   │   │   ├── Email.java                     # Email entity model
│   │   │   ├── EmailRepository.java           # MongoDB repository
│   │   │   └── MailCloneApplication.java      # Application entry point
│   │   ├── resources/
│   │   │   └── static/
│   │   │       └── index.html                 # Web UI
│   │   └── templates/                         # Thymeleaf templates (if any)
│   └── test/
│       └── MailCloneApplicationTests.java     # Unit tests
└── README.md                                  # This file
```

**Note:** `.env` file is automatically created when you run `cp .env.example .env`

## 🔌 API Endpoints

### Inbox

- **GET `/emails`** - Retrieve all emails
- **GET `/emails/{id}`** - Get specific email
- **POST `/emails/send`** - Send/compose new email

### Attachments

- **POST `/upload`** - Upload file attachment
- **GET `/download/{id}`** - Download attachment

### Spam Analysis

- **POST `/analyze-spam`** - Analyze email for spam
- **GET `/spam-results/{id}`** - Get spam analysis results

### Link Checking

- **POST `/check-links`** - Verify links in email content

## 🔐 Security

- **Environment Variables**: All sensitive data stored in `.env` file (not committed to Git) ✅
- **`.env` Protection**: Automatically blocked by `.gitignore` - your credentials won't leak ✅
- **`.env.example`**: Safe template file with placeholders - distributed for team setup ✅
- **No Hardcoded Secrets**: All API keys and credentials externalized from source code ✅
- **Dotenv Library**: `spring-dotenv` automatically loads variables from `.env` on startup ✅

### How It Works

1. **`.env.example`** - Public template (safe to commit)

   ```properties
   MONGODB_URI=mongodb+srv://YOUR_USERNAME:YOUR_PASSWORD@...
   GEMINI_API_KEY=YOUR_GEMINI_API_KEY_HERE
   ```

2. **`.env`** - Your private copy (NOT committed, blocked by `.gitignore`)

   ```properties
   MONGODB_URI=mongodb+srv://actualuser:actualpass@...
   GEMINI_API_KEY=ActualKeyXYZ...
   ```

3. **Spring Boot** - Automatically reads from `.env` at startup
   - No manual configuration needed
   - No environment variable setting needed
   - Clean and team-friendly approach

### For Team Members

New developers simply:

```bash
cp .env.example .env
# Edit .env with their own credentials
# Then run: mvn spring-boot:run
```

No manual setup or configuration files needed! ✅

## 📦 Dependencies

Key dependencies used:

```xml
<!-- Spring Boot Starters -->
<spring-boot-starter-web>
<spring-boot-starter-websocket>
<spring-boot-starter-webflux>
<spring-boot-starter-data-mongodb>
<spring-boot-starter-actuator>

<!-- Email Libraries -->
<subethasmtp> (SMTP Server)
<javax.mail> (Email handling)

<!-- Utilities -->
<lombok> (Boilerplate reduction)
```

## 🧪 Testing

Run tests with:

```bash
mvn test
```

## 🐛 Troubleshooting

### Connection refused on MongoDB

```
Error: Unable to connect to MongoDB
```

**Solution**: Verify your MongoDB Atlas connection string in `application.properties`:

```bash
# Test connection with Mongo Shell
mongosh "mongodb+srv://username:password@cluster.mongodb.net/database"
```

### Gemini API Error

```
Error: 401 Unauthorized on Gemini API
```

**Solution**:

1. Check your API key is correct
2. Ensure API is enabled in Google Cloud Console
3. Verify quota limits haven't been exceeded

### Port already in use

```
Error: Address already in use: bind
```

**Solution**: Change the port in `application.properties`:

```properties
server.port=8081
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is part of the MCA Program (2024-26 Batch).

---

## 📚 References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MongoDB Atlas Guide](https://docs.mongodb.com/atlas/)
- [Google Gemini API](https://ai.google.dev/)
- [WebSocket in Spring](https://spring.io/guides/gs/messaging-stomp-websocket/)

## 👤 Author

Sakthi - MCA Student

---

**Happy emailing! 📧**
