# InstaPound - Design Patterns and SOLID Principles Documentation

## Overview
InstaPound is a photo-sharing web application backend built with Java Spring Boot, implementing multiple design patterns and adhering to SOLID principles.

## Design Patterns Implemented (8+)

### 1. Value Object Pattern
**Location:** `domain.valueobject` package
- `PackageLimits`: Immutable value object encapsulating package limitations
- `PackageUsage`: Value object tracking package consumption
- `ImageProcessingOptions`: Value object for image processing configuration

**Why:** Ensures immutability and encapsulation of related data, making the domain model more expressive and type-safe.

**SOLID:** Adheres to Single Responsibility Principle (SRP) - each value object has one clear purpose.

---

### 2. Repository Pattern
**Location:** `repository` package
- `UserRepository`, `PhotoRepository`, `HashtagRepository`, `UserPackageRepository`, `ActionLogRepository`

**Why:** Abstracts data access logic, providing a collection-like interface for domain entities. Makes it easy to change persistence mechanisms without affecting business logic.

**SOLID:** Adheres to Dependency Inversion Principle (DIP) - services depend on repository interfaces, not concrete implementations.

---

### 3. Strategy Pattern - Image Processing
**Location:** `util.ImageProcessor` and `util.BasicImageProcessor`
- Interface: `ImageProcessor`
- Concrete Implementation: `BasicImageProcessor`

**Why:** Allows different image processing algorithms to be interchanged at runtime. Can easily add new processors (e.g., CloudinaryImageProcessor) without modifying existing code.

**SOLID:** Adheres to Open/Closed Principle (OCP) - open for extension, closed for modification. Also adheres to Interface Segregation Principle (ISP).

---

### 4. Strategy Pattern - Storage Service
**Location:** `util.StorageService`, `util.LocalStorageService`, `util.CloudinaryStorageService`
- Interface: `StorageService`
- Implementations: `LocalStorageService`, `CloudinaryStorageService`

**Why:** Enables switching between different storage backends (local filesystem, Cloudinary) without changing business logic.

**SOLID:** Adheres to Open/Closed Principle (OCP) and Dependency Inversion Principle (DIP).

---

### 5. Factory Pattern
**Location:** `util.StorageServiceFactory`

**Why:** Creates appropriate storage service instances based on the requested storage type. Centralizes object creation logic and hides instantiation details.

**SOLID:** Adheres to Single Responsibility Principle (SRP) - only responsible for creating storage services. Also adheres to Dependency Inversion Principle (DIP).

---

### 6. Observer Pattern
**Location:** `service.ActionLogger`

**Why:** Logs all user actions throughout the system. Acts as an observer that reacts to events (actions) happening in the application.

**SOLID:** Adheres to Single Responsibility Principle (SRP) - only responsible for logging actions.

---

### 7. Specification Pattern
**Location:** `service.PhotoSpecifications`

**Why:** Encapsulates query logic in reusable, composable specifications. Makes complex queries more maintainable and testable.

**SOLID:** Adheres to Open/Closed Principle (OCP) - can add new specifications without modifying existing ones.

---

### 8. Singleton Pattern
**Location:** Spring-managed beans (all `@Service`, `@Component`, `@Repository` beans)
- Examples: All service classes, repositories, configuration classes

**Why:** Spring Boot creates singleton beans by default, ensuring only one instance exists throughout the application lifecycle. Reduces memory footprint and ensures consistent state.

**SOLID:** All beans adhere to Single Responsibility Principle (SRP).

---

### Additional Patterns:

### 9. Dependency Injection Pattern
**Location:** Throughout the application using `@RequiredArgsConstructor` and constructor injection

**Why:** Promotes loose coupling and makes the application more testable. Dependencies are injected rather than created internally.

**SOLID:** Core implementation of Dependency Inversion Principle (DIP).

---

### 10. DTO (Data Transfer Object) Pattern
**Location:** `dto` package
- `UserRegistrationRequest`, `PhotoUploadRequest`, `PhotoResponse`, `UserResponse`, `PhotoSearchRequest`

**Why:** Separates internal domain models from external API representations. Allows versioning and prevents exposure of internal structure.

**SOLID:** Adheres to Single Responsibility Principle (SRP) and Interface Segregation Principle (ISP).

---

## SOLID Principles Application

### Single Responsibility Principle (SRP)
✅ **Applied throughout:**
- Each entity has one responsibility (User, Photo, Hashtag, etc.)
- Each service handles one aspect of business logic (UserService, PhotoService)
- Each repository handles one entity's data access
- Value objects encapsulate specific data concepts

**Examples:**
- `UserService`: Only manages user-related operations
- `PhotoService`: Only manages photo-related operations
- `ActionLogger`: Only responsible for logging actions

---

### Open/Closed Principle (OCP)
✅ **Applied in:**
- Strategy Pattern implementations (ImageProcessor, StorageService)
- Specification Pattern (PhotoSpecifications)
- Entity validation methods (User.canUpload can be extended)

**Examples:**
- Can add new image filters without modifying BasicImageProcessor
- Can add new storage providers without changing existing code
- Can add new photo search specifications without modifying existing ones

---

### Liskov Substitution Principle (LSP)
✅ **Applied in:**
- All strategy implementations can substitute their interfaces
- LocalStorageService and CloudinaryStorageService can be used interchangeably
- All repository implementations extend JpaRepository

**Examples:**
- Any `StorageService` implementation can be used wherever the interface is expected
- Any `ImageProcessor` implementation works with the same contract

---

### Interface Segregation Principle (ISP)
✅ **Applied in:**
- Focused interfaces (ImageProcessor, StorageService)
- DTOs contain only necessary data for specific operations
- Repository interfaces only define needed query methods

**Examples:**
- `StorageService` interface only has store, retrieve, delete methods
- `ImageProcessor` interface only has image processing methods
- Each DTO is tailored to its specific use case

---

### Dependency Inversion Principle (DIP)
✅ **Applied throughout:**
- Services depend on repository interfaces, not implementations
- PhotoService depends on StorageServiceFactory (abstraction), not concrete storage services
- All dependencies are injected through constructors

**Examples:**
- `PhotoService` depends on `StorageServiceFactory` interface
- `UserService` depends on `UserRepository` interface
- All service layer components depend on abstractions, not concretions

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                 REST Controllers                     │
│  (AuthController, PhotoController, UserController)  │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│                 Service Layer                        │
│     (UserService, PhotoService, ActionLogger)       │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│              Repository Layer                        │
│  (UserRepository, PhotoRepository, etc.)            │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│              Domain Entities                         │
│    (User, Photo, Hashtag, ActionLog, etc.)         │
└─────────────────────────────────────────────────────┘
```

## Technology Stack

- **Framework:** Spring Boot 3.5.6
- **Language:** Java 17
- **Database:** H2 (in-memory)
- **Security:** Spring Security with OAuth2
- **Cloud Storage:** Cloudinary (optional)
- **Build Tool:** Maven

## Key Features

1. **User Management**
   - Local registration and OAuth2 (Google, GitHub)
   - Three package tiers (FREE, PRO, GOLD)
   - Package changes with next-day activation
   - Package usage tracking

2. **Photo Management**
   - Upload with metadata (description, hashtags)
   - Image processing (resize, filters, format conversion)
   - Multiple storage options (local, Cloudinary)
   - Thumbnail generation
   - Download original or processed versions

3. **Search & Browse**
   - Latest photos view
   - Advanced search by hashtags, author, date, size
   - Pagination support

4. **Access Control**
   - Anonymous users: Browse and download
   - Registered users: Upload, edit, delete own photos
   - Administrators: Manage all users and photos

5. **Audit Trail**
   - Comprehensive action logging
   - Track who did what and when
   - Statistics and reporting

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `GET /api/auth/oauth2/success` - OAuth2 success callback
- `GET /api/auth/oauth2/failure` - OAuth2 failure callback

### Photos
- `GET /api/photos` - Get latest photos
- `GET /api/photos/{id}` - Get specific photo
- `POST /api/photos` - Upload photo
- `PUT /api/photos/{id}` - Update photo metadata
- `DELETE /api/photos/{id}` - Delete photo
- `POST /api/photos/search` - Search photos
- `GET /api/photos/{id}/download` - Download photo

### Users
- `GET /api/users/{id}` - Get user profile
- `PUT /api/users/{id}/package` - Change package

## Running the Application

```bash
# Build the application
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Access H2 console
http://localhost:8080/h2-console
```

## Testing

```bash
# Run tests
./mvnw test
```

## Conclusion

InstaPound demonstrates professional software engineering practices by implementing multiple design patterns and adhering to SOLID principles. The architecture is modular, maintainable, and extensible, making it easy to add new features without breaking existing functionality.
