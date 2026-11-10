-- SkillForge: Authentication & User Profiles schema (MySQL 8+)
-- Optional: Hibernate (ddl-auto=update) creates these automatically. Use this file if you prefer to
-- create the schema yourself, then start the app with DDL_AUTO=validate.

CREATE DATABASE IF NOT EXISTS skillforge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE skillforge;

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,              -- BCrypt hash, never plaintext
    role          VARCHAR(20)  NOT NULL,              -- STUDENT | ADMIN
    created_at    DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student_profiles (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    user_id          BIGINT       NOT NULL,
    name             VARCHAR(255) NOT NULL,
    usn              VARCHAR(255) NULL,
    college          VARCHAR(255) NULL,
    degree           VARCHAR(255) NULL,
    branch           VARCHAR(255) NULL,
    semester         INT          NULL,
    cgpa             DOUBLE       NULL,
    phone            VARCHAR(255) NULL,
    career_goal      VARCHAR(255) NULL,
    career_interests VARCHAR(500) NULL,
    resume_url       VARCHAR(500) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_profile_user (user_id),
    UNIQUE KEY uk_profile_usn (usn),
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Master skill list shared by all modules
CREATE TABLE IF NOT EXISTS skills (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    category VARCHAR(255) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_skills_name (name)
) ENGINE=InnoDB;

-- A student's skills with proficiency (many-to-many with an extra column)
CREATE TABLE IF NOT EXISTS profile_skills (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    profile_id BIGINT      NOT NULL,
    skill_id   BIGINT      NOT NULL,
    level      VARCHAR(20) NOT NULL,                  -- BEGINNER | INTERMEDIATE | ADVANCED
    PRIMARY KEY (id),
    UNIQUE KEY uk_profile_skill (profile_id, skill_id),
    CONSTRAINT fk_ps_profile FOREIGN KEY (profile_id) REFERENCES student_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_ps_skill   FOREIGN KEY (skill_id)   REFERENCES skills (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS projects (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    profile_id  BIGINT        NOT NULL,
    title       VARCHAR(255)  NOT NULL,
    description VARCHAR(1000) NULL,
    tech_used   VARCHAR(255)  NULL,
    link        VARCHAR(255)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_project_profile FOREIGN KEY (profile_id) REFERENCES student_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS certifications (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    profile_id BIGINT       NOT NULL,
    name       VARCHAR(255) NOT NULL,
    issuer     VARCHAR(255) NULL,
    issue_date DATE         NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_cert_profile FOREIGN KEY (profile_id) REFERENCES student_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS experiences (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    profile_id  BIGINT        NOT NULL,
    company     VARCHAR(255)  NOT NULL,
    role        VARCHAR(255)  NOT NULL,
    duration    VARCHAR(255)  NULL,
    description VARCHAR(1000) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_exp_profile FOREIGN KEY (profile_id) REFERENCES student_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tokens invalidated by logout (checked on every request until they expire)
CREATE TABLE IF NOT EXISTS revoked_tokens (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    jti        VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_revoked_jti (jti)
) ENGINE=InnoDB;


-- =====================================================================
-- Person 3: Opportunities, Applications, and Bookmarks
-- =====================================================================

CREATE TABLE IF NOT EXISTS opportunities (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    recruiter_id      BIGINT        NOT NULL,
    title             VARCHAR(255)  NOT NULL,
    company           VARCHAR(255)  NOT NULL,
    location          VARCHAR(255)  NOT NULL,
    type              VARCHAR(30)   NOT NULL,              -- INTERNSHIP | JOB | HACKATHON
    description       TEXT          NOT NULL,
    deadline          DATE          NOT NULL,
    min_cgpa          DOUBLE        NULL,
    eligible_branches VARCHAR(500)  NULL,                  -- Comma-separated branch list
    eligible_years    VARCHAR(50)   NULL,                  -- Comma-separated years e.g. "3, 4"
    status            VARCHAR(20)   NOT NULL DEFAULT 'OPEN', -- OPEN | CLOSED
    created_at        DATETIME(6)   NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_opp_recruiter FOREIGN KEY (recruiter_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS opportunity_skill_requirements (
    id             BIGINT  NOT NULL AUTO_INCREMENT,
    opportunity_id BIGINT  NOT NULL,
    skill_id       BIGINT  NOT NULL,
    required_level INT     NOT NULL,                      -- 1 to 5 scale
    PRIMARY KEY (id),
    UNIQUE KEY uk_opp_skill (opportunity_id, skill_id),
    CONSTRAINT fk_osr_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities (id) ON DELETE CASCADE,
    CONSTRAINT fk_osr_skill       FOREIGN KEY (skill_id)       REFERENCES skills (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS opportunity_applications (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    opportunity_id    BIGINT      NOT NULL,
    applicant_id      BIGINT      NOT NULL,
    status            VARCHAR(30) NOT NULL DEFAULT 'APPLIED', -- APPLIED | SHORTLISTED | INTERVIEW | SELECTED | REJECTED
    applied_at        DATETIME(6) NOT NULL,
    status_updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_opp_applicant (opportunity_id, applicant_id),
    CONSTRAINT fk_app_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities (id) ON DELETE CASCADE,
    CONSTRAINT fk_app_applicant   FOREIGN KEY (applicant_id)   REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS opportunity_bookmarks (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    user_id        BIGINT      NOT NULL,
    opportunity_id BIGINT      NOT NULL,
    created_at     DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bookmark_user_opp (user_id, opportunity_id),
    CONSTRAINT fk_bm_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_bm_opp  FOREIGN KEY (opportunity_id) REFERENCES opportunities (id) ON DELETE CASCADE
) ENGINE=InnoDB;
