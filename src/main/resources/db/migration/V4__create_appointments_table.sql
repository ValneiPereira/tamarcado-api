-- TASK-BE-003: Create appointments table

CREATE TABLE appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    professional_id UUID NOT NULL REFERENCES professionals(id) ON DELETE CASCADE,
    service_id UUID NOT NULL REFERENCES services(id) ON DELETE RESTRICT,
    date DATE NOT NULL,
    time TIME NOT NULL,
    notes VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_appointment_client ON appointments(client_id);
CREATE INDEX idx_appointment_professional ON appointments(professional_id);
CREATE INDEX idx_appointment_status ON appointments(status);
CREATE INDEX idx_appointment_date ON appointments(date);
CREATE INDEX idx_appointment_date_status ON appointments(date, status);