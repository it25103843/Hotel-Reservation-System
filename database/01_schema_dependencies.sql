-- Halcyon House Hotel — Payment Management module
-- Database: halcyon_hotel (MySQL 8)
-- Parent/related tables from OTHER modules that this module needs (foreign keys / read access).
-- Run BEFORE 02_schema_module.sql.

CREATE TABLE IF NOT EXISTS accounts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('ADMIN','STAFF','CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
  status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS rooms (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  category ENUM('STANDARD','DELUXE','SUITE','PENTHOUSE') NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  capacity INT NOT NULL,
  description TEXT,
  image_url VARCHAR(500),
  status ENUM('AVAILABLE','OCCUPIED','MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE'
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bookings (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  room_id BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  guest_name VARCHAR(150),
  check_in DATE NOT NULL,
  check_out DATE NOT NULL,
  status ENUM('CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_booking_room FOREIGN KEY (room_id) REFERENCES rooms(id),
  CONSTRAINT fk_booking_account FOREIGN KEY (account_id) REFERENCES accounts(id)
) ENGINE=InnoDB;
