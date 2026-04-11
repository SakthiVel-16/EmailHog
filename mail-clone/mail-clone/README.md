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

### 1. Clone Repository

```bash
git clone https://github.com/yourusername/mail-clone.git
cd mail-clone/mail-clone
```

### 2. Create `.env` File

```bash
cp .env.example .env
```

### 3. Add Your Credentials

Edit `.env` with your MongoDB URI and Gemini API Key:

```properties
MONGODB_URI=YOUR_MONGODB_URI_HERE
MONGODB_DATABASE=emailhog
GEMINI_API_KEY=YOUR_GEMINI_API_KEY_HERE
```

Get credentials from:

- **MongoDB Atlas:** https://www.mongodb.com/cloud/atlas
- **Gemini API:** https://aistudio.google.com

### 4. Run Application

**Windows:**

```bash
mvnw.cmd spring-boot:run
```

**Mac/Linux:**

```bash
./mvnw spring-boot:run
```

### 5. Open Browser

```
http://localhost:8080
```

---

## 🔐 Security

✅ `.env` - Your credentials (private, blocked by `.gitignore`)
✅ `.env.example` - Safe template (public, on GitHub)
✅ `application.properties` - Placeholders only (public, on GitHub)

New team members just follow the 5 steps above!

---

## 📋 Project Structure

```
mail-clone/mail-clone/
├── .env                     # Your credentials (private)
├── .env.example             # Template (public)
├── application.properties   # Config (public)
├── pom.xml                  # Maven config
├── src/
│   ├── main/java/...        # Application code
│   └── resources/static/    # Web UI
└── README.md                # This file
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
