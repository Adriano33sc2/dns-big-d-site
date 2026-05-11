(ns dns-big-d-site.backend.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql])
  (:import (org.postgresql.ds PGSimpleDataSource)))

(defn get-db-url []
  (or (System/getenv "DATABASE_URL")
      "jdbc:postgresql://localhost:5432/dns_coaching"))

(defn get-db-props []
  (doto (PGSimpleDataSource.)
    (.setURL (get-db-url))
    (.setUser (or (System/getenv "DB_USER") "postgres"))
    (.setPassword (or (System/getenv "DB_PASSWORD") "postgres"))))

(defonce ^:private ds (get-db-props))

(defn init-db! []
  (jdbc/execute! ds ["SELECT 1"])
  (println "Database connection established"))

(defn insert-multi! [table columns dataset]
  (sql/insert-multi! ds table columns dataset))

(defn execute! [sql & params]
  (jdbc/execute! ds (into [sql] params)))

(defn execute-one! [sql & params]
  (jdbc/execute-one! ds (into [sql] params)))

(def migrations-sql
  "CREATE TABLE IF NOT EXISTS users (
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

  INSERT INTO users (username, password_hash, role) VALUES
    ('admin', 'bcrypt+sha512$8048648c86b9085446fccb3df12d0317$12$11561c14bb1e14de77f959746e6f14a26570164cecd5672b', 'admin')
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

  CREATE INDEX IF NOT EXISTS idx_steps_build_order_id ON build_order_steps(build_order_id);")

(defn run-migrations []
  (println "Running migrations...")
  (doseq [stmt (clojure.string/split migrations-sql #";")]
    (let [stmt (.trim stmt)]
      (when (and (not (.startsWith stmt "--"))
                 (not (= stmt "")))
        (try
          (execute! stmt)
          (catch Exception e
            (println "Migration error:" (.getMessage e))))))))
