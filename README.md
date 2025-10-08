# InstaPound - Instagram-like Photo Sharing Backend

A comprehensive Java Spring Boot REST API for photo uploading, browsing, and management with user authentication, package tiers, and cloud storage support.

## 🎯 Features

### User Management
- **Anonymous Access**: Browse and download photos without registration
- **Registration**: Local account creation with email/username
- **OAuth2 Authentication**: Support for Google and GitHub sign-in
- **User Roles**: Anonymous, Registered, Administrator
- **Package Tiers**: FREE, PRO, GOLD with different limits
- **Package Management**: Change packages once per day (effective next day)
- **Usage Tracking**: Monitor upload counts, storage usage, and package limits

### Photo Management
- **Upload**: Photos with description and hashtags
- **Image Processing**: 
  - Resize to custom dimensions
  - Convert formats (PNG, JPG, BMP)
  - Apply filters (sepia, grayscale, blur, sharpen, vintage)
  - Automatic thumbnail generation
- **Storage Options**: Local filesystem or Cloudinary cloud storage
- **Browse**: View latest 10 photos with thumbnails by default
- **Search**: Advanced filtering by hashtags, author, date range, file size
- **Edit**: Update description and hashtags (owner and admin only)
- **Download**: Original or processed versions
- **Statistics**: View and download counts

### Security & Access Control
- **Authentication**: Local accounts and OAuth2
- **Authorization**: Role-based access control
- **Anonymous**: Browse and download only
- **Registered**: Upload, edit own photos, manage profile
- **Administrator**: Manage all users and photos, view statistics

### Audit Trail
- **Action Logging**: Every action is logged (who, when, what)
- **Statistics**: User activity tracking
- **IP Tracking**: Record IP addresses for security

## 🏗️ Architecture & Design Patterns

This application implements **8+ design patterns** and adheres to **SOLID principles**:

### Design Patterns Implemented

1. **Value Object Pattern** - Immutable data structures (`PackageLimits`, `PackageUsage`, `ImageProcessingOptions`)
2. **Repository Pattern** - Data access abstraction (all repository interfaces)
3. **Strategy Pattern** - Image Processing - Interchangeable algorithms (`ImageProcessor`, `BasicImageProcessor`)
4. **Strategy Pattern** - Storage Services - Local vs Cloud (`StorageService`, `LocalStorageService`, `CloudinaryStorageService`)
5. **Factory Pattern** - Storage service creation (`StorageServiceFactory`)
6. **Observer Pattern** - Action logging (`ActionLogger`)
7. **Specification Pattern** - Query composition (`PhotoSpecifications`)
8. **Singleton Pattern** - Spring beans (all `@Service`, `@Repository`, `@Component`)
9. **Dependency Injection Pattern** - Constructor injection throughout
10. **DTO Pattern** - Data transfer objects for API communication

See [DESIGN_PATTERNS.md](DESIGN_PATTERNS.md) for detailed documentation.

### SOLID Principles

✅ **Single Responsibility Principle** - Each class has one clear responsibility  
✅ **Open/Closed Principle** - Open for extension, closed for modification  
✅ **Liskov Substitution Principle** - Substitutable implementations  
✅ **Interface Segregation Principle** - Focused interfaces  
✅ **Dependency Inversion Principle** - Depend on abstractions, not concretions

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build & Run

```bash
# Clone the repository
git clone https://github.com/ldposavec/InstaPound.git
cd InstaPound

# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### H2 Database Console

Access the H2 console at: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:instapound`
- **Username**: `sa`
- **Password**: (empty)

## 📡 API Endpoints

### Authentication

- `POST /api/auth/register` - Register new user
  ```json
  {
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "packageType": "FREE"
  }
  ```

### Photos

- `GET /api/photos` - Get latest photos (paginated)
- `GET /api/photos/{id}` - Get specific photo
- `POST /api/photos` - Upload photo (multipart/form-data)
- `PUT /api/photos/{id}` - Update photo metadata
- `DELETE /api/photos/{id}` - Delete photo
- `POST /api/photos/search` - Search photos with criteria
- `GET /api/photos/{id}/download?processed=false` - Download photo

### Users

- `GET /api/users/{id}` - Get user profile
- `PUT /api/users/{id}/package` - Change package tier

## 📦 Package Tiers

| Package | Price  | Max Upload Size | Daily Uploads | Total Photos |
|---------|--------|-----------------|---------------|--------------|
| FREE    | $0.00  | 5 MB           | 5             | 50           |
| PRO     | $9.99  | 20 MB          | 50            | 1,000        |
| GOLD    | $29.99 | 100 MB         | 500           | 10,000       |

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 17
- **Database**: H2 (in-memory, easily switchable to PostgreSQL/MySQL)
- **Security**: Spring Security with BCrypt password encoding
- **OAuth2**: Support for Google and GitHub
- **Image Processing**: Java AWT
- **Cloud Storage**: Cloudinary (optional)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Spring Boot Test

## 🔧 Configuration

### Application Properties

Key configuration in `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:h2:mem:instapound
spring.jpa.hibernate.ddl-auto=create-drop

# File Upload
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Local Storage
app.storage.local.path=uploads

# Cloudinary (optional)
app.storage.cloudinary.cloud-name=
app.storage.cloudinary.api-key=
app.storage.cloudinary.api-secret=
```

### OAuth2 Setup (Optional)

To enable OAuth2 authentication, uncomment and configure in `application.properties`:

```properties
spring.security.oauth2.client.registration.google.client-id=your-google-client-id
spring.security.oauth2.client.registration.google.client-secret=your-google-client-secret
spring.security.oauth2.client.registration.github.client-id=your-github-client-id
spring.security.oauth2.client.registration.github.client-secret=your-github-client-secret
```

## 📊 Database Schema

The application uses JPA/Hibernate with the following entities:

- **User** - User accounts with authentication info
- **UserPackage** - Package definitions (FREE, PRO, GOLD)
- **Photo** - Uploaded photos with metadata
- **Hashtag** - Photo tags for categorization
- **ActionLog** - Audit trail of all actions

Relationships are properly mapped with JPA annotations.

## 🔐 Security

- **Password Encryption**: BCrypt hashing
- **CSRF Protection**: Disabled for REST API (can be enabled for web UI)
- **Session Management**: Stateless (JWT-ready)
- **Role-Based Access**: Method-level security
- **SQL Injection**: Protected by JPA/Hibernate
- **XSS**: Input validation with Jakarta Validation

## 🧪 Testing

Run the test suite:

```bash
./mvnw test
```

The project includes:
- Unit tests for services
- Integration tests for repositories
- Application context tests

## 📝 Code Quality

The codebase follows:
- **Clean Code** principles
- **SOLID** principles
- **DRY** (Don't Repeat Yourself)
- **KISS** (Keep It Simple, Stupid)
- Comprehensive JavaDoc comments
- Proper exception handling
- Logging at appropriate levels

## 🚀 Future Enhancements

- [ ] Add JWT token-based authentication
- [ ] Implement photo likes and comments
- [ ] Add user followers/following functionality
- [ ] Real-time notifications
- [ ] Image metadata extraction (EXIF data)
- [ ] Advanced search with full-text indexing
- [ ] Rate limiting for API endpoints
- [ ] Caching with Redis
- [ ] Async processing with message queues
- [ ] Microservices architecture

## 📄 License

This project is created for educational purposes.

## 👥 Authors

- Development Team - Initial work

## 🙏 Acknowledgments

- Spring Boot documentation
- Design Patterns: Elements of Reusable Object-Oriented Software
- Clean Code by Robert C. Martin
- Effective Java by Joshua Bloch

## 📞 Support

For questions and support, please open an issue in the GitHub repository.
