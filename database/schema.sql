-- =====================================================
-- Real-Time Chat - MySQL schema (optional)
-- Hibernate (ddl-auto=update) creates these automatically,
-- but here is the full structure so you can see/create it by hand.
-- =====================================================
CREATE DATABASE IF NOT EXISTS chat_app_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE chat_app_db;

CREATE TABLE IF NOT EXISTS users (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  name        VARCHAR(100) NOT NULL,
  username    VARCHAR(30)  NOT NULL,
  email       VARCHAR(120) NOT NULL,
  password    VARCHAR(100) NOT NULL,              -- BCrypt hash (60 chars), never plain text
  status      VARCHAR(20)  NOT NULL DEFAULT 'OFFLINE',  -- ONLINE | OFFLINE
  created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  CONSTRAINT uk_users_username UNIQUE (username),
  CONSTRAINT uk_users_email    UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS messages (
  id           BIGINT      NOT NULL AUTO_INCREMENT,
  sender_id    BIGINT      NOT NULL,
  receiver_id  BIGINT      NOT NULL,
  content      TEXT        NOT NULL,
  sent_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  status       VARCHAR(20) NOT NULL DEFAULT 'SENT',   -- SENT | DELIVERED | READ
  PRIMARY KEY (id),
  CONSTRAINT fk_messages_sender   FOREIGN KEY (sender_id)   REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users (id) ON DELETE CASCADE,
  INDEX idx_messages_conversation (sender_id, receiver_id, sent_at),
  INDEX idx_messages_receiver_status (receiver_id, status)
) ENGINE=InnoDB;

-- Sample data: the app creates demo users (alice, bob, charlie / Password@123)
-- on first start with properly BCrypt-hashed passwords (see DataInitializer.java),
-- so no manual INSERT is needed.
