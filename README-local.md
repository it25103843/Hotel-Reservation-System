# Halcyon House — Payment Management

Part of the Halcyon House Hotel Management System (Spring Boot 3 + MySQL 8 + HTML/CSS/JS).

## Folder layout
- `frontend/` – pages for this module + shared files (`css/style.css`, `js/common.js`, `index.html` login)
- `backend/` – Spring Boot sources (`pom.xml`, `src/main/java/...`, `application.properties`)
- `database/` – SQL (run in order 01 → 04; `05_queries.sql` is reference)

## Owned by this module
**Frontend:**
- `invoices.html`
- `payments.html`

**Backend (Java):**
- `CardController`
- `CardUtils`
- `Payment`
- `PaymentController`
- `PaymentRepository`
- `PaymentService`
- `SavedCard`
- `SavedCardRepository`

**Database tables:** cards, payments

## Supporting files from other modules (copied so the code compiles)
- `Account`
- `AccountRepository`
- `Booking`
- `Room`

## Shared core (identical in every module zip)
- `HotelApplication`
- `SecurityConfig`
- `JwtAuthFilter`
- `JwtUtil`
- `RoleGuard`
- `pom.xml`
- `application.properties`
- `css/style.css`
- `js/common.js`
- `index.html`

## Database setup
1. `01_schema_dependencies.sql` – related tables from other modules (if any)
2. `02_schema_module.sql` – this module's tables
3. `03_seed_dependencies.sql`, `04_seed_module.sql` – sample data

Note: the original project also runs `schema.sql`/`data.sql` automatically on startup (Spring `spring.sql.init`). Demo logins: admin@halcyon.com/admin123, staff@halcyon.com/staff123, guest@halcyon.com/guest123.
