# SkillForge

SkillForge is a career and skills platform for students and recruiters. Students can build a profile, track their skills, discover internships and jobs, and manage applications. Recruiters can publish opportunities and review applicants.

## Features

- Student registration, authentication, and profile management
- Skills, projects, certifications, and experience tracking
- Opportunity discovery with eligibility and skills-match information
- Bookmarks and application status tracking
- Recruiter opportunity management and applicant review
- Sample skills, recruiter account, and opportunities seeded for local development

## Technology

| Area | Technology |
| --- | --- |
| Backend | Java 25, Spring Boot 3.5, Maven, Spring Security, Spring Data JPA |
| Frontend | React 18, React Router, Vite |
| Database | MySQL 8+ |

## Prerequisites

- JDK 25
- Maven 3.9+
- Node.js 18+ and npm
- MySQL 8+ running locally

Check the installed tools:

```powershell
java -version
mvn -version
node --version
npm --version
```

The backend's `pom.xml` targets Java 25. On Windows, make sure both `JAVA_HOME` and `PATH` point to that JDK before running Maven.

## Run locally

Run the backend and frontend in separate PowerShell terminals. Start MySQL before the backend.

### 1. Configure the database

By default, the backend connects to `jdbc:mysql://localhost:3306/skillforge` as `root` with an empty password. Hibernate creates or updates the tables on startup (`DDL_AUTO=update`), and the JDBC URL creates the database if it does not exist.

If your MySQL username or password differs, set these variables in the backend terminal before starting it:

```powershell
$env:DB_USERNAME = 'your-mysql-username'
$env:DB_PASSWORD = 'your-mysql-password'
```

You can optionally create the schema yourself with [database/schema.sql](database/schema.sql). If doing so, set `$env:DDL_AUTO = 'validate'` to have Hibernate validate the schema rather than update it.

### 2. Start the backend

From the repository root:

```powershell
Set-Location 'C:\path\to\SkillForge'
$env:JAVA_HOME = 'C:\path\to\jdk-25'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run
```

The backend listens on **http://localhost:8081** by default. To use another port, set `PORT` before starting Maven, for example:

```powershell
$env:PORT = '8082'
mvn spring-boot:run
```

Keep the terminal open while using the app.

### 3. Start the frontend

In a second PowerShell terminal:

```powershell
Set-Location 'C:\path\to\SkillForge\frontend'
npm install
$env:VITE_API_BASE_URL = 'http://localhost:8081'
npm run dev
```

If you changed the backend port, update `VITE_API_BASE_URL` to match. Open the local URL printed by Vite (normally **http://localhost:5173**).

### 4. Create an account and explore

Register through the app to create a student account. The backend seeds the starter skill catalog and sample opportunities on startup.

For recruiter testing, the local seed account is:

| Email | Password |
| --- | --- |
| `recruiter@skillforge.com` | `Recruiter123!` |

Use this demo credential only for local development. Do not expose it or the default development configuration in a deployed environment.

## Configuration

The backend reads configuration from environment variables. Defaults are defined in `src/main/resources/application.properties`.

| Variable | Purpose | Default |
| --- | --- | --- |
| `PORT` | Backend HTTP port | `8081` |
| `DB_URL` | MySQL JDBC URL | Local `skillforge` database on port `3306` |
| `DB_USERNAME` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | Empty |
| `DDL_AUTO` | Hibernate schema behavior | `update` |
| `JWT_SECRET` | Base64-encoded signing secret | Development default; replace outside local development |
| `JWT_EXPIRATION_MS` | JWT lifetime in milliseconds | `3600000` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed frontend origins | Local Vite and common development ports |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Optional admin account, created when both are set | Unset |

For example, configure an admin account before starting the backend:

```powershell
$env:ADMIN_EMAIL = 'admin@example.com'
$env:ADMIN_PASSWORD = 'use-a-strong-local-password'
```

Keep credentials and production secrets out of source control. For non-local deployments, provide a unique strong `JWT_SECRET` and suitable database credentials.

## API overview

The Spring Boot API is rooted at `/api`.

| Area | Example endpoints |
| --- | --- |
| Authentication | `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout` |
| Profile | `GET /api/profile/me`, `PUT /api/profile/me` |
| Skills | `GET /api/skills`, `/api/profile/skills` |
| Opportunities | `GET /api/opportunities`, `GET /api/opportunities/{id}` |
| Applications and bookmarks | `/api/profile/applications`, `/api/profile/bookmarks` |
| Recruiter tools | `/api/recruiter/opportunities`, `/api/recruiter/opportunities/{id}/applicants` |

The app uses bearer-token authentication for protected API requests. Additional opportunity endpoints and workflows are documented in [the opportunities module README](src/main/java/com/skillforge/opportunity/README.md).

## Run tests and build

From the repository root, with JDK 25 active:

```powershell
mvn test
```

To build the frontend production bundle:

```powershell
Set-Location 'C:\path\to\SkillForge\frontend'
npm run build
```

## Troubleshooting

- **Maven reports missing Lombok-generated methods:** Confirm Maven is using JDK 25 (`mvn -version`) and run `mvn test` from the repository root to verify compilation.
- **Database connection refused:** Start MySQL and verify `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
- **Port 8081 is already in use:** Set `PORT` to an available port and set the frontend's `VITE_API_BASE_URL` to the same backend port.
- **The frontend cannot reach the API:** Confirm the backend is running, the Vite environment variable points to the right port, and the browser page is served from an origin allowed by `CORS_ALLOWED_ORIGINS`.
- **Frontend dependencies are missing:** Run `npm install` in the `frontend` directory.

## Repository layout

```text
database/   MySQL schema
frontend/   React and Vite application
src/main/   Spring Boot application and configuration
src/test/   Backend tests
postman/    API request collection
```
