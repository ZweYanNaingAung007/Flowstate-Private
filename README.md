# Flowstate — Focus & Productivity Intelligence Platform

Flowstate is a backend productivity platform built with Spring Boot. It is designed to help individuals organize their work, manage priorities, track progress, and develop better working habits.

The platform combines task management, user management, and focused work sessions to provide a foundation for understanding and improving personal productivity. It emphasizes RESTful API design, secure data access, maintainable architecture, and efficient backend operations.

## Features

### Task Management

- Create, retrieve, update, and delete tasks.
- Search tasks by title or completion status.
- Organize tasks into categories.
- Pagination and sorting support.
- Task status tracking.
- Soft deletion and task restoration.

### User Management & Security

- User registration and authentication.
- JWT-based authentication and authorization.
- User-specific task and category management.
- Access control to ensure users can only manage their own resources.

### Focus Session Management

- Start, complete, and cancel focus sessions.
- Associate focus sessions with tasks.
- Track planned and actual focus durations.
- Maintain focus session history.

### Productivity Analytics

- Track daily and weekly focused time.
- Monitor completed focus sessions.
- Analyze time spent on individual tasks.
- Generate productivity statistics.

### Backend Engineering

- DTO-based request and response handling.
- Input validation.
- Centralized exception handling.
- Unit and integration testing.
- API rate limiting.
- Caching for frequently accessed data.
- Application monitoring with Spring Boot Actuator.

## Technology Stack

- **Language:** Java 21
- **Framework:** Spring Boot 4
- **Web:** Spring Web
- **Persistence:** Spring Data JPA, Hibernate
- **Database:** H2
- **Security:** Spring Security, JWT
- **Validation:** Jakarta Bean Validation
- **Caching:** Spring Cache, Caffeine
- **Rate Limiting:** Bucket4j
- **Testing:** JUnit 5, Mockito, Spring Boot Test, MockMvc
- **API Documentation:** Swagger / OpenAPI
- **Build Tool:** Maven
- **Other:** Lombok, Spring Boot Actuator

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher

## API Endpoints

All API endpoints are organized under the `/api/v1` base path.

### Task Management

All task endpoints are prefixed with `/api/v1/tasks`.

#### Get All Tasks

- **GET** `/api/v1/tasks`
- Query Parameters:
  - `page` (default: 0)
  - `size` (default: 10)
  - `sortBy` (default: `createdAt`)
  - `sortDir` (default: `DESC`)
- Returns a paginated list of tasks.

#### Get Task by ID

- **GET** `/api/v1/tasks/{id}`
- Path Variable: `id` (Long)
- Returns a specific task.

#### Create Task

- **POST** `/api/v1/tasks`
- Body: `TaskRequest` JSON
- Creates and returns a task.

#### Update Task

- **PUT** `/api/v1/tasks/{id}`
- Path Variable: `id` (Long)
- Body: `TaskRequest` JSON
- Updates and returns the specified task.

#### Delete Task

- **DELETE** `/api/v1/tasks/{id}`
- Path Variable: `id` (Long)
- Soft-deletes the specified task.

#### Search Tasks

- **GET** `/api/v1/tasks/search`
- Query Parameters:
  - `title` (optional)
  - `completed` (optional, Boolean)
  - `page`, `size`, `sortBy`, `sortDir`
- Returns paginated search results.

#### Get Tasks by Completion Status

- **GET** `/api/v1/tasks/completed/{status}`
- Path Variable: `status` (Boolean)
- Returns tasks matching the specified completion status.

#### Search Tasks by Title

- **GET** `/api/v1/tasks/search-by-title`
- Query Parameter: `title`
- Returns tasks matching the specified title.

### Category Management

All category endpoints are prefixed with `/api/v1/categories`.

#### Get Category by ID

- **GET** `/api/v1/categories/{id}`
- Path Variable: `id` (Long)
- Returns a specific category.

#### Create Category

- **POST** `/api/v1/categories`
- Body: Category JSON
- Creates and returns a category.

### User Management

All user endpoints are prefixed with `/api/v1/auth` or `/api/v1/users`.

Planned endpoints:

- **POST** `/api/v1/auth/register` — Register a new user.
- **POST** `/api/v1/auth/login` — Authenticate and receive a JWT.
- **GET** `/api/v1/users/me` — Retrieve the authenticated user's profile.

### Focus Session Management

All focus session endpoints are prefixed with `/api/v1/focus-sessions`.

Planned endpoints:

- **POST** `/api/v1/focus-sessions/start` — Start a focus session.
- **PATCH** `/api/v1/focus-sessions/{id}/complete` — Complete a focus session.
- **PATCH** `/api/v1/focus-sessions/{id}/cancel` — Cancel a focus session.
- **GET** `/api/v1/focus-sessions` — Retrieve focus session history.
- **GET** `/api/v1/focus-sessions/{id}` — Retrieve a specific focus session.
- **GET** `/api/v1/focus-sessions/statistics` — Retrieve productivity statistics.

### Application Information

#### Get App Info

- **GET** `/api/v1/info`
- Returns application name, version, and maximum tasks per page.

#### Get Advanced App Info

- **GET** `/api/v1/info-advanced`
- Returns application configuration information.

## Database

Flowstate uses an H2 in-memory database for local development.

No external database installation is required.

The database is recreated when the application restarts. It is intended for development and demonstration purposes.

## Monitoring

Spring Boot Actuator provides application monitoring and management capabilities.

Available endpoints may include:

- `/actuator/health` — Application health status.
- `/actuator/info` — Application information.
- `/actuator/metrics` — Application metrics.
- `/actuator/env` — Environment properties.
- `/actuator/loggers` — Logger configuration.
- `/actuator/beans` — Spring beans.
- `/actuator/mappings` — Request mappings.

Endpoint availability depends on the application's Actuator configuration.

## Testing

The project uses a combination of unit and integration testing.

Testing technologies include:

- **JUnit 5** for test organization and execution.
- **Mockito** for mocking dependencies.
- **Spring Boot Test** for application-level testing.
- **MockMvc** for testing REST API endpoints.

Tests cover business logic, validation, authentication, authorization, and API behavior as these features are implemented.

## Running Locally

### 1. Clone the repository

### 2. Navigate to the project directory

```bash
cd flowstate
```

### 3. Run the application

On Windows:

```bash
mvnw.cmd spring-boot:run
```

On Linux or macOS:

```bash
./mvnw spring-boot:run
```

## Project Status

**Under active development.**

Flowstate is being developed incrementally, with a focus on backend architecture, security, data management, performance optimization, and automated testing.
