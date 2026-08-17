# WorkTime

WorkTime is a full-stack work and time management application built with Spring Boot, Angular and PostgreSQL.

The application allows administrators to manage users, projects and tasks while employees can view their assigned work, update task statuses and track working time.

## Features

### Authentication and authorization

- Session-based authentication
- BCrypt password hashing
- Role-based access control
- `ADMIN` and `EMPLOYEE` roles
- Protected Angular routes
- HTTP authentication interceptor
- Logout confirmation and session termination
- Centralized API error notifications

### User management

- Create and update users
- Assign administrator or employee roles
- Activate and deactivate accounts
- Case-insensitive email handling
- Prevent inactive users from signing in or receiving new tasks
- Restrict user management to administrators

### Project management

- Create and update projects
- Manage project lifecycle statuses
- Filter projects by name, status and date range
- Sort projects by end date
- Prevent invalid project date ranges
- Prevent completed or cancelled projects from receiving new tasks
- Keep projects as inactive records instead of physically deleting them

### Task management

- Create and update tasks
- Assign tasks to users and projects
- Define priority, status, due date and estimated duration
- Filter tasks by priority, status, project and assigned user
- Restrict employees to their assigned tasks
- Allow employees to update the status of assigned tasks
- Detect and display overdue open tasks
- Record task status history with the responsible user and timestamp

### Time tracking

- Start and stop timers
- Record start and end times on the backend
- Calculate elapsed duration on the backend
- Allow only one active timer per user
- Restore the active timer after a page refresh
- Create manual time entries
- Validate manual entry dates and duration
- Display personal time entry history
- Calculate daily and weekly totals
- Generate project-based and user-based time reports

### Dashboard and reporting

- Daily tracked-time card
- Weekly tracked-time card
- Overdue task count
- Active timer status
- Overdue task detail list
- Daily tracked-time bar chart
- Inclusive date range filter
- CSV export for the selected date range
- Responsive Angular Material interface

## Technology stack

### Backend

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Bean Validation
- PostgreSQL
- Flyway
- Lombok
- SpringDoc OpenAPI
- Maven Wrapper
- JUnit 5
- Mockito
- Spring Security Test

### Frontend

- Angular 22
- Angular Material 22
- TypeScript 6
- RxJS 7
- Signals and computed state
- Standalone Angular components
- Lazy-loaded routes
- Vitest
- npm 11

## Roles

| Capability                    | Admin | Employee |
| ----------------------------- | :---: | :------: |
| Manage users                  |  Yes  |    No    |
| Create and update projects    |  Yes  |    No    |
| View projects                 |  Yes  |   Yes    |
| Create tasks                  |  Yes  |    No    |
| Fully update tasks            |  Yes  |    No    |
| View accessible tasks         |  Yes  |   Yes    |
| Update assigned task status   |  Yes  |   Yes    |
| View task status history      |  Yes  |    No    |
| Start and stop a timer        |  Yes  |   Yes    |
| Create manual time entries    |  Yes  |   Yes    |
| View personal time entries    |  Yes  |   Yes    |
| View project and user reports |  Yes  |    No    |

## Project structure

```text
WorkTime/
├── backend/
│   ├── src/main/java/com/worktime/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   ├── security/
│   │   └── service/
│   ├── src/main/resources/
│   │   ├── db/migration/
│   │   └── application.properties
│   ├── src/test/
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
├── frontend/
│   ├── src/app/
│   │   ├── core/
│   │   ├── features/
│   │   └── layout/
│   ├── angular.json
│   └── package.json
└── README.md
```

## Prerequisites

Install the following tools before running the project:

- Java 21
- PostgreSQL
- Node.js
- npm
- Git

The project includes Maven Wrapper, so a separate Maven installation is not required.

## Database setup

Create a PostgreSQL database:

```sql
CREATE DATABASE worktime;
```

The backend uses the following environment variables:

| Variable      | Example                                     |
| ------------- | ------------------------------------------- |
| `DB_URL`      | `jdbc:postgresql://localhost:5432/worktime` |
| `DB_USERNAME` | `postgres`                                  |
| `DB_PASSWORD` | `your_password`                             |

Do not commit real database passwords or other secrets to the repository.

Flyway runs automatically when the backend starts and applies the migrations under:

```text
backend/src/main/resources/db/migration
```

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

This means Flyway manages the database schema while Hibernate verifies that the entity mappings match it.

## Running the backend

Open PowerShell in the backend directory:

```powershell
cd backend
```

Set the environment variables for the current terminal session:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/worktime"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_password"
```

Start the backend:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend runs at:

```text
http://localhost:8080
```

No default administrator password is committed to the repository. The application requires an administrator account created through the approved local bootstrap or database setup process.

## Running the frontend

Open a second terminal:

```powershell
cd frontend
npm install
npm start
```

The frontend runs at:

```text
http://localhost:4200
```

The frontend expects the backend at:

```text
http://localhost:8080
```

## API documentation

With the backend running, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

The OpenAPI specification is available at:

```text
http://localhost:8080/v3/api-docs
```

## Main API areas

| Area           | Base path           |
| -------------- | ------------------- |
| Authentication | `/api/auth`         |
| Users          | `/api/users`        |
| Projects       | `/api/projects`     |
| Tasks          | `/api/tasks`        |
| Time entries   | `/api/time-entries` |

Important time tracking endpoints include:

```text
POST /api/time-entries/start
POST /api/time-entries/stop
POST /api/time-entries/manual
GET  /api/time-entries
GET  /api/time-entries/active
GET  /api/time-entries/summary/daily
GET  /api/time-entries/summary/weekly
GET  /api/time-entries/reports/projects/{projectId}
GET  /api/time-entries/reports/users/{userId}
```

## Running backend tests

Set the database environment variables, then run:

```powershell
cd backend
.\mvnw.cmd test
```

The backend test suite contains service unit tests, MVC controller tests and security behavior checks.

## Building the frontend

Install dependencies and generate a production build:

```powershell
cd frontend
npm install
npm run build
```

Build output is generated under:

```text
frontend/dist/
```

## Frontend tests

Run frontend tests with:

```powershell
cd frontend
npm test
```

## Security notes

- Passwords are stored with BCrypt hashing.
- Authentication uses server-side sessions.
- Protected frontend requests include credentials.
- Authorization is also enforced on the backend.
- Deactivated users cannot sign in.
- Sensitive values must be supplied through environment variables.
- No real password or production credential should be stored in source control.

## Git workflow

Development follows a branch-based workflow:

```text
main
└── feature/descriptive-task-name
```

Typical flow:

```powershell
git switch main
git pull origin main
git switch -c feature/example-feature
```

After development and validation:

```powershell
git add <changed-files>
git commit -m "feat: describe the change"
git push -u origin feature/example-feature
```

Changes are merged into `main` through pull requests.

## Status

WorkTime currently includes the core project, task, user, time tracking, reporting and dashboard functionality. Further improvements may include expanded audit logging, additional report visualizations, pagination and broader automated test coverage.