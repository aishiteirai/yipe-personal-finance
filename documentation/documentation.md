# YIPE Personal Finances — Spring Boot

## 1. Technology Stack

| Layer | Choice | Rationale |
|-------|--------|-----------|
| Language | Java 21 | LTS, modern features (records, pattern matching, virtual threads) |
| Framework | Spring Boot 3.4+ | Mature, ecosystem, auto-configuration |
| Build | Maven | Standard for Spring Boot |
| ORM | Spring Data JPA + Hibernate | Automatic schema generation, repository pattern |
| Database | H2 (dev) / PostgreSQL (prod) | H2 for rapid development, PostgreSQL for production |
| Migrations | Flyway | Version-controlled schema evolution |
| Frontend | Thymeleaf + Bootstrap 5 + HTMX | Server-side rendering, minimal JS, good DX for single developer |
| Charts | Chart.js (via Thymeleaf) | Lightweight JS charting, no heavy Python dependencies |
| Security | Spring Security (form login) | Built-in, sufficient for personal/single-user app |
| Validation | Jakarta Validation | Bean validation annotations |
| DTO Mapping | MapStruct | Compile-time mapping, zero runtime overhead |
| Testing | JUnit 5 + Mockito + Spring Boot Test | Standard Spring testing stack |
| CI | GitHub Actions | Runs tests on push/PR |

**Why Thymeleaf + HTMX over a SPA?**
- Single developer maintaining the project
- No need for a separate Node.js build pipeline
- HTMX allows dynamic interactions (inline editing, modal forms) without writing JS-heavy frontend code
- Server-side rendering means business logic stays in one place
- Faster initial development velocity

---

## 2. High-Level Architecture

```
┌─────────────────────────────────────────────────┐
│                   Browser                       │
│  Thymeleaf Templates + Bootstrap 5 + HTMX +     │
│  Chart.js                                       │
└──────────────┬──────────────────────────────────┘
               │ HTTP (form submits + HTMX requests)
               ▼
┌─────────────────────────────────────────────────┐
│            Spring Boot Application              │
│  ┌──────────┐ ┌──────────┐ ┌──────────────────┐ │
│  │Controller│ │  Service │ │  Repository (JPA)│ │
│  │  Layer   │◄──► Layer  │◄──►     Layer      │ │
│  └──────────┘ └──────────┘ └──────────────────┘ │
│                      │                          │
│               ┌──────┴──────┐                   │
│               │   Model     │                   │
│               │   (Entity)  │                   │
│               └─────────────┘                   │
└──────────────────────┬──────────────────────────┘
                       │ JDBC
                ┌───────┴────────┐
                │H2 / PostgreSQL │
                └────────────────┘
```

**Layer responsibilities:**
- **Controller** — handles HTTP requests, validates input, returns Thymeleaf views or HTMX fragments
- **Service** — business logic (invoice calculation, budget rules, installment restructuring, etc.)
- **Repository** — Spring Data JPA interfaces for DB access
- **Model (Entity)** — JPA entities mapped to database tables

---

## 3. Project Structure

```
yipe-personal-finances/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/yipe/finance/
│   │   │   ├── YipeApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── WebConfig.java
│   │   │   │   └── DataInitializer.java
│   │   │   ├── controller/
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── TransactionController.java
│   │   │   │   ├── StatementController.java
│   │   │   │   ├── InvoiceController.java
│   │   │   │   ├── BudgetController.java
│   │   │   │   ├── SettingsController.java
│   │   │   │   └── ImportExportController.java
│   │   │   ├── dto/
│   │   │   │   ├── TransactionDTO.java
│   │   │   │   ├── TransactionFilterDTO.java
│   │   │   │   ├── InvoiceDTO.java
│   │   │   │   ├── BudgetPlanDTO.java
│   │   │   │   └── DashboardSummaryDTO.java
│   │   │   ├── entity/
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── TransactionType.java
│   │   │   │   ├── Category.java
│   │   │   │   ├── Account.java
│   │   │   │   ├── Card.java
│   │   │   │   └── Salary.java
│   │   │   ├── mapper/
│   │   │   │   ├── TransactionMapper.java    # not yet created
│   │   │   │   └── CardMapper.java           # not yet created
│   │   │   ├── repository/
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── AccountRepository.java
│   │   │   │   ├── CardRepository.java
│   │   │   │   └── SalaryRepository.java
│   │   │   ├── service/
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── DashboardService.java
│   │   │   │   ├── InvoiceService.java
│   │   │   │   ├── BudgetService.java
│   │   │   │   └── ImportExportService.java
│   │   │   └── exception/
│   │   │       ├── ResourceNotFoundException.java
│   │   │       └── GlobalExceptionHandler.java
│   │   ├── resources/
│   │   │   ├── application.yml
│   │   │   ├── db/migration/
│   │   │   │   ├── V1__create_initial_schema.sql
│   │   │   │   ├── V2__seed_default_data.sql
│   │   │   │   └── V3__seed_test_data.sql
│   │   │   ├── static/
│   │   │   │   ├── css/
│   │   │   │   │   └── yipe.css
│   │   │   │   └── js/
│   │   │   └── templates/
│   │   │       ├── layout.html
│   │   │       ├── dashboard.html
│   │   │       ├── transactions/
│   │   │       │   ├── list.html
│   │   │       │   ├── form.html
│   │   │       │   └── fragments.html
│   │   │       ├── statement.html
│   │   │       ├── invoices.html
│   │   │       ├── budget.html
│   │   │       ├── settings.html
│   │   │       ├── import-export.html
│   │   │       ├── login.html
│   │   │       └── error.html
│   └── test/
│       └── java/com/yipe/finance/
│           ├── YipeApplicationTests.java
│           ├── service/
│           │   ├── TransactionServiceTest.java
│           │   ├── InvoiceServiceTest.java
│           │   └── BudgetServiceTest.java
│           └── controller/
│               └── DashboardControllerTest.java
└── documentation/
    ├── documentation.md       # this file — stable project reference
    ├── development.md         # living roadmap, sprints, known issues
    ├── design.md              # design system specification
    └── YIPE-SKILLS-AND-AGENTS.md  # OpenCode tooling documentation
```

---

## 4. Database Schema (JPA Entities)

### 4.1 Transaction (`transacoes`)

| Column | Type | Notes |
|--------|------|-------|
| id | Long (PK, auto) | |
| date | LocalDate | |
| type | String (enum-like) | DEBIT, CREDIT, VR, INVESTMENT, RESERVE, INCOME |
| amount | BigDecimal | |
| category | String | FK to Category name |
| account | String | FK to Account/Card name |
| description | String | |
| installment | String | e.g. "1/12", "Recurring", "Single" |

### 4.2 Category (`categorias`)

| Column | Type |
|--------|------|
| name | String (PK) |

### 4.3 Account (`contas`)

| Column | Type |
|--------|------|
| name | String (PK) |
| type | String (BANK, VR) |

### 4.4 Card (`cartoes`)

| Column | Type |
|--------|------|
| name | String (PK) |
| bank | String |
| closingDay | Integer |
| dueDay | Integer |

### 4.5 Salary (`salarios`)

| Column | Type |
|--------|------|
| id | Long (PK, auto) |
| name | String |
| day | Integer |
| amount | BigDecimal |
| account | String |

---

## 5. Screen Designs

### 5.1 Dashboard (`/dashboard`)

**Layout:**
- Top row: 3 metric cards (General Balance, Total Invested, Adjust Balance button)
- Section "Raio-X de Hoje": 3 info cards (Credit spent today, Debit spent today, End-of-day balance) with "View in Statement" links
- Divider
- Section "Central de Análises":
  - Year/Month selector (2 dropdowns)
  - 6 Chart.js charts in a 2-column grid (line, bar, sankey/flow, waterfall, radar, yearly stacked bar)
- Sidebar: navigation menu (same 7 items as current app)

### 5.2 Transactions (`/transactions`)

**Layout:**
- Form with fields: type (dropdown), amount, date, description, account/card (dynamic based on type), category (dynamic based on type), recurring checkbox
- If recurring: radio (installment / indefinite monthly), number of months
- Submit button

**HTMX:** Type change → updates account dropdown and category visibility

### 5.3 Statement (`/statement`)

**Layout:**
- Filter section (collapsible): year, month, day, type, category dropdowns + clear button
- Data table: columns (ID, date, type, description, category, account, amount, installment), sortable
- Tools section with 3 tabs:
  1. **Edit Single** — select a row, edit fields inline, save/delete
  2. **Bulk Edit** — check rows, change account/category in bulk
  3. **Restructure Installments** — select installment group, change day/amount/count/account

### 5.4 Credit Card Invoices (`/invoices`)

**Layout:**
- Card selector (dropdown)
- Invoice reference month selector (year-month, auto-detect current)
- Metric: total invoice amount
- Due date info
- Table: date, description, category, amount, installment

### 5.5 Budget Planning (`/budget`)

**Layout:**
- Period selector (year, month)
- Income section: shows real income (auto-calculated from INCOME transactions) + editable field for projections
- Budget rule sliders: Necessities / Wants / Investments (must sum to 100%)
- Category mapping: multiselects for which categories are "Necessities" and which are "Wants"
- Progress bars (3): Necessities, Wants, Investments with color coding (green/ orange/ red)
- Warnings when over budget

### 5.6 Settings (`/settings`)

**Layout:**
- 4 tabs: Cards, Accounts/Banks, Categories, Salaries
- Each tab has an editable table with add/delete rows
- Save button per tab

### 5.7 Import/Export (`/import-export`)

**Layout:**
- Export: "Download Backup (CSV)" button → file download
- Import: file upload (CSV), preview first rows, confirm button

---

## 6. API Design (Controllers)

Server-side rendering — most controllers return full pages; some return HTML fragments for HTMX.

### 6.1 Page routes (full views)

| Method | Path | View | Description |
|--------|------|------|-------------|
| GET | `/` | redirect → `/dashboard` | Home |
| GET | `/dashboard` | `dashboard` | Main dashboard |
| GET | `/transactions` | `transactions/list` | Transaction list + new form |
| GET | `/statement` | `statement` | Statement with filters |
| GET | `/invoices` | `invoices` | Credit card invoices |
| GET | `/budget` | `budget` | Budget planning |
| GET | `/settings` | `settings` | Settings manager |
| GET | `/import-export` | `import-export` | Import/export page |

### 6.2 Action routes (form submits / HTMX)

| Method | Path | Action |
|--------|------|--------|
| POST | `/transactions` | Create new transaction (with installment expansion) |
| PUT | `/transactions/{id}` | Update single transaction |
| DELETE | `/transactions/{id}` | Delete single transaction |
| POST | `/transactions/bulk-update` | Bulk update account/category |
| POST | `/transactions/restructure` | Restructure installment group |
| GET | `/dashboard/charts?year=X&month=Y` | Return chart panel fragment |
| GET | `/statement/table?filters...` | Return filtered table fragment |
| POST | `/settings/cards` | Save cards table |
| POST | `/settings/accounts` | Save accounts table |
| POST | `/settings/categories` | Save categories table |
| POST | `/settings/salaries` | Save salaries table |
| POST | `/import/conform` | Confirm CSV import |
| GET | `/export/csv` | Download CSV backup |

---

## 7. Security

- Single user form-based login via Spring Security
- CSRF protection enabled (form submits)
- Session management for login persistence
- BCrypt password encoding
- H2 console whitelisted in dev profile

---

## 8. Development Setup

**Prerequisites:**
- Java 21+
- Maven 3.9+

**Local run:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Testing:**
```bash
mvn test
```

**Build & package:**
```bash
mvn package -DskipTests
java -jar target/yipe-finance-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

**Login:** `admin` / `admin` (dev only — hardcoded in `SecurityConfig`)

**H2 Console:** `http://localhost:8080/h2-console` (dev only)

---

## 9. OpenCode Development Tools

See [YIPE-SKILLS-AND-AGENTS.md](YIPE-SKILLS-AND-AGENTS.md) for full documentation.

### Installed Skills (Community)

| Skill | Source | Purpose |
|-------|--------|---------|
| `java-springboot` | vekzz-dev | Spring Boot patterns (DI, DTOs, validation, security) |
| `spring-boot-engineer` | synapse-ai-hub | Complete Spring Boot workflow with verification gates |
| `java-springboot-testing` | vekzz-dev | Test slices, MockMvc, Testcontainers |
| `java-junit` | vekzz-dev | JUnit 5, parametrized tests, AssertJ, Mockito |
| `java-architecture` | Happydong | Enterprise architecture, packages by feature |
| `java-ddd-patterns` | Happydong | DDD, rich domain model, MapStruct, error codes |
| `java-code-style` | Happydong | Naming, logging, exception conventions |
| `java-decoupling` | Happydong | Dependency injection, events, ports & adapters |
| `java-design-patterns` | Happydong | GoF patterns for business logic |
| `git-commit` | vekzz-dev | Conventional commits with diff analysis |
| `changelog-maintenance` | vekzz-dev | Semver, changelogs, release notes |

### Custom Agents (Project-specific)

- **`@yipe-scaffold`** — Creates a complete Spring Boot module (entity → repository → service → controller → Thymeleaf template → Flyway migration)
- **`@yipe-test-gen`** — Generates JUnit 5 + Mockito tests (unit + slice + integration) for any class

---

## 10. Migration Roadmap

All 14 phases complete — the original Python/Streamlit app has been fully ported to Spring Boot.

| Phase | Status | Tasks |
|-------|--------|-------|
| **1** | ✅ | Project setup (Maven, dependencies, application.yml, Flyway initial schema) |
| **2** | ✅ | Entities + Repositories + DataInitializer |
| **3** | ✅ | Layout template + sidebar navigation + CSS |
| **4** | ✅ | Transaction module (create, list, edit, delete, installments) |
| **5** | ✅ | Statement module (filters, table, bulk edit, installment restructure) |
| **6** | ✅ | Dashboard module (metrics, charts, daily x-ray) |
| **7** | ✅ | Invoice module (credit card bill calculation) |
| **8** | ✅ | Budget module (rule sliders, category mapping, progress bars) |
| **9** | ✅ | Settings module (CRUD tables for cards/accounts/categories/salaries) |
| **10** | ✅ | Import/Export module (CSV) |
| **11** | ✅ | Security (Spring Security, form login, logout) |
| **12** | ✅ | Testing (unit + web slice tests, 25 tests) |
| **13** | ✅ | Polish (exception handler, error page, form validation feedback, edge cases) |
| **14** | ✅ | UI Redesign — Bootstrap Icons, HTMX 2.x, Alpine.js 3.x, dark mode, skeleton loaders, mobile sidebar, toast system, `yipe.css` rewrite, login with password toggle |
| **14a** | ✅ | Fix dashboard charts — script outside `layout:fragment`, missing Chart.js CDN, missing `th:inline="javascript"` |
