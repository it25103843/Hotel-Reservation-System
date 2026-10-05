-- Halcyon House Hotel — Payment Management module
-- Database: halcyon_hotel (MySQL 8)
-- Tables OWNED by this module.

CREATE TABLE IF NOT EXISTS cards (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  cardholder_name VARCHAR(150) NOT NULL,
  card_number VARCHAR(25) NOT NULL,
  brand VARCHAR(30) NOT NULL,
  last4 VARCHAR(4) NOT NULL,
  expiry_month INT NOT NULL,
  expiry_year INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_card_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    method VARCHAR(50),
    card_brand VARCHAR(50),
    card_last4 VARCHAR(4),
    status VARCHAR(50),
    paid_at TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id),
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
    ) ENGINE=InnoDB;
ALTER TABLE payments MODIFY COLUMN method ENUM('CARD','CASH') NULL;
