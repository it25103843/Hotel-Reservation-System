-- Payment Management — SQL equivalents of the queries used by the backend (Spring Data derived queries)

-- ===== table: cards =====
SELECT * FROM cards;
SELECT * FROM cards WHERE id=?;
SELECT * FROM cards WHERE account_id=? ORDER BY created_at DESC;   -- SavedCardRepository
DELETE FROM cards WHERE account_id=?;   -- SavedCardRepository
-- ===== table: payments =====
SELECT * FROM payments;
SELECT * FROM payments WHERE id=?;
SELECT * FROM payments WHERE account_id=? ORDER BY paid_at DESC;  -- ordered by createdAt in JPA   -- PaymentRepository
SELECT * FROM payments WHERE booking_id=?;   -- PaymentRepository
DELETE FROM payments WHERE account_id=?;   -- PaymentRepository
