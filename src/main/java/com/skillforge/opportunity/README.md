# SkillForge — Opportunities & Application Management (Person 3 Module)

This module implements the **Jobs, Internships, Hackathons & Application Management** functionality for the SkillForge platform.

---

## 1. Architecture Overview

```
com.skillforge.opportunity
├── model/           # JPA entities: Opportunity, OpportunitySkillRequirement, Application, Bookmark
├── repository/      # Spring Data JPA repositories with query filtering
├── dto/             # Request & response records, match result models
├── service/         # MatchService (matching engine), OpportunityService, ApplicationService
├── controller/      # OpportunityController, RecruiterOpportunityController, RecruiterApplicationController
├── exception/       # Specialized business exceptions (mapped to uniform ApiError responses)
├── config/          # OpportunityDataSeeder (seeds demo recruiter and sample opportunities)
└── integration/     # Decoupled gateway interfaces & implementations
    ├── CurrentUserProvider      (reads current principal and user ID)
    ├── StudentProfileGateway    (reads student CGPA, branch, year, skills)
    └── SkillCatalogGateway      (resolves skills against the master catalog)
```

---

## 2. API Endpoints

### Student & Public Endpoints
| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/opportunities` | Authenticated | Browse & filter opportunities (`q`, `type`, `location`, `skillId`). Computes real-time match % and eligibility for logged-in students. |
| `GET` | `/api/opportunities/{id}` | Authenticated | Full details, required vs student skill levels, and eligibility breakdown. |
| `POST` | `/api/opportunities/{id}/apply` | Student | Apply to an open opportunity. Validates eligibility and prevents duplicate applications. |
| `POST` | `/api/opportunities/{id}/bookmark` | Student | Bookmark an opportunity. |
| `DELETE` | `/api/opportunities/{id}/bookmark` | Student | Remove bookmark. |
| `GET` | `/api/profile/bookmarks` | Student | List all bookmarked opportunities. |
| `GET` | `/api/profile/applications` | Student | List all submitted applications with status tracking. |

### Recruiter Endpoints
| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/recruiter/opportunities` | Recruiter | List all postings owned by the recruiter with applicant counts. |
| `POST` | `/api/recruiter/opportunities` | Recruiter | Create a new internship, job, or hackathon with skill requirements. |
| `PUT` | `/api/recruiter/opportunities/{id}` | Recruiter | Update posting details and required skills. |
| `DELETE` | `/api/recruiter/opportunities/{id}` | Recruiter | Delete posting and associated applications. |
| `PUT / PATCH` | `/api/recruiter/opportunities/{id}/status` | Recruiter | Toggle posting status (`OPEN` or `CLOSED`). |
| `GET` | `/api/recruiter/opportunities/{id}/applicants` | Recruiter | View candidate roster with candidate match %, missing skills, and status. |
| `PUT / PATCH` | `/api/applications/{id}/status` | Recruiter | Progress application through legal status stages. |

---

## 3. Application Status Workflow

```
   [APPLIED] 
     ├──> [SHORTLISTED] ──> [INTERVIEW] ──> [SELECTED]
     │         │                  │
     └──>──────┴──────────>───────┴───────> [REJECTED]
```
Illegal transitions (e.g. jumping directly from `APPLIED` to `SELECTED`, or updating a terminal `REJECTED` candidate) are strictly rejected with HTTP 400 Bad Request.

---

## 4. Default Seed Accounts (For Local Testing)

* **Recruiter Account:**
  * **Email:** `recruiter@skillforge.com`
  * **Password:** `Recruiter123!`
  * **Role:** `RECRUITER`

* **Sample Opportunities Seeded Automatically:**
  1. **Backend Engineering Intern** (`INTERNSHIP`) — Apex Cloud Systems, Bangalore
  2. **Full Stack Software Engineer** (`JOB`) — Nexus Digital Technologies, Hyderabad
  3. **National AI & Web Innovation Hackathon 2026** (`HACKATHON`) — SkillForge Foundation, Remote
