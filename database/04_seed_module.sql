-- Halcyon House Hotel — Payment Management module
-- Database: halcyon_hotel (MySQL 8)
-- Sample data for tables OWNED by this module.

INSERT IGNORE INTO cards (id, account_id, cardholder_name, card_number, brand, last4, expiry_month, expiry_year) VALUES
 (1, 3, 'Priya Nair', '4242424242424242', 'Visa', '4242', 12, 2029);

INSERT IGNORE INTO payments (id, booking_id, account_id, amount, method, status) VALUES
 (1, 1, 3, 585.00, NULL, 'UNPAID');
