-- TASK-BE-003: Create services table

CREATE TABLE services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    professional_id UUID NOT NULL REFERENCES professionals(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_service_professional ON services(professional_id);
CREATE INDEX idx_service_active ON services(active);