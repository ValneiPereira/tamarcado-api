-- TASK-BE-003: Create professionals table

CREATE TABLE professionals (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    category VARCHAR(20) NOT NULL,
    service_type VARCHAR(30) NOT NULL,
    average_rating DECIMAL(3, 2),
    total_ratings INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_professional_category ON professionals(category);
CREATE INDEX idx_professional_service_type ON professionals(service_type);
CREATE INDEX idx_professional_active ON professionals(active);