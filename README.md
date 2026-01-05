# 🏠 PVHCENIMA House Management API

A Spring Boot REST API for managing houses, rooms, and rentals with JWT authentication and AWS S3 file storage.

---

## 📋 Table of Contents

- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [Database Schema](#-database-schema)
- [Authentication](#-authentication)
- [API Endpoints](#-api-endpoints)
- [Project Structure](#-project-structure)
- [File Upload (S3)](#-file-upload-s3)
- [Error Handling](#-error-handling)

---

## 🗂 API Overview (Quick Reference)

| Controller | Base Path | Endpoints | Auth |
|------------|-----------|-----------|------|
| **Auth** | `/api/v1/auth` | 2 | 🔓 Public |
| **House** | `/api/v1/house` | 5 | 🔒 JWT |
| **Room** | `/api/v1/room` | 10 | 🔒 JWT |
| **Utility** | `/api/v1/utility` | 10 | 🔒 JWT |
| **File** | `/api/v1/file` | 1 | 🔓 Public |
| **Total** | | **28 endpoints** | |

---

## 🛠 Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.2.21 | Programming Language |
| Spring Boot | 4.0.1 | Framework |
| Spring Security | 7.0.2 | Authentication & Authorization |
| Spring Data JPA | 4.0.1 | Database ORM |
| PostgreSQL | - | Database |
| JWT (jjwt) | 0.11.5 | Token-based Authentication |
| AWS S3 SDK | 2.25.32 | File Storage |
| Swagger/OpenAPI | 2.8.4 | API Documentation |
| Flyway | - | Database Migrations |
| MapStruct | 1.6.3 | Object Mapping |

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- PostgreSQL
- AWS S3 Bucket (for file uploads)

### Installation

```bash
# Clone the repository
git clone <repository-url>

# Navigate to project directory
cd pvhcenima_api

# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

### Access

- **API Base URL**: `http://localhost:8080/api/v1`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/v3/api-docs`

---

## ⚙ Configuration

### application.properties

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/pvhcenima_db
spring.datasource.username=postgres
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.key=${JWT_KEY}
jwt.access-token-expiration=3600000      # 1 hour
jwt.refresh-token-expiration=86400000    # 24 hours

# AWS S3
cloud.aws.credentials.access-key=YOUR_ACCESS_KEY
cloud.aws.credentials.secret-key=YOUR_SECRET_KEY
cloud.aws.region.static=ap-southeast-2
cloud.aws.s3.bucket=your-bucket-name
```

---

## 🗄 Database Schema

### Entity Relationship Diagram

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│     app_user    │       │      house      │       │      room       │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ user_id (PK)    │──┐    │ house_id (PK)   │──┐    │ room_id (PK)    │
│ full_name       │  │    │ house_name      │  │    │ room_name       │
│ email (unique)  │  │    │ house_address   │  │    │ house_id (FK)   │──→ house
│ phone_number    │  │    │ house_image     │  │    │ renter_id (FK)  │──→ app_user
│ password        │  └──→ │ owner_id (FK)   │  └──→ │                 │
│ role            │       └─────────────────┘       └─────────────────┘
│ profile_image   │                                          │
└─────────────────┘                                          │
                                                    ┌────────┴────────┐
                                                    ▼                 ▼
                                         ┌─────────────────┐  ┌─────────────────┐
                                         │   room_images   │  │     utility     │
                                         ├─────────────────┤  ├─────────────────┤
                                         │ room_id (FK)    │  │ utility_id (PK) │
                                         │ image_url       │  │ room_id (FK)    │
                                         └─────────────────┘  │ is_pay          │
                                                              │ old_water       │
                                                              │ new_water       │
                                                              │ room_cost       │
                                                              │ water_cost      │
                                                              │ total_cost      │
                                                              │ month           │
                                                              └─────────────────┘
```

### Entities

#### User (`app_user`)
| Column | Type | Description |
|--------|------|-------------|
| user_id | UUID | Primary Key |
| full_name | String | User's full name |
| email | String | Unique email |
| phone_number | String | Unique phone number |
| password | String | BCrypt encrypted |
| role | Enum | RENTER, HOUSEOWNER, ADMIN |
| profile_image | String | S3 URL (nullable) |

#### House (`house`)
| Column | Type | Description |
|--------|------|-------------|
| house_id | UUID | Primary Key |
| house_name | String | Name of the house |
| house_address | String | Address |
| house_image | String | S3 URL (nullable) |
| owner_id | UUID | FK → User |

#### Room (`room`)
| Column | Type | Description |
|--------|------|-------------|
| room_id | UUID | Primary Key |
| room_name | String | Name of the room |
| house_id | UUID | FK → House |
| renter_id | UUID | FK → User (nullable) |

#### Room Images (`room_images`)
| Column | Type | Description |
|--------|------|-------------|
| room_id | UUID | FK → Room |
| image_url | String | S3 URL |

#### Utility (`utility`)
| Column | Type | Description |
|--------|------|-------------|
| utility_id | UUID | Primary Key |
| room_id | UUID | FK → Room |
| is_pay | Boolean | Payment status |
| old_water | Double | Previous water reading |
| new_water | Double | Current water reading |
| room_cost | BigDecimal | Monthly room rent |
| water_cost | BigDecimal | Water usage cost |
| total_cost | BigDecimal | room_cost + water_cost |
| month | LocalDate | Billing month |

---

## 🔐 Authentication

### JWT Token Flow

```
┌─────────┐     1. Login      ┌─────────┐     2. Generate     ┌─────────┐
│  User   │ ───────────────→  │   API   │ ────────────────→   │  JWT    │
│         │   email/phone     │         │     Token           │  Token  │
│         │   + password      │         │                     │         │
└─────────┘                   └─────────┘                     └─────────┘
                                                                   │
     ┌─────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────────┐
│  JWT Token Contains:                                                 │
│  - sub: user email/phone                                            │
│  - userId: user's UUID                                              │
│  - exp: expiration timestamp                                        │
└─────────────────────────────────────────────────────────────────────┘
```

### Roles & Permissions

| Role | Permissions |
|------|-------------|
| RENTER | View rooms, view rented rooms |
| HOUSEOWNER | All RENTER + manage houses & rooms |
| ADMIN | All permissions |

---

## 📡 API Endpoints

> **Legend**: 🔓 = Public (no auth) | 🔒 = Protected (requires JWT)

### Authentication (`/api/v1/auth`) 🔓

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/register` | Register new user |
| POST | `/login` | Login and get JWT token |

#### Register Request
```json
{
    "fullName": "John Doe",
    "phoneNumber": "0123456789",
    "password": "password123",
    "email": "john@example.com",
    "role": "RENTER"
}
```

#### Login Request
```json
{
    "emailOrPhonenumber": "john@example.com",
    "password": "password123"
}
```

#### Login Response
```json
{
    "success": true,
    "message": "Login successful",
    "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### House (`/api/v1/house`) 🔒

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create a new house |
| GET | `/` | Get all my houses |
| GET | `/{houseId}` | Get house by ID |
| PUT | `/{houseId}` | Update house |
| DELETE | `/{houseId}` | Delete house |

#### Create House Request
```json
{
    "houseName": "My Villa",
    "houseAddress": "123 Main Street",
    "houseImage": "https://s3.../image.jpg"
}
```

---

### Room (`/api/v1/room`) 🔒

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create a new room |
| GET | `/{roomId}` | Get room by ID |
| PUT | `/{roomId}` | Update room |
| DELETE | `/{roomId}` | Delete room |
| GET | `/my-rooms` | Get all rooms in my houses |
| GET | `/house/{houseId}` | Get rooms by house |
| GET | `/house/{houseId}/available` | Get available rooms |
| GET | `/my-rented` | Get rooms I'm renting |
| POST | `/{roomId}/assign-renter/{renterId}` | Assign renter |
| DELETE | `/{roomId}/remove-renter` | Remove renter |

#### Create Room Request
```json
{
    "roomName": "Room A1",
    "houseId": "550e8400-e29b-41d4-a716-446655440000",
    "images": [
        "https://s3.../img1.jpg",
        "https://s3.../img2.jpg"
    ]
}
```

#### Room Response
```json
{
    "success": true,
    "data": {
        "roomId": "...",
        "roomName": "Room A1",
        "houseId": "...",
        "houseName": "My Villa",
        "renterId": null,
        "renterName": null,
        "isAvailable": true,
        "images": ["https://s3.../img1.jpg", "https://s3.../img2.jpg"]
    }
}
```

---

### Utility (`/api/v1/utility`) 🔒

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create utility record (oldWater auto-fetched) |
| GET | `/{utilityId}` | Get utility by ID |
| PATCH | `/{utilityId}/pay` | Mark utility as paid/unpaid |
| DELETE | `/{utilityId}` | Delete utility record |
| GET | `/my-utilities` | Get all utilities for my rooms |
| GET | `/room/{roomId}` | Get utilities by room |
| GET | `/room/{roomId}/latest` | Get latest utility (current water reading) |
| GET | `/room/{roomId}/unpaid` | Get unpaid utilities for a room |
| GET | `/house/{houseId}` | Get utilities by house |
| GET | `/month/{month}` | Get utilities by month (yyyy-MM-dd) |

#### Create Utility Request
```json
{
    "roomId": "550e8400-e29b-41d4-a716-446655440000",
    "newWater": 150.5,
    "roomCost": 50,
    "waterCostPerUnit": 5000,
    "month": "2026-01-15",
    "oldWater": 100.0  // Optional: only for FIRST record of new room
}
```

#### Mark Paid Request
```json
{
    "isPay": true
}
```

#### Utility Response
```json
{
    "success": true,
    "data": {
        "utilityId": "...",
        "roomId": "...",
        "roomName": "Room A1",
        "houseName": "My Villa",
        "isPay": false,
        "oldWater": 100.0,
        "newWater": 150.5,
        "waterUsage": 50.5,
        "roomCost": 50,
        "waterCost": 252500,
        "totalCost": 252550,
        "month": "2026-01-15"
    }
}
```

---

### File Upload (`/api/v1/file`) 🔓

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/upload` | Upload image to S3 |

#### Upload (multipart/form-data)
```
file: [image file]
```

#### Response
```json
{
    "success": true,
    "message": "Image uploaded successfully",
    "data": "https://bucket.s3.region.amazonaws.com/images/abc-123.jpg"
}
```

---

## 📁 File Upload (S3)

### Workflow

```
1. Client uploads image:
   POST /api/v1/file/upload
   Content-Type: multipart/form-data
   
2. Server uploads to S3 and returns URL:
   "https://bucket.s3.region.amazonaws.com/images/uuid.jpg"
   
3. Client uses URL when creating/updating entities:
   POST /api/v1/room
   { "roomName": "...", "images": ["https://..."] }
```

### S3 Configuration

1. Create S3 bucket in AWS Console
2. Disable "Block Public Access"
3. Add bucket policy:
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::your-bucket-name/*"
        }
    ]
}
```

---

## ⚠️ Error Handling

### Response Format

All responses follow this structure:

```json
{
    "success": true/false,
    "message": "Description",
    "data": { ... },       // Only on success
    "errors": [ ... ]      // Only on validation errors
}
```

### HTTP Status Codes

| Code | Meaning | When |
|------|---------|------|
| 200 | OK | Successful request |
| 401 | Unauthorized | No token / Invalid token / Expired token |
| 403 | Forbidden | Valid token but insufficient permissions |
| 400 | Bad Request | Validation error / Invalid input |
| 404 | Not Found | Resource not found |
| 500 | Server Error | Unexpected error |

### Error Examples

#### 401 Unauthorized
```json
{
    "success": false,
    "message": "Unauthorized - Please provide a valid token"
}
```

#### 403 Forbidden
```json
{
    "success": false,
    "message": "Forbidden - You don't have permission to access this resource"
}
```

#### 400 Bad Request
```json
{
    "success": false,
    "message": "Email already exists"
}
```

---

## 📂 Project Structure

```
src/main/kotlin/com/example/pvhcenima_api/
│
├── 📄 PvhcenimaApiApplication.kt    # Main application entry point
│
├── 📁 config/                        # Configuration & Security
│   ├── Configuration.kt              # Bean configurations
│   ├── SecurityConfiguration.kt      # Spring Security setup
│   ├── JwtAuthenticationFilter.kt    # JWT validation filter
│   ├── JwtProperties.kt              # JWT config properties
│   ├── CustomAuthEntryPoint.kt       # 401 Unauthorized handler
│   ├── CustomAccessDeniedHandler.kt  # 403 Forbidden handler
│   ├── OpenApiConfig.kt              # Swagger/OpenAPI config
│   └── S3Config.kt                   # AWS S3 client config
│
├── 📁 controller/                    # REST API Controllers
│   ├── AuthController.kt             # POST /api/v1/auth/*
│   ├── HouseController.kt            # CRUD /api/v1/house/*
│   ├── RoomController.kt             # CRUD /api/v1/room/*
│   ├── UtilityController.kt          # CRUD /api/v1/utility/*
│   └── FileController.kt             # POST /api/v1/file/upload
│
├── 📁 service/                       # Business Logic Layer
│   ├── AuthService.kt                # Auth interface
│   ├── HouseService.kt               # House interface
│   ├── RoomService.kt                # Room interface
│   ├── UtilityService.kt             # Utility interface
│   ├── TokenService.kt               # JWT generate/validate
│   ├── CurrentUserService.kt         # Extract user from SecurityContext
│   ├── CustomUserDetailService.kt    # Spring Security UserDetailsService
│   ├── S3Service.kt                  # AWS S3 operations
│   └── 📁 serviceImplement/          # Service Implementations
│       ├── AuthServiceImplement.kt
│       ├── HouseServiceImplement.kt
│       ├── RoomServiceImplement.kt
│       └── UtilityServiceImplement.kt
│
├── 📁 repository/                    # Data Access Layer (JPA)
│   ├── UserRepository.kt
│   ├── HouseRepository.kt
│   ├── RoomRepository.kt
│   └── UtilityRepository.kt
│
├── 📁 model/                         # Data Models
│   ├── 📁 entity/                    # JPA Entities
│   │   ├── User.kt                   # app_user table
│   │   ├── House.kt                  # house table
│   │   ├── Room.kt                   # room table
│   │   ├── Utility.kt                # utility table
│   │   └── Role.kt                   # Enum: RENTER, HOUSEOWNER, ADMIN
│   │
│   ├── 📁 request/                   # Request DTOs
│   │   ├── UserRequest.kt            # Register request
│   │   ├── UserLogin.kt              # Login request
│   │   ├── HouseRequest.kt           # House CRUD request
│   │   ├── RoomRequest.kt            # Room CRUD request
│   │   └── UtilityRequest.kt         # Utility CRUD request
│   │
│   └── 📁 response/                  # Response DTOs
│       ├── BaseResponse.kt           # Generic API response wrapper
│       ├── HouseResponse.kt
│       ├── HouseOwnerDto.kt          # Owner info in house response
│       ├── RoomResponse.kt
│       └── UtilityResponse.kt
│
└── 📁 exception/                     # Error Handling
    └── GlobalExceptionHandler.kt     # @RestControllerAdvice
```

---

## 🔧 Development

### Run Tests
```bash
./gradlew test
```

### Build JAR
```bash
./gradlew build
```

### Run with Docker (optional)
```bash
docker-compose up -d
```

---

## 📝 License

MIT License

---

## 👨‍💻 Author

PVHCENIMA Team

