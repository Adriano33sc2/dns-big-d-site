-- DNS Coaching Backoffice Database Schema

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'user',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS sessions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(512) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_sessions_token ON sessions(token);
CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON sessions(user_id);

-- Default admin user (password: "admin123" — change immediately)
INSERT INTO users (username, password_hash, role) VALUES
    ('admin', '$2a$10$rK.Y5pW3qJ8vN2xL7mF9hOzE6tB1cA4dS8fG0iH2jK4lM6nO8pQ0r', 'admin')
ON CONFLICT (username) DO NOTHING;

CREATE TABLE IF NOT EXISTS build_orders (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    play_style TEXT,
    strategic_goals TEXT,
    counters TEXT,
    weaknesses TEXT,
    transition_plan TEXT,
    youtube_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS build_order_steps (
    id SERIAL PRIMARY KEY,
    build_order_id INTEGER REFERENCES build_orders(id) ON DELETE CASCADE,
    supply INTEGER,
    time_seconds INTEGER NOT NULL DEFAULT 0,
    action_name VARCHAR(255) NOT NULL,
    notes TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_steps_build_order_id ON build_order_steps(build_order_id);
