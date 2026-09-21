CREATE TABLE IF NOT EXISTS users (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NULL,
  email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sessions (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  token_hash CHAR(64) NOT NULL UNIQUE,
  expires_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL,
  INDEX(user_id),
  CONSTRAINT fk_sessions_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oauth_states (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  state VARCHAR(128) NOT NULL UNIQUE,
  expires_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL,
  INDEX(user_id),
  CONSTRAINT fk_oauth_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS instagram_accounts (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  instagram_user_id VARCHAR(100) NOT NULL,
  username VARCHAR(190) NULL,
  name VARCHAR(190) NULL,
  profile_picture_url TEXT NULL,
  access_token_encrypted TEXT NOT NULL,
  token_expires_at DATETIME NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uq_instagram_user(instagram_user_id),
  INDEX(user_id),
  CONSTRAINT fk_instagram_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_settings (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL UNIQUE,
  gemini_api_key_encrypted TEXT NULL,
  response_model VARCHAR(150) NOT NULL,
  analysis_model VARCHAR(150) NOT NULL,
  system_prompt TEXT NULL,
  temperature DECIMAL(3,2) NOT NULL DEFAULT 0.40,
  max_output_tokens INT NOT NULL DEFAULT 512,
  ai_enabled TINYINT(1) NOT NULL DEFAULT 1,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ai_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_knowledge (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(190) NOT NULL,
  content TEXT NOT NULL,
  type VARCHAR(40) NOT NULL DEFAULT 'permanent_instruction',
  priority INT NOT NULL DEFAULT 0,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX(user_id),
  CONSTRAINT fk_knowledge_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS conversations (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  instagram_account_id BIGINT UNSIGNED NOT NULL,
  instagram_user_id VARCHAR(100) NOT NULL,
  username VARCHAR(190) NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'ai',
  lead_status VARCHAR(30) NOT NULL DEFAULT 'new',
  ai_enabled TINYINT(1) NOT NULL DEFAULT 1,
  last_message_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uq_conversation(instagram_account_id,instagram_user_id),
  INDEX(user_id),
  CONSTRAINT fk_conv_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_conv_account FOREIGN KEY(instagram_account_id) REFERENCES instagram_accounts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS messages (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  conversation_id BIGINT UNSIGNED NOT NULL,
  sender_type ENUM('customer','ai','agent','system') NOT NULL,
  message_text TEXT NOT NULL,
  external_message_id VARCHAR(190) NULL,
  ai_generated TINYINT(1) NOT NULL DEFAULT 0,
  ai_confidence DECIMAL(5,2) NULL,
  created_at DATETIME NOT NULL,
  INDEX(conversation_id),
  UNIQUE KEY uq_external_message(external_message_id),
  CONSTRAINT fk_messages_conv FOREIGN KEY(conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS webhook_events (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  payload LONGTEXT NOT NULL,
  processed TINYINT(1) NOT NULL DEFAULT 0,
  received_at DATETIME NOT NULL,
  processed_at DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS system_logs (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NULL,
  type VARCHAR(50) NOT NULL,
  action VARCHAR(190) NOT NULL,
  input_data LONGTEXT NULL,
  output_data LONGTEXT NULL,
  status VARCHAR(30) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
