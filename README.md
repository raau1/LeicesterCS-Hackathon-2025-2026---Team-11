# Study Buddy

A Spring Boot application for students to find study partners by year, module, and preferences.

## Features

- User authentication with JWT tokens
- Create study sessions with customizable details
- Browse and filter sessions by year and module
- Join request system for sessions
- User profiles with ratings and stats

## Tech Stack

- **Backend**: Spring Boot 3.2, Spring Security, Spring Data JPA
- **Database**: H2 (development), MySQL/PostgreSQL (production)
- **Authentication**: JWT (JSON Web Tokens)
- **Frontend**: HTML, CSS, JavaScript (vanilla)

## Project Structure

```
├── src/main/java/com/studybuddy/
│   ├── StudyBuddyApplication.java    # Main application
│   ├── config/                       # Security & JWT config
│   ├── controller/                   # REST controllers
│   ├── dto/                          # Data Transfer Objects
│   ├── model/                        # JPA entities
│   ├── repository/                   # Data access layer
│   └── service/                      # Business logic
├── src/main/resources/
│   ├── application.properties        # App configuration
│   └── static/                       # Frontend files
│       ├── index.html
│       ├── css/styles.css
│       └── js/
├── pom.xml                           # Maven dependencies
└── README.md
```

## Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### 1. Clone the Repository

```bash
git clone <repository-url>
cd LeicesterCS-Hackathon-2025-2026---Team-11
```

### 2. Configure Application (Optional)

Edit `src/main/resources/application.properties` to customize:
- JWT secret key
- Database settings
- Server port

### 3. Build and Run

```bash
# Using Maven
mvn spring-boot:run

# Or build JAR and run
mvn clean package
java -jar target/study-buddy-1.0.0.jar
```

### 4. Access the Application

- **Web App**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:studybuddy`
  - Username: `sa`
  - Password: (empty)

## API Endpoints

### Authentication

```
POST /api/auth/signup    - Register new user
POST /api/auth/login     - Login user
```

### Sessions

```
GET    /api/sessions              - Get all sessions (with filters)
GET    /api/sessions/{id}         - Get session by ID
POST   /api/sessions              - Create new session (auth required)
DELETE /api/sessions/{id}         - Delete session (creator only)
GET    /api/sessions/my-sessions  - Get user's created sessions
GET    /api/sessions/joined       - Get user's joined sessions
POST   /api/sessions/{id}/request - Request to join session
POST   /api/sessions/{id}/accept/{userId}  - Accept join request
POST   /api/sessions/{id}/decline/{userId} - Decline join request
```

### Users

```
GET  /api/users/me        - Get current user profile
GET  /api/users/me/stats  - Get user statistics
GET  /api/users/{id}      - Get user by ID
PUT  /api/users/me/modules - Update user's modules
```

## Database Schema

### Users Table
```sql
users (
    id BIGINT PRIMARY KEY,
    name VARCHAR,
    email VARCHAR UNIQUE,
    password VARCHAR,
    year VARCHAR,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
)
```

### Study Sessions Table
```sql
study_sessions (
    id BIGINT PRIMARY KEY,
    title VARCHAR,
    module VARCHAR,
    year VARCHAR,
    session_date DATE,
    session_time TIME,
    duration INT,
    location VARCHAR,
    max_participants INT,
    description TEXT,
    creator_id BIGINT,
    status VARCHAR,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
)
```

### Ratings Table
```sql
ratings (
    id BIGINT PRIMARY KEY,
    from_user_id BIGINT,
    to_user_id BIGINT,
    session_id BIGINT,
    score INT,
    comment TEXT,
    created_at TIMESTAMP
)
```

## Day 1 Deliverables

### Backend Team
- [x] Spring Boot project setup with Maven
- [x] JPA entities (User, StudySession, Rating)
- [x] Repositories with custom queries
- [x] Service layer with business logic
- [x] REST controllers for all endpoints
- [x] JWT authentication with Spring Security
- [x] Session CRUD operations
- [x] Join request system

### Frontend Team
- [x] Single-page application structure
- [x] Responsive navigation with routing
- [x] Authentication UI (login/signup forms)
- [x] Session creation form
- [x] Browse sessions page with filters
- [x] Session card component
- [x] User profile page skeleton
- [x] API integration layer

## Testing the API

### Using cURL

```bash
# Sign up
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@test.com","password":"password123","year":"2"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@test.com","password":"password123"}'

# Create session (use token from login response)
curl -X POST http://localhost:8080/api/sessions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title":"Data Structures Study",
    "module":"CS201",
    "year":"2",
    "date":"2025-01-25",
    "time":"14:00",
    "duration":60,
    "location":"Library Room 204",
    "maxParticipants":4,
    "preferences":["quiet","practice"]
  }'

# Get all sessions
curl http://localhost:8080/api/sessions
```

## Next Steps (Day 2)

- Add real-time updates with WebSockets
- Implement rating/review system
- Build chat/messaging feature
- Add email notifications
- Calendar integration
- Enhanced search and filtering

## Production Deployment

1. Update `application.properties` for production:
   - Use MySQL/PostgreSQL instead of H2
   - Set secure JWT secret
   - Configure HTTPS

2. Build production JAR:
   ```bash
   mvn clean package -Pprod
   ```

3. Deploy to your server or cloud platform

## Team

- Team 11 - Leicester CS Hackathon 2025-2026

## License

MIT
