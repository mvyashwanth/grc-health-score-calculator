-- V1__init.sql
-- Initial schema for Tool-86 Health Score Calculator

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE health_records (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    age INTEGER NOT NULL CHECK (age >= 0 AND age <= 150),
    bmi DECIMAL(5,2) CHECK (bmi >= 0),
    blood_pressure_systolic INTEGER CHECK (blood_pressure_systolic >= 0),
    blood_pressure_diastolic INTEGER CHECK (blood_pressure_diastolic >= 0),
    cholesterol INTEGER CHECK (cholesterol >= 0),
    blood_sugar DECIMAL(6,2) CHECK (blood_sugar >= 0),
    exercise_hours_per_week DECIMAL(4,1) CHECK (exercise_hours_per_week >= 0),
    sleep_hours_per_day DECIMAL(3,1) CHECK (sleep_hours_per_day >= 0),
    smoking BOOLEAN NOT NULL DEFAULT FALSE,
    alcohol_units_per_week INTEGER CHECK (alcohol_units_per_week >= 0),
    stress_level INTEGER CHECK (stress_level >= 1 AND stress_level <= 10),
    health_score DECIMAL(5,2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ai_description TEXT,
    ai_recommendations TEXT,
    ai_report TEXT,
    is_fallback BOOLEAN DEFAULT FALSE,
    user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_health_records_user_id ON health_records(user_id);
CREATE INDEX idx_health_records_status ON health_records(status);
CREATE INDEX idx_health_records_created_at ON health_records(created_at);
CREATE INDEX idx_health_records_health_score ON health_records(health_score);
CREATE INDEX idx_health_records_deleted_at ON health_records(deleted_at);

-- Seed admin user (password: Admin@123 - bcrypt)
INSERT INTO users (username, email, password, role)
VALUES ('admin', 'admin@tool86.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewH7mG2H2QeVUj2G', 'ADMIN');
