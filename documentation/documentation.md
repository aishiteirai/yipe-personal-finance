# YIPE Personal Finances

> Personal finance control app. Track spending, manage budgets, monitor credit card invoices, and visualize your financial health — all in one place.

Built as a Spring Boot migration from an original Python/Streamlit prototype. Single-user, server-side rendered. Portuguese UI.

---

## 1. What You Can Do

### Dashboard (`/dashboard`)
See your financial snapshot at a glance:
- **General balance** — money in minus money out
- **Today's x-ray** — credit/debit spent today, end-of-day balance
- **6 interactive charts** — daily evolution (line), expenses by category (bar), money flow (sankey), monthly waterfall, category radar, yearly comparison (stacked bar)
- Year/month selector to browse historical data

### Transactions (`/transactions`)
Register new transactions with:
- Type (debit, credit, VR, investment, reserve, income)
- Amount, date, description, account/card, category
- Recurring support — installments (e.g. 1/12) or monthly recurring
- Dynamic form — changing type updates account and category options

### Statement (`/statement`)
Full transaction log with:
- Filters: year, month, day, type, category
- **Single edit** — select a row and edit inline
- **Bulk edit** — select multiple rows, change account/category at once
- **Restructure installments** — move an installment group to different day/amount/count/account

### Credit Card Invoices (`/invoices`)
- Select a card and see its invoice for any month
- Auto-calculates invoice periods based on closing day
- Shows total amount, due date, and itemized transactions

### Budget Planning (`/budget`)
50/30/20 rule-based budget:
- Auto-detects income from transactions and registered salaries
- Custom income override for projections
- Progress bars with color alerts (green → orange → red)
- Configure which categories are "Necessities" vs "Wants"

### Settings (`/settings`)
Manage your financial entities:
- Cards (name, bank, closing day, due day)
- Accounts/Banks (name, type: BANK or VR)
- Categories (name)
- Salaries (name, day, amount, account)

### Import/Export (`/import-export`)
- **Export** — download all data as CSV
- **Import** — upload CSV, preview first 10 rows, confirm

---

## 2. Tech Stack

| Layer | Choice |
|-------|--------|
| Language | Java 21 |
| Framework | Spring Boot 3.4 |
| Frontend | Thymeleaf + Bootstrap 5 + HTMX + Alpine.js |
| Charts | Chart.js |
| Database | H2 (development) / PostgreSQL (production) |
| Migrations | Flyway |
| Security | Spring Security (form login) |
| ORM | Spring Data JPA + Hibernate |
| Testing | JUnit 5 + Mockito + AssertJ |
| Build | Maven |

Thymeleaf + HTMX was chosen over a JavaScript SPA for simpler development: server-side rendering keeps business logic centralized, no Node.js build pipeline needed, and HTMX provides dynamic interactions without writing heavy frontend code.

---

## 3. How It Works (Architecture)

```
Browser (Thymeleaf + HTMX + Chart.js)
       │ HTTP requests
       ▼
Controller Layer ──► Service Layer ──► Repository Layer ──► Database
       │                  │                    │
   Validates          Business logic       Data access
   input              (budget rules,        (JPA queries)
   Returns views/     invoice periods,
   HTMX fragments     installment math)
```

**Flow:** You click a link or submit a form → the **Controller** receives the request, calls a **Service** for business logic, which queries the database through a **Repository** → the result is rendered into HTML and sent back. HTMX allows updating only parts of the page (like a chart or table) without a full reload.

---

## 4. Screens Overview

### Dashboard — Central de Análises
- Metric cards: balance, invested, adjust balance
- Today's snapshot: credit spent, debit spent, balance
- 6 charts with year/month selector

### Transaction Form
- Type selector drives which account and category fields show
- Recurring checkbox reveals installment/monthly options
- Validation feedback on submit

### Statement Table
- Collapsible filter panel
- Sortable transaction history
- 3 tool tabs: edit single, bulk edit, restructure

### Invoices
- Card selector → auto-loads available invoice periods
- Shows total, due date, itemized purchases for the period

### Budget
- Period selector (year + month)
- Income: real income or manual override
- Sliders for 50/30/20 percentages (must total 100%)
- Category multiselects for Necessities and Wants
- Live progress bars with color thresholds

### Settings
- 4 tabs: Cards, Accounts, Categories, Salaries
- Each tab: editable table with add/delete and rename support

### Import/Export
- Export downloads a CSV with all transactions
- Import uploads CSV → preview → confirm with error reporting

---

## 5. Database Structure

| Entity | Table | Key Fields |
|--------|-------|------------|
| Transaction | `transacoes` | date, type, amount, category, account, description, installment |
| Category | `categorias` | name (PK) |
| Account | `contas` | name (PK), type (BANK/VR) |
| Card | `cartoes` | name (PK), bank, closingDay, dueDay |
| Salary | `salarios` | name, day, amount, account |

Entity fields use Portuguese names (`nome`, `data`, `valor`, `tipo`). Transactions link to categories and accounts by name (string reference).

---

## 6. Routes (API)

### Pages
| Path | Description |
|------|-------------|
| `/` | Redirects to dashboard |
| `/dashboard` | Main dashboard with metrics and charts |
| `/transactions` | New transaction form |
| `/statement` | Filtered transaction table |
| `/invoices` | Credit card invoice viewer |
| `/budget` | Budget planning with 50/30/20 |
| `/settings` | Manage cards, accounts, categories, salaries |
| `/import-export` | CSV import/export |

### Actions
| Method + Path | What it does |
|--------------|--------------|
| POST `/transactions` | Create transaction (supports installments) |
| POST `/statement/{id}/update` | Edit a single transaction |
| POST `/statement/{id}/delete` | Delete a transaction |
| POST `/statement/bulk-update` | Bulk edit account/category |
| POST `/statement/restructure` | Restructure installment group |
| POST `/settings/cards/save` | Save/rename card |
| POST `/settings/accounts/save` | Save/rename account |
| POST `/settings/categories/save` | Save/rename category |
| POST `/settings/salaries/save` | Save salary |
| POST `/import-export/import/preview` | Preview CSV upload |
| POST `/import-export/import/confirm` | Confirm CSV import |
| GET `/import-export/export` | Download CSV backup |

---

## 7. Security

- Form-based login with Spring Security (single user)
- Passwords hashed with BCrypt
- CSRF protection enabled for all form submissions
- H2 console available in dev profile only
- Dev credentials: `admin` / `admin` (hardcoded — replace with env vars for production)

---

## 8. Running the App

**Prerequisites:** Java 21+, Maven 3.9+

```bash
# Run in development mode
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run all tests
mvn test

# Build production JAR
mvn package -DskipTests
java -jar target/yipe-finance-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Open `http://localhost:8080` and log in with `admin` / `admin`.

---

## 9. Project Layout

```
src/
├── main/java/com/yipe/finance/
│   ├── config/          # Security, web config, data initialization
│   ├── controller/      # 7 controllers (one per screen)
│   ├── dto/             # Data transfer objects
│   ├── entity/          # JPA entities (Transaction, Category, etc.)
│   ├── mapper/          # MapStruct mappers
│   ├── repository/      # 5 Spring Data JPA repositories
│   ├── service/         # Business logic (5 services)
│   └── exception/       # Global error handler
├── main/resources/
│   ├── db/migration/    # 3 Flyway migrations (schema + seed data)
│   ├── static/css/      # yipe.css (custom styles)
│   ├── static/js/       # yipe.js (shared utilities)
│   └── templates/       # 9 Thymeleaf templates
└── test/java/com/yipe/finance/
    ├── controller/      # 7 controller tests (@WebMvcTest)
    ├── service/         # 4 service tests
    └── repository/      # 5 repository tests (@DataJpaTest)
```

---

## 10. AI Tooling

This project includes OpenCode skills and agents to help with development:

**Custom Agents (invoke with `@name`):**
- `@yipe-scaffold` — Generate a complete CRUD module (entity → service → controller → template → migration)
- `@yipe-test-gen` — Generate unit + slice + integration tests for any class
- `@yipe-fix` — Diagnose and fix known bugs from the roadmap

**Project Skills (auto-loaded):**
- `yipe-htmx` — HTMX fragment patterns
- `yipe-entity` — JPA entity conventions (Portuguese fields)
- `yipe-controller` — Controller patterns
- `yipe-migration` — Flyway migration conventions
- `yipe-security` — Spring Security configuration
- `yipe-test-data` — Test data seeding

For full details, see [YIPE-SKILLS-AND-AGENTS.md](YIPE-SKILLS-AND-AGENTS.md).
