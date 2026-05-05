CREATE DATABASE IF NOT EXISTS messagerie_db;
USE messagerie_db;

CREATE TABLE IF NOT EXISTS users (
id         INT AUTO_INCREMENT PRIMARY KEY,
username   VARCHAR(50) UNIQUE NOT NULL,
password   VARCHAR(255) NOT NULL,
online     BOOLEAN DEFAULT FALSE,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_username ON users(username);

CREATE TABLE IF NOT EXISTS sessions (
 id           INT AUTO_INCREMENT PRIMARY KEY,
 username     VARCHAR(50) NOT NULL,
 connected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS messages (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    expediteur   VARCHAR(50) NOT NULL,
    destinataire VARCHAR(50) NOT NULL,
    contenu      TEXT NOT NULL,
    type         VARCHAR(30) NOT NULL DEFAULT 'MESSAGE',
    envoye_le    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (expediteur)   REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (destinataire) REFERENCES users(username) ON DELETE CASCADE
    );

CREATE INDEX idx_messages_conv ON messages(expediteur, destinataire);