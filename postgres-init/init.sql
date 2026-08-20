-- ─────────────────────────────────────────────────────────────────────────────
-- AutoDeploy Platform — PostgreSQL Initialization
-- Creates one database per microservice (database-per-service pattern).
-- This file is mounted at /docker-entrypoint-initdb.d/ in the Postgres container
-- and runs automatically on the first startup.
-- ─────────────────────────────────────────────────────────────────────────────

-- Auth Service Database
SELECT 'CREATE DATABASE auth_db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'auth_db')\gexec

-- Project Service Database
SELECT 'CREATE DATABASE project_db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'project_db')\gexec

-- Build Service Database
SELECT 'CREATE DATABASE build_db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'build_db')\gexec

-- Deployment Service Database
SELECT 'CREATE DATABASE deployment_db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'deployment_db')\gexec

-- Domain Service Database
SELECT 'CREATE DATABASE domain_db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'domain_db')\gexec

-- Notification Service Database
SELECT 'CREATE DATABASE notification_db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notification_db')\gexec

-- Grant the default postgres user privileges on all databases
GRANT ALL PRIVILEGES ON DATABASE auth_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE project_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE build_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE deployment_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE domain_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO postgres;
