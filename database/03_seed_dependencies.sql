-- Halcyon House Hotel — Payment Management module
-- Database: halcyon_hotel (MySQL 8)
-- Sample data of related tables from other modules (needed for foreign keys in the seed below).

INSERT IGNORE INTO accounts (id, name, email, password_hash, role, status) VALUES
 (1, 'Imani Cole',  'admin@halcyon.com', '$2b$10$rUHsDQTMmcnV4UtAvANXf.l98UMRia0xO3dtp1AlAvVijqe201aL.', 'ADMIN',    'ACTIVE'),
 (2, 'Devon Marsh',  'staff@halcyon.com', '$2b$10$kdssFDM.EHplaFGHdOFodeuCfcTjEpQjkFMEadPFeiBvNPV3Ek5IG', 'STAFF',    'ACTIVE'),
 (3, 'Priya Nair',   'guest@halcyon.com', '$2b$10$4/sKjxkfR.WgDTWWbGdmdOF7buQMVm6Gtne/SF1zXBj7UeuQdaVzi', 'CUSTOMER', 'ACTIVE');

INSERT IGNORE INTO rooms (id, name, category, price, capacity, description, image_url, status) VALUES
 (1, 'Room 101', 'STANDARD', 120.00, 2, 'A calm, sunlit room with garden views.', 'https://picsum.photos/seed/halcyon-standard-1/640/420', 'AVAILABLE'),
 (2, 'Room 102', 'STANDARD', 120.00, 2, 'Compact and cosy, close to the courtyard.', 'https://picsum.photos/seed/halcyon-standard-2/640/420', 'AVAILABLE'),
 (3, 'Room 201', 'DELUXE',   195.00, 3, 'Extra space, a reading nook, and city views.', 'https://picsum.photos/seed/halcyon-deluxe-1/640/420', 'AVAILABLE'),
 (4, 'Room 202', 'DELUXE',   195.00, 3, 'Corner room with morning light and a soaking tub.', 'https://picsum.photos/seed/halcyon-deluxe-2/640/420', 'MAINTENANCE'),
 (5, 'Room 301', 'SUITE',    320.00, 4, 'Separate living area and a private balcony.', 'https://picsum.photos/seed/halcyon-suite-1/640/420', 'AVAILABLE'),
 (6, 'Room 302', 'SUITE',    320.00, 4, 'Two-room suite overlooking the orchard.', 'https://picsum.photos/seed/halcyon-suite-2/640/420', 'AVAILABLE'),
 (7, 'The Ardmore Penthouse', 'PENTHOUSE', 640.00, 6, 'Rooftop terrace, wraparound views, a grand piano.', 'https://picsum.photos/seed/halcyon-penthouse-1/640/420', 'AVAILABLE');

INSERT IGNORE INTO bookings (id, room_id, account_id, guest_name, check_in, check_out, status) VALUES
 (1, 3, 3, 'Priya Nair', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'CONFIRMED');
